import java.io.IOException;

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
 * Almost none of that is done here. Talking to the user belongs to {@link Ui},
 * the save file to {@link Storage}, holding the tasks to {@link TaskList}, and
 * understanding a typed line to {@link Parser}. This class is what holds those
 * four together: it decides which of them a command calls for, and in what
 * order. That leaves it able to say what the chatbot does without saying how any
 * of it is done.
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
     * Saves the task list, reporting a failure instead of crashing.
     * <p>
     * Called after every command that changes the list, which is what makes the
     * save automatic. {@link Storage#save} throws rather than printing, because
     * it has no business talking to the user; catching the {@link IOException}
     * here in one place keeps {@link #run()} free of try/catch at all four call
     * sites, and means a save failure costs the user a warning rather than the
     * session.
     */
    private void save() {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }

    /**
     * Reads and carries out commands until the user says {@code bye} or the
     * input ends.
     * <p>
     * Each case reads as the steps the command takes -- work out the argument,
     * change the list, save, say what happened -- because the parsing, the
     * checking and the wording have all moved to the classes that own them.
     */
    public void run() {
        // Labelled so the BYE case can end the loop. A plain break inside a
        // switch leaves the switch, not the loop, which would silently read on
        // past a bye instead of stopping.
        readLoop:
        while (ui.hasNextCommand()) {
            try {
                // Rejects an unknown keyword as it is built, so every case
                // below is a command that really exists.
                Parser parser = new Parser(ui.readCommand());

                switch (parser.getCommand()) {
                case BYE -> {
                    break readLoop;
                }
                case LIST -> ui.showTaskList(tasks);
                case ON -> ui.showTasksOnDay(tasks, parser.parseDay());
                case MARK -> {
                    Task taskToMark = tasks.getByNumber(parser.parseTaskNumber("mark"));
                    taskToMark.markAsDone();
                    save();
                    ui.showMarked(taskToMark);
                }
                case UNMARK -> {
                    Task taskToUnmark = tasks.getByNumber(parser.parseTaskNumber("unmark"));
                    taskToUnmark.unmarkAsDone();
                    save();
                    ui.showUnmarked(taskToUnmark);
                }
                case DELETE -> {
                    Task removedTask = tasks.deleteByNumber(parser.parseTaskNumber("delete"));
                    save();
                    ui.showRemoved(removedTask, tasks.size());
                }
                // One case for the three add commands: whichever was typed, the
                // task is appended, saved and announced the same way, so that
                // stays written once.
                case TODO, DEADLINE, EVENT -> {
                    // Built first, so a task is only added to the list once its
                    // arguments have parsed without throwing.
                    Task newTask = parser.parseNewTask();
                    tasks.add(newTask);
                    save();
                    ui.showAdded(newTask, tasks.size());
                }
                // A switch statement over an enum is not checked for
                // exhaustiveness, so a command added to Command but not handled
                // here would silently do nothing. Fail loudly instead. This
                // cannot be reached from user input: the parser has already
                // rejected any word that is not one of the constants.
                default -> throw new AssertionError("Command not handled: " + parser.getCommand());
                }
            } catch (ThomasException e) {
                // Every user mistake, wherever it was noticed, arrives here as
                // one kind of exception and is reported the same way. That is
                // what keeps the cases above free of error handling.
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