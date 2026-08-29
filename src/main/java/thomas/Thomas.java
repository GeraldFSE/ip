package thomas;

import java.io.IOException;

import thomas.command.Command;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * Thomas reads commands from standard input and stores three kinds of task:
 * {@code todo}, {@code deadline ... /by ...} and
 * {@code event ... /from ... /to ...}. The stored tasks are printed on
 * {@code list} and on {@code on <day>}, can be marked done with {@code mark} and
 * not done again with {@code unmark}, and removed with {@code delete}. It stops
 * when the user types {@code bye} or the input ends.
 * <p>
 * The list survives between runs: it is loaded from {@value #DATA_PATH} at
 * start-up and written back after every command that changes it, so no task is
 * lost even if the program is closed without typing {@code bye}.
 * <p>
 * None of that is done here. Talking to the user belongs to {@link Ui}, the save
 * file to {@link Storage}, holding the tasks to {@link TaskList}, understanding
 * a typed line to {@link Parser}, and carrying out what it asked for to a
 * {@link Command}. This class only assembles those parts and turns the handle:
 * read a line, parse it, run it, repeat.
 */
public class Thomas {
    /**
     * Where the task list is saved, as a path from the project root.
     * <p>
     * Relative paths resolve against the directory the program was started
     * from, so this assumes Thomas is run from the project root, as the test
     * script does. It is chosen here, rather than inside {@link Storage},
     * because deciding where the tasks live is this program's business while
     * reading and writing them is Storage's.
     */
    private static final String DATA_PATH = "./data/tasklist.txt";

    /** Reads and writes the save file. */
    private final Storage storage;

    /** Everything the user sees and types. */
    private final Ui ui;

    /**
     * The tasks being tracked.
     * <p>
     * Not final: an unreadable save file leaves this holding a fresh empty list
     * instead of the loaded one.
     */
    private TaskList tasks;

    /**
     * Starts a chatbot over one save file, loading whatever it already holds.
     * <p>
     * The greeting is printed before loading, so that any complaint about the
     * save file arrives after Thomas has introduced itself rather than before.
     *
     * @param filePath where the task list is kept
     */
    public Thomas(String filePath) {
        // One Ui for the whole session: it owns the Scanner over standard
        // input, and a second one would buffer ahead and swallow commands.
        ui = new Ui();
        ui.showWelcome();

        storage = new Storage(filePath);
        try {
            // Storage hands back plain tasks; wrapping them in a TaskList is
            // what adds the checked operations the commands rely on.
            tasks = new TaskList(storage.load());
            // Storage records the lines it could not read instead of printing
            // them, so they are shown here, where the Ui is.
            for (String skipped : storage.getSkippedLines()) {
                ui.showSkippedLine(skipped);
            }
        } catch (IOException e) {
            // An unreadable save file should not stop the chatbot: say so and
            // carry on with an empty list rather than dying with a stack trace.
            ui.showLoadingError(e.getMessage());
            tasks = new TaskList();
        }
    }

    /**
     * Reads and carries out commands until the user says {@code bye} or the
     * input ends.
     * <p>
     * The loop no longer knows what any command does. It reads a line, asks
     * {@link Parser} for the command it names, runs it, and asks whether that
     * was the last one -- so a new command is a new class, and this method never
     * changes again.
     * <p>
     * Both ways of ending are handled: {@code bye} answers true to
     * {@link Command#isExit()}, and input that simply runs out fails
     * {@link Ui#hasNextCommand()}. The farewell sits after the loop because it
     * is owed in both cases.
     */
    public void run() {
        boolean isExit = false;
        while (!isExit && ui.hasNextCommand()) {
            try {
                Command command = Parser.parse(ui.readCommand());
                command.execute(tasks, ui, storage);
                isExit = command.isExit();
            } catch (ThomasException e) {
                // Every user mistake, whether noticed while reading the line or
                // while carrying it out, arrives here as one kind of exception
                // and is reported the same way. That is what keeps both the
                // parser and the commands free of error handling.
                ui.showError(e.getMessage());
            }
        }

        // No save here: every command that changes the list has already saved,
        // so the file is current even if the program never reaches this point.
        ui.showGoodbye();
    }

    /**
     * Starts the chatbot.
     *
     * @param args command line arguments; unused
     */
    public static void main(String[] args) {
        new Thomas(DATA_PATH).run();
    }
}