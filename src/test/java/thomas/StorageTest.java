package thomas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import thomas.task.DeadlineTask;
import thomas.task.EventTask;
import thomas.task.Task;
import thomas.task.TodoTask;

/**
 * Tests {@link Storage}.
 * <p>
 * This is the class where a bug costs the user something they cannot get back:
 * every other mistake in the program is visible on screen and survives until the
 * next command, while a save file written wrongly or read wrongly loses tasks
 * silently, between one run and the next. That is what makes it worth testing
 * ahead of the display code.
 * <p>
 * Decoding is the part with the decisions in it, and it is deliberately strict:
 * each task type is written with an exact number of fields, so a description
 * that happens to contain the field separator is refused rather than loaded back
 * truncated. A damaged line is skipped and complained about rather than
 * abandoning the file, so the cases below check both halves of that -- what was
 * skipped, and that everything else still loaded.
 * <p>
 * {@code parseSavedTask} is private, which is right: nothing outside needs to
 * turn a line into a task. It is reached here through {@link Storage#load()}
 * against a real file in a temporary folder, which is what {@code Storage}
 * taking its path as an argument is for. JUnit creates and removes that folder,
 * so no case can see another's file.
 */
public class StorageTest {

    /** A folder JUnit makes fresh for each test and deletes afterwards. */
    @TempDir
    private Path folder;

    /** The save file each case works on, inside that folder. */
    private Path saveFile() {
        return folder.resolve("tasklist.txt");
    }

    /** Creates a storage over this case's save file. */
    private Storage storage() {
        return new Storage(saveFile().toString());
    }

    /** Writes the given lines to the save file as the whole of its contents. */
    private void writeSaveFile(String... lines) throws IOException {
        Files.write(saveFile(), List.of(lines));
    }

    /** Returns the save file's lines, ignoring how they were separated. */
    private List<String> readSaveFile() throws IOException {
        return Files.readAllLines(saveFile());
    }

    // ---- load: the file itself ----

    /** A missing file is a first run, not a failure. */
    @Test
    public void load_missingFile_returnsEmptyList() throws IOException {
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    @Test
    public void load_emptyFile_returnsEmptyList() throws IOException {
        writeSaveFile();
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    /** Blank lines are passed over quietly: they are not damage worth reporting. */
    @Test
    public void load_blankLines_skippedWithoutComplaint() throws IOException {
        writeSaveFile("", "T | 0 | read book", "   ");
        Storage storage = storage();

        ArrayList<Task> tasks = storage.load();

        assertEquals(1, tasks.size());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    // ---- load: lines that decode ----

    @Test
    public void load_todoLine_returnsTodo() throws IOException {
        writeSaveFile("T | 0 | read book");

        assertEquals("[T][ ] read book", storage().load().get(0).toString());
    }

    @Test
    public void load_deadlineLine_returnsDeadline() throws IOException {
        writeSaveFile("D | 0 | return book | 2019-12-02 1800");

        assertEquals("[D][ ] return book (by: Dec 02 2019, 6:00 PM)",
                storage().load().get(0).toString());
    }

    @Test
    public void load_eventLine_returnsEvent() throws IOException {
        writeSaveFile("E | 0 | project meeting | 2019-12-02 1400 | 2019-12-02 1600");

        assertEquals("[E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 02 2019, 4:00 PM)",
                storage().load().get(0).toString());
    }

    @Test
    public void load_doneFlagSet_taskIsDone() throws IOException {
        writeSaveFile("T | 1 | read book");

        assertEquals("[T][X] read book", storage().load().get(0).toString());
    }

    @Test
    public void load_doneFlagClear_taskIsNotDone() throws IOException {
        writeSaveFile("T | 0 | read book");

        assertEquals("[T][ ] read book", storage().load().get(0).toString());
    }

    /**
     * Only "1" means done, and anything else is read as not done rather than as
     * a corrupt line: a damaged flag costs the tick, not the whole task.
     */
    @Test
    public void load_damagedDoneFlag_taskLoadsAsNotDone() throws IOException {
        writeSaveFile("T | x | read book");
        Storage storage = storage();

        ArrayList<Task> tasks = storage.load();

        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertTrue(storage.getSkippedLines().isEmpty());
    }

    @Test
    public void load_severalLines_returnsThemInFileOrder() throws IOException {
        writeSaveFile("T | 0 | read book",
                "D | 1 | return book | 2019-12-02 1800",
                "T | 0 | buy book");

        ArrayList<Task> tasks = storage().load();

        assertEquals(3, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertEquals("[D][X] return book (by: Dec 02 2019, 6:00 PM)", tasks.get(1).toString());
        assertEquals("[T][ ] buy book", tasks.get(2).toString());
    }

    // ---- load: lines that do not decode ----

    @Test
    public void load_unknownTypeLetter_lineSkipped() throws IOException {
        writeSaveFile("X | 0 | read book");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("unknown task type 'X': X | 0 | read book"),
                storage.getSkippedLines());
    }

    @Test
    public void load_tooFewFields_lineSkipped() throws IOException {
        writeSaveFile("T | 0");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("too few fields: T | 0"), storage.getSkippedLines());
    }

    /**
     * The field count is exact rather than a minimum, which is what catches a
     * description containing the separator: it splits into an extra field, and
     * loading it back would silently truncate the description.
     */
    @Test
    public void load_descriptionContainingSeparator_lineSkipped() throws IOException {
        writeSaveFile("T | 0 | read book | and return it");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("expected 3 fields but found 4: T | 0 | read book | and return it"),
                storage.getSkippedLines());
    }

    /**
     * Splitting keeps trailing empty fields, so a line ending in a separator is
     * reported rather than quietly losing its last field.
     */
    @Test
    public void load_trailingSeparator_lineSkipped() throws IOException {
        writeSaveFile("T | 0 | read book | ");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("expected 3 fields but found 4: T | 0 | read book | "),
                storage.getSkippedLines());
    }

    @Test
    public void load_deadlineMissingDate_lineSkipped() throws IOException {
        writeSaveFile("D | 0 | return book");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("expected 4 fields but found 3: D | 0 | return book"),
                storage.getSkippedLines());
    }

    @Test
    public void load_eventMissingEndDate_lineSkipped() throws IOException {
        writeSaveFile("E | 0 | project meeting | 2019-12-02 1400");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("expected 5 fields but found 4: E | 0 | project meeting | 2019-12-02 1400"),
                storage.getSkippedLines());
    }

    /** A date the parser cannot read is reported in the same words the user sees. */
    @Test
    public void load_unreadableDate_lineSkipped() throws IOException {
        writeSaveFile("D | 0 | return book | tomorrow");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("I can't read 'tomorrow' as a deadline date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800."),
                storage.getSkippedLines());
    }

    /**
     * Loading goes through {@link EventTask}'s constructor, so a line edited by
     * hand to run backwards is refused here too rather than loading an event no
     * command could have created.
     */
    @Test
    public void load_eventEndingBeforeItStarts_lineSkipped() throws IOException {
        writeSaveFile("E | 0 | project meeting | 2019-12-02 1600 | 2019-12-02 1400");
        Storage storage = storage();

        assertTrue(storage.load().isEmpty());
        assertEquals(List.of("HUH?! Your event ends before it starts! Check your /from and /to."),
                storage.getSkippedLines());
    }

    /** One damaged line must not cost the user every other task in the file. */
    @Test
    public void load_someLinesDamaged_othersStillLoad() throws IOException {
        writeSaveFile("T | 0 | read book",
                "X | 0 | nonsense",
                "T | 0 | buy book");
        Storage storage = storage();

        ArrayList<Task> tasks = storage.load();

        assertEquals(2, tasks.size());
        assertEquals("[T][ ] read book", tasks.get(0).toString());
        assertEquals("[T][ ] buy book", tasks.get(1).toString());
        assertEquals(1, storage.getSkippedLines().size());
    }

    /** Complaints come back in the order the lines appeared. */
    @Test
    public void load_severalLinesDamaged_complaintsInFileOrder() throws IOException {
        writeSaveFile("X | 0 | first", "T | 0", "Y | 0 | third");
        Storage storage = storage();

        storage.load();

        assertEquals(List.of("unknown task type 'X': X | 0 | first",
                "too few fields: T | 0",
                "unknown task type 'Y': Y | 0 | third"),
                storage.getSkippedLines());
    }

    /**
     * The complaints are cleared at the start of each load, so a second load
     * reports what that load skipped rather than everything ever skipped.
     */
    @Test
    public void load_calledTwice_reportsOnlyTheLastLoad() throws IOException {
        writeSaveFile("X | 0 | nonsense");
        Storage storage = storage();
        storage.load();

        writeSaveFile("T | 0 | read book");
        storage.load();

        assertTrue(storage.getSkippedLines().isEmpty());
    }

    // ---- save ----

    @Test
    public void save_severalTasks_writesOneLinePerTask() throws IOException, ThomasException {
        TaskList tasks = new TaskList();
        tasks.add(new TodoTask("read book"));
        tasks.add(new DeadlineTask("return book", LocalDateTime.of(2019, 12, 2, 18, 0)));
        tasks.add(new EventTask("project meeting",
                LocalDateTime.of(2019, 12, 2, 14, 0), LocalDateTime.of(2019, 12, 2, 16, 0)));

        storage().save(tasks);

        assertEquals(List.of("T | 0 | read book",
                "D | 0 | return book | 2019-12-02 1800",
                "E | 0 | project meeting | 2019-12-02 1400 | 2019-12-02 1600"),
                readSaveFile());
    }

    @Test
    public void save_doneTask_writesTheDoneFlag() throws IOException {
        TaskList tasks = new TaskList();
        Task task = new TodoTask("read book");
        task.markAsDone();
        tasks.add(task);

        storage().save(tasks);

        assertEquals(List.of("T | 1 | read book"), readSaveFile());
    }

    @Test
    public void save_emptyList_writesEmptyFile() throws IOException {
        storage().save(new TaskList());

        assertTrue(readSaveFile().isEmpty());
    }

    /** Saving replaces the file, so tasks deleted in this run do not come back. */
    @Test
    public void save_overExistingFile_replacesItsContents() throws IOException {
        writeSaveFile("T | 0 | read book", "T | 0 | return book");
        TaskList tasks = new TaskList();
        tasks.add(new TodoTask("buy book"));

        storage().save(tasks);

        assertEquals(List.of("T | 0 | buy book"), readSaveFile());
    }

    /**
     * Saving runs after every change to the task list, so it must only read the
     * list: emptying it here would delete the tasks it is meant to be saving.
     */
    @Test
    public void save_anyList_leavesTheListUnchanged() throws IOException {
        TaskList tasks = new TaskList();
        tasks.add(new TodoTask("read book"));

        storage().save(tasks);

        assertEquals(1, tasks.size());
    }

    /** The folder a save file sits in is created if it is not there yet. */
    @Test
    public void save_missingFolder_createsIt() throws IOException {
        Path nested = folder.resolve("data").resolve("tasklist.txt");
        TaskList tasks = new TaskList();
        tasks.add(new TodoTask("read book"));

        new Storage(nested.toString()).save(tasks);

        assertTrue(new File(nested.toString()).exists());
        assertEquals(List.of("T | 0 | read book"), Files.readAllLines(nested));
    }

    // ---- the two sides together ----

    /**
     * The point of the whole class: whatever is written can be read back as the
     * same tasks. Encoding lives on the tasks and decoding lives in
     * {@code Storage}, so only a test that runs both catches the two drifting
     * apart -- each side on its own is self-consistent.
     */
    @Test
    public void saveThenLoad_everyTaskType_survivesTheRoundTrip() throws IOException, ThomasException {
        TaskList original = new TaskList();
        Task todo = new TodoTask("read book");
        todo.markAsDone();
        original.add(todo);
        original.add(new DeadlineTask("return book", LocalDateTime.of(2019, 12, 2, 18, 0)));
        original.add(new EventTask("project meeting",
                LocalDateTime.of(2019, 12, 2, 14, 0), LocalDateTime.of(2019, 12, 4, 16, 0)));
        Storage storage = storage();

        storage.save(original);
        ArrayList<Task> loaded = storage.load();

        assertEquals(3, loaded.size());
        assertTrue(storage.getSkippedLines().isEmpty());
        for (int i = 0; i < original.size(); i++) {
            assertEquals(original.get(i).toString(), loaded.get(i).toString());
        }
    }

    /** A description with awkward characters still comes back whole. */
    @Test
    public void saveThenLoad_descriptionWithSpacesAndPunctuation_survives() throws IOException {
        TaskList original = new TaskList();
        original.add(new TodoTask("read  book (chapter 3) -- twice!"));
        Storage storage = storage();

        storage.save(original);

        assertEquals("[T][ ] read  book (chapter 3) -- twice!",
                storage.load().get(0).toString());
    }

    /**
     * A description containing the field separator cannot survive, and is lost
     * on the way back in rather than coming back truncated. This is the known
     * cost of the format, recorded here so that changing it is a deliberate act
     * rather than an accident.
     */
    @Test
    public void saveThenLoad_descriptionContainingSeparator_isSkipped() throws IOException {
        TaskList original = new TaskList();
        original.add(new TodoTask("read book | and return it"));
        Storage storage = storage();

        storage.save(original);
        ArrayList<Task> loaded = storage.load();

        assertTrue(loaded.isEmpty());
        assertFalse(storage.getSkippedLines().isEmpty());
    }
}
