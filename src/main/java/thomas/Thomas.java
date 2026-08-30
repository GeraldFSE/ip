package thomas;

import java.io.IOException;

import thomas.command.Command;

/**
 * Entry point for the Thomas chatbot.
 * Thomas reads commands from standard input, stores to-dos, deadlines and
 * events, and stops when the user types bye or the input ends.
 * The task list survives between runs, since it is loaded at start-up and
 * written back after every command that changes it.
 * This class only assembles the parts that do the work and turns the handle:
 * read a line, parse it, run it, repeat.
 */
public class Thomas {
    /** Path the task list is saved at, relative to where the program is run */
    private static final String DATA_PATH = "./data/tasklist.txt";

    /** Reader and writer for the save file */
    private final Storage storage;

    /** Everything the user sees and types */
    private final Ui ui;

    /** Tasks being tracked, replaced by an empty list if loading fails */
    private TaskList tasks;

    /**
     * Creates a chatbot over one save file, loading whatever it already holds.
     * The greeting is printed before loading, so that any complaint about the
     * save file arrives after Thomas has introduced itself.
     *
     * @param filePath Path the task list is kept at.
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
     * Reads and carries out commands until the user says bye or the input ends.
     * The loop does not know what any command does. It reads a line, asks for
     * the command it names, runs it, and asks whether that was the last one.
     * The farewell sits after the loop because it is owed both ways.
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
     * @param args Command line arguments, unused.
     */
    public static void main(String[] args) {
        new Thomas(DATA_PATH).run();
    }
}
