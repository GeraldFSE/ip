import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Loads tasks from the save file and writes them back to it.
 * <p>
 * The save file format is known here and nowhere else: the encoding side lives
 * on {@link Task#toSaveFormat()} and the decoding side lives in this class, so
 * changing how a task is stored means changing those two places and no others.
 * The rest of the program asks for tasks and hands back tasks, never lines.
 * <p>
 * A {@code Storage} is created with the path it works on rather than reading a
 * constant, so the caller decides where tasks are kept -- which is also what
 * lets a test point one at a throwaway file.
 */
public class Storage {
    /** Where the task list is read from and written to. */
    private final String filePath;

    /**
     * Save file lines from the last {@link #load()} that could not be read.
     * <p>
     * Collected rather than printed, because printing is {@link Ui}'s job and
     * this class does not know about the screen. The caller shows them after
     * loading, which is when they were noticed, so the user still sees them
     * before the first command runs.
     */
    private final ArrayList<String> skippedLines = new ArrayList<>();

    /**
     * Creates a storage over one file.
     * <p>
     * A relative path resolves against the directory the program was started
     * from. Nothing is opened here: a save file that does not exist yet is the
     * normal first run, and is dealt with by {@link #load()}.
     *
     * @param filePath where the task list is kept, for example
     *                 {@code "./data/tasklist.txt"}
     */
    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Checks that a save file line holds exactly the fields its type needs.
     *
     * @param fields   the line already split on the field separator
     * @param expected how many fields this task type is written with
     * @param line     the original line, quoted back in the error message
     * @throws ThomasException if the count does not match
     */
    private static void requireFieldCount(String[] fields, int expected, String line)
            throws ThomasException {
        if (fields.length != expected) {
            throw new ThomasException("expected " + expected + " fields but found "
                    + fields.length + ": " + line);
        }
    }

    /**
     * Turns one line of the save file back into a task.
     * <p>
     * The line is split on the field separator rather than parsed out of the
     * display text, so the shape is fixed and known: type letter, done flag,
     * description, then whatever extra fields that type carries.
     *
     * @param line one line of the save file, without its line separator
     * @return the task the line describes
     * @throws ThomasException if the type is unknown or fields are missing
     */
    private static Task parseSavedTask(String line) throws ThomasException {
        // -1 keeps trailing empty fields, so a line ending in a separator is
        // reported as corrupt below rather than silently shortening the array.
        // The separator is a regex here, so its | must be escaped.
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3) {
            throw new ThomasException("too few fields: " + line);
        }

        // Each type has an exact field count. Checking for exactly the right
        // number, rather than at least it, is what catches a description that
        // itself contains " | ": that splits into an extra field and would
        // otherwise be loaded back silently truncated.
        String description = fields[2];
        Task task = switch (fields[0]) {
        case "T" -> {
            requireFieldCount(fields, 3, line);
            yield new TodoTask(description);
        }
        case "D" -> {
            requireFieldCount(fields, 4, line);
            LocalDateTime byDate = Task.parseDate(fields[3], "a deadline date");
            yield new DeadlineTask(description, byDate);
        }
        case "E" -> {
            requireFieldCount(fields, 5, line);
            LocalDateTime fromDate = Task.parseDate(fields[3], "a start date");
            LocalDateTime toDate = Task.parseDate(fields[4], "an end date");
            yield new EventTask(description, fromDate, toDate);
        }
        default -> throw new ThomasException("unknown task type '" + fields[0] + "': " + line);
        };

        // "1" means done; anything else is treated as not done, so a damaged
        // flag costs the tick rather than the whole task.
        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Returns the complaints about lines the last {@link #load()} had to skip.
     * <p>
     * Empty when the file was read cleanly, which is the usual case. The caller
     * shows these through {@link Ui}, so that this class stays free of any
     * knowledge of how the user is talked to.
     *
     * @return one message per skipped line, in the order the lines appeared
     */
    public ArrayList<String> getSkippedLines() {
        return skippedLines;
    }

    /**
     * Reads the saved tasks.
     * <p>
     * A missing file is the normal first run, not an error, so it gives back an
     * empty list. Individual unreadable lines are skipped and recorded in
     * {@link #getSkippedLines()} rather than abandoning the whole file: one
     * damaged line should not cost the user every other task.
     *
     * @return the tasks the file holds, in the order they were written
     * @throws IOException if the file exists but cannot be read
     */
    public ArrayList<Task> load() throws IOException {
        // Cleared rather than appended to, so a second load reports only what
        // that load skipped instead of everything ever skipped.
        skippedLines.clear();

        ArrayList<Task> tasks = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return tasks;
        }

        // try-with-resources: the Scanner holds a real file handle, so it is
        // closed however this block ends, including on an exception.
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                // nextLine(), not next(): descriptions contain spaces, and
                // next() would hand back one word at a time.
                String current = scan.nextLine();
                if (current.isBlank()) {
                    continue;
                }
                try {
                    tasks.add(parseSavedTask(current));
                } catch (ThomasException e) {
                    skippedLines.add(e.getMessage());
                }
            }
        }
        return tasks;
    }

    /**
     * Writes every task to the save file, replacing what was there before.
     * <p>
     * The list is only read, never emptied: this runs after every change to the
     * task list, so mutating it here would delete the tasks it is meant to be
     * saving.
     *
     * @param tasks the tasks to write, left unchanged
     * @throws IOException if the folder or file cannot be written
     */
    public void save(TaskList tasks) throws IOException {
        File file = new File(filePath);

        // FileWriter cannot create missing folders, so ./data must be made
        // first. mkdirs() creates every missing level and is a no-op when they
        // already exist. getParentFile() is null for a bare filename.
        File folder = file.getParentFile();
        if (folder != null) {
            folder.mkdirs();
        }

        // try-with-resources: closing is what flushes buffered text to disk, so
        // skipping it on an exception would lose the tasks.
        try (FileWriter fw = new FileWriter(file)) {
            for (int i = 0; i < tasks.size(); i++) {
                fw.write(tasks.get(i).toSaveFormat() + System.lineSeparator());
            }
        }
    }
}