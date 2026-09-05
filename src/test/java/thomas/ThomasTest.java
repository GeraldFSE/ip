package thomas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests Thomas.getResponse and the two questions the GUI asks after it.
 * This is the whole of the GUI's path through the chatbot: the window parses
 * nothing and runs nothing itself, it types a line in here and paints whatever
 * comes back. The console path is covered end to end by the text-UI plan, which
 * never reaches this method, so without these cases the GUI has no coverage at
 * all.
 * The three things worth pinning are that the reply is the same wording the
 * console gets, that a mistake comes back as a reply rather than as an
 * exception through the window, and that the two pieces of state left behind --
 * the command type the dialog box colors by, and whether the session is over --
 * say what just happened rather than what happened before it.
 * Each case gets its own save file in a temporary folder, so no case sees
 * another's tasks.
 */
public class ThomasTest {

    /** Folder JUnit makes fresh for each test and deletes afterwards */
    @TempDir
    private Path folder;

    /**
     * Returns a chatbot saving into this case's own folder.
     *
     * @return New chatbot, with an empty task list.
     */
    private Thomas chatbot() {
        return new Thomas(folder.resolve("tasklist.txt").toString());
    }

    /**
     * Writes a save file for this case to load, one line per argument.
     * The lines are taken as separate arguments rather than as one string with
     * newlines in it, so a case shows the file as the loader sees it -- a line
     * at a time -- and cannot say "two lines" while writing one. The same
     * helper, for the same reason, is in {@code StorageTest}.
     *
     * @param lines Save file lines, in order, without line separators.
     */
    private void writeSaveFile(String... lines) throws IOException {
        Files.write(folder.resolve("tasklist.txt"), List.of(lines));
    }

    @Test
    public void getStartupMessage_readableSaveFile_isGreetingAlone() {
        // The banner the console prints above this is deliberately not here: it
        // is ASCII art, and the window draws text in a proportional font.
        assertEquals("Choo Choo! I'm Thomas!\n"
                + "How can I serve you today?",
                chatbot().getStartupMessage());
    }

    @Test
    public void getStartupMessage_damagedSaveFileLine_warnsAfterGreeting() throws IOException {
        writeSaveFile("T | 0 | read book", "X | 0 | mystery");

        // The damaged line costs only itself: the greeting still arrives, the
        // warning follows it, and the readable task above it still loaded.
        Thomas thomas = chatbot();
        assertEquals("Choo Choo! I'm Thomas!\n"
                + "How can I serve you today?\n"
                + "Skipping a line I could not read: unknown task type 'X': X | 0 | mystery",
                thomas.getStartupMessage());
        assertEquals("Here are the tasks in your list:\n"
                + "1. [T][ ] read book",
                thomas.getResponse("list"));
    }

    @Test
    public void getResponse_todo_returnsSameConfirmationAsConsole() {
        Thomas thomas = chatbot();

        // The wording is Ui's, and the message carries its own lines only: the
        // dividers and indentation the console shows are added when printing.
        assertEquals("Got it. I've added this task:\n"
                + "   [T][ ] read book\n"
                + "Now you have 1 task(s) in the list.",
                thomas.getResponse("todo read book"));
    }

    @Test
    public void getResponse_todo_recordsCommandType() {
        Thomas thomas = chatbot();
        thomas.getResponse("todo read book");

        // The simple name, since that is what DialogBox matches its style
        // classes against.
        assertEquals("AddCommand", thomas.getCommandType());
        assertFalse(thomas.isDone());
    }

    @Test
    public void getResponse_unknownCommand_returnsErrorMessage() {
        Thomas thomas = chatbot();

        // A mistake is a reply like any other here. The console prints it
        // through Ui.showError instead, but the words are the same.
        assertEquals("Erm sorry, what does that mean again?",
                thomas.getResponse("blah"));
    }

    @Test
    public void getResponse_unknownCommand_clearsCommandType() {
        Thomas thomas = chatbot();
        thomas.getResponse("todo read book");
        thomas.getResponse("blah");

        // No command ran, so the previous command's color must not be left
        // behind for the error bubble to pick up.
        assertEquals("", thomas.getCommandType());
    }

    @Test
    public void getResponse_bye_reportsSessionDone() {
        Thomas thomas = chatbot();

        assertEquals("Until next time! Choo Choo!", thomas.getResponse("bye"));
        assertTrue(thomas.isDone());
    }

    @Test
    public void isDone_beforeAnyCommand_isFalse() {
        assertFalse(chatbot().isDone());
    }

    @Test
    public void getCommandType_beforeAnyCommand_isEmpty() {
        // Not null: DialogBox switches on this value, and a null would close
        // the window with an exception before the first bubble is drawn.
        assertEquals("", chatbot().getCommandType());
    }

    @Test
    public void getResponse_deleteMissingTask_returnsErrorAndClearsCommandType() {
        Thomas thomas = chatbot();
        thomas.getResponse("todo read book");

        // A command that parses but cannot be carried out fails the same way an
        // unparseable one does, which is what keeps the window free of error
        // handling.
        assertEquals("There is no task 5! You only have 1 task(s).",
                thomas.getResponse("delete 5"));
        assertEquals("", thomas.getCommandType());
    }
}
