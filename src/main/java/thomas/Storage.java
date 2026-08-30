package thomas;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

import thomas.task.DeadlineTask;
import thomas.task.EventTask;
import thomas.task.Task;
import thomas.task.TodoTask;

/**
 * Loads tasks from the save file and writes them back to it.
 * The decoding side of the save file format is known here and the encoding side
 * on the tasks themselves, so that the rest of the program asks for tasks and
 * hands back tasks, never lines.
 * The path is given by the caller rather than read from a constant, so that the
 * caller decides where tasks are kept.
 */
public class Storage {
    /** Path the task list is read from and written to */
    private final String filePath;

    /** Complaints about save file lines the last load could not read */
    private final ArrayList<String> skippedLines = new ArrayList<>();

    /**
     * Creates a storage over one file.
     * A relative path resolves against the directory the program was started
     * from. Nothing is opened here, since a save file that does not exist yet is
     * the normal first run.
     *
     * @param filePath Path the task list is kept at, for example
     *                 "./data/tasklist.txt".
     */
    public Storage(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Checks that a save file line holds exactly the fields its type needs.
     *
     * @param fields Line already split on the field separator.
     * @param expected Number of fields this task type is written with.
     * @param line Original line, quoted back in the error message.
     * @throws ThomasException If the count does not match.
     */
    private static void requireFieldCount(String[] fields, int expected, String line)
            throws ThomasException {
        if (fields.length != expected) {
            throw new ThomasException("expected " + expected + " fields but found "
                    + fields.length + ": " + line);
        }
    }

    /**
     * Returns the task described by one line of the save file.
     * The line is split on the field separator, so its shape is fixed and known:
     * type letter, done flag, description, then any extra fields that type
     * carries.
     *
     * @param line One line of the save file, without its line separator.
     * @return Task the line describes.
     * @throws ThomasException If the type is unknown or fields are missing.
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
     * Returns the complaints about lines the last load had to skip.
     * Empty when the file was read cleanly, which is the usual case. The caller
     * shows these, so that this class stays free of any knowledge of how the
     * user is talked to.
     *
     * @return One message per skipped line, in the order the lines appeared.
     */
    public ArrayList<String> getSkippedLines() {
        return skippedLines;
    }

    /**
     * Returns the saved tasks.
     * If the file does not exist the list is empty, since that is the normal
     * first run. If a single line cannot be read it is skipped and recorded
     * rather than abandoning the whole file.
     *
     * @return Tasks the file holds, in the order they were written.
     * @throws IOException If the file exists but cannot be read.
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
     * The list is only read, never emptied, since this runs after every change
     * to the task list.
     *
     * @param tasks Tasks to write, left unchanged.
     * @throws IOException If the folder or file cannot be written.
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
