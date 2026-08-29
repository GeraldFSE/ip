import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas reads commands from standard input and stores three
 * kinds of task: {@code todo}, {@code deadline ... /by ...} and
 * {@code event ... /from ... /to ...}. The stored tasks are printed on
 * {@code list}, can be marked done with {@code mark} and not done again with
 * {@code unmark}, and removed with {@code delete}. It stops when the user types
 * {@code bye} or the input ends.
 * <p>
 * The list survives between runs: it is loaded from {@value #DATA_PATH} at
 * start-up and written back after every command that changes it, so no task is
 * lost even if the program is closed without typing {@code bye}.
 * <p>
 * Little of that is done here. Talking to the user belongs to {@link Ui}, the
 * save file to {@link Storage}, and holding the tasks to {@link TaskList}, so
 * this class is left with one job: working out which operation a typed line
 * asks for, and calling it on those three.
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

    /**
     * Returns a command's argument, rejecting a command given without one.
     * <p>
     * {@code parts} is the command line split in two, so it holds a single
     * element when the user typed a bare keyword such as {@code todo}. Checking
     * {@code isBlank()} as well covers the other case the length misses:
     * {@code "todo    "} splits into two parts, the second all spaces.
     * <p>
     * Returning the argument rather than only checking it keeps the indexing
     * into {@code parts} in one place, instead of every caller reaching back
     * for {@code parts[1]} after asking whether it exists.
     *
     * @param parts   the command line split into keyword and argument
     * @param message what to tell the user when the argument is missing
     * @return the argument, with surrounding spaces removed
     * @throws ThomasException if there is no argument, or it is only spaces
     */
    private static String requireArgument(String[] parts, String message) throws ThomasException {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new ThomasException(message);
        }
        return parts[1].trim();
    }

    /**
     * Reads the task number the user gave to {@code mark}, {@code unmark} or
     * {@code delete}.
     * <p>
     * Only that the argument is a whole number is settled here. Whether a task
     * actually carries that number is {@link TaskList}'s to answer, since only
     * the list knows how many tasks there are; this method never sees the list.
     *
     * @param parts  the command line split into keyword and argument
     * @param action the command being run, used to word the missing-argument
     *               message, for example {@code "mark"}
     * @return the number the user typed, counting from 1 and not yet checked
     *         against the list
     * @throws ThomasException if the number is missing or is not a whole number
     */
    private static int parseTaskNumber(String[] parts, String action) throws ThomasException {
        String argument = requireArgument(parts, "HEYY!! You need a valid number to " + action);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // Rethrown as a ThomasException so the read loop has one kind of
            // user error to report, and no Java class name reaches the user.
            throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
        }
    }

    /**
     * Turns a whole day the user typed into a {@link LocalDate}.
     * <p>
     * Separate from {@link Task#parseDate} because the two read different things.
     * A task happens at a moment, so it needs a date and a time; {@code on}
     * asks about a whole day, so a time would be meaningless there and is
     * refused rather than ignored. Returning a {@link LocalDate} rather than a
     * {@link LocalDateTime} is what says so in the type.
     *
     * @param text the day as written, expected as {@code yyyy-mm-dd}
     * @return the day the text names
     * @throws ThomasException if the text is not a date in that form
     */
    private static LocalDate parseDay(String text) throws ThomasException {
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ThomasException("I can't read '" + text + "' as a day! "
                    + "Write it as 2019-12-02.");
        }
    }

    /**
     * Saves the task list, reporting a failure instead of crashing.
     * <p>
     * Called after every command that changes the list, which is what makes the
     * save automatic. {@link Storage#save} throws rather than printing, because
     * it has no business talking to the user; catching the {@link IOException}
     * here in one place keeps the read loop free of try/catch at all six call
     * sites, and means a save failure costs the user a warning rather than the
     * session.
     *
     * @param tasks   the tasks to write
     * @param storage where to write them
     * @param ui      used to report a failed save
     */
    private static void save(TaskList tasks, Storage storage, Ui ui) {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }

    /**
     * Runs the chatbot until the user says {@code bye} or the input ends.
     *
     * @param args command line arguments; unused
     */
    public static void main(String[] args) {
        // One Ui for the whole session: it owns the Scanner over standard
        // input, and a second one would buffer ahead and swallow commands.
        Ui ui = new Ui();
        ui.showWelcome();

        Storage storage = new Storage(DATA_PATH);

        // Storage hands back plain tasks; wrapping them in a TaskList is what
        // adds the checked operations the commands below rely on.
        TaskList tasks;
        try {
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

        // Labelled so the BYE case can end the loop. A plain break inside a
        // switch leaves the switch, not the loop, which would silently read on
        // past a bye instead of stopping.
        readLoop:
        while (ui.hasNextCommand()) {
            try {
                String line = ui.readCommand();

                // Split on the first space only, so parts[0] is the command
                // keyword and parts[1], when present, is its argument.
                String[] parts = line.split(" ", 2);

                // Rejects an unknown keyword here, so every case below is a
                // command that really exists.
                Command command = Command.fromKeyword(parts[0]);

                switch (command) {
                case BYE -> {
                    break readLoop;
                }
                case LIST -> ui.showTaskList(tasks);
                case ON -> {
                    LocalDate day = parseDay(requireArgument(parts,
                            "HEYY!! Which day do you want to see?"));
                    ui.showTasksOnDay(tasks, day);
                }
                case MARK -> {
                    Task taskToMark = tasks.getByNumber(parseTaskNumber(parts, "mark"));
                    taskToMark.markAsDone();
                    save(tasks, storage, ui);
                    ui.showMarked(taskToMark);
                }
                case UNMARK -> {
                    Task taskToUnmark = tasks.getByNumber(parseTaskNumber(parts, "unmark"));
                    taskToUnmark.unmarkAsDone();
                    save(tasks, storage, ui);
                    ui.showUnmarked(taskToUnmark);
                }
                case DELETE -> {
                    Task removedTask = tasks.deleteByNumber(parseTaskNumber(parts, "delete"));
                    save(tasks, storage, ui);
                    ui.showRemoved(removedTask, tasks.size());
                }
                // One case for the three add commands, so appending and
                // announcing the new task stays written once.
                case TODO, DEADLINE, EVENT -> {
                    // Built into a local first, so a task is only added to the
                    // list once its arguments have parsed without throwing.
                    Task newTask;
                    if (command == Command.TODO) {
                        newTask = new TodoTask(requireArgument(parts,
                                "HEYY!! The description of a todo cannot be empty!"));
                    } else if (command == Command.DEADLINE) {
                        String arguments = requireArgument(parts,
                                "HEYY!! The description of a deadline cannot be empty!");
                        // "return book /by 2019-12-02 1800"
                        //     -> ["return book", "2019-12-02 1800"]
                        String[] details = arguments.split(" /by ", 2);
                        if (details.length < 2) {
                            // Two different mistakes arrive here, because the
                            // separator carries a leading space so that a word
                            // such as "standby" is not mistaken for a marker.
                            // requireArgument has already trimmed the argument,
                            // so a line that is nothing but "/by ..." has no
                            // space in front of the marker for the separator to
                            // match: the marker is there, and it is the
                            // description in front of it that is missing.
                            if (arguments.startsWith("/by")) {
                                throw new ThomasException(
                                        "HEYY!! The description of a deadline cannot be empty!");
                            }
                            throw new ThomasException("Are you forgetting something!! When is the deadline!");
                        }
                        String deadlineName = details[0].trim();
                        String by = details[1].trim();
                        if (deadlineName.isEmpty()) {
                            throw new ThomasException("HEYY!! The description of a deadline cannot be empty!");
                        }
                        // The marker can be present with nothing after it: "... /by  ".
                        if (by.isEmpty()) {
                            throw new ThomasException("Are you forgetting something!! When is the deadline!");
                        }
                        LocalDateTime byDate = Task.parseDate(by, "a deadline date");
                        newTask = new DeadlineTask(deadlineName, byDate);
                    } else {
                        String arguments = requireArgument(parts,
                                "HEYY!! The description of an event cannot be empty!");
                        // Split the markers off one at a time rather than together.
                        // Splitting on " /from | /to " at once matches them in any
                        // order, so "/to 4pm /from 2pm" would silently swap the two.
                        // "meeting /from Mon 2pm /to 4pm" -> ["meeting", "Mon 2pm /to 4pm"]
                        String[] afterFrom = arguments.split(" /from ", 2);
                        if (afterFrom.length < 2) {
                            // As in the deadline branch above: a line that
                            // begins with the marker has a /from, and it is the
                            // description in front of it that is missing.
                            if (arguments.startsWith("/from")) {
                                throw new ThomasException(
                                        "HEYY!! The description of an event cannot be empty!");
                            }
                            throw new ThomasException("Erm when does it start? You need a /from!");
                        }
                        // "Mon 2pm /to 4pm" -> ["Mon 2pm", "4pm"]
                        String[] afterTo = afterFrom[1].split(" /to ", 2);
                        if (afterTo.length < 2) {
                            throw new ThomasException("Erm when does it end? You need a /to after your /from!");
                        }
                        String eventName = afterFrom[0].trim();
                        String from = afterTo[0].trim();
                        String to = afterTo[1].trim();
                        if (eventName.isEmpty()) {
                            throw new ThomasException("HEYY!! The description of an event cannot be empty!");
                        }
                        if (from.isEmpty()) {
                            throw new ThomasException("Erm when does it start? You need a /from!");
                        }
                        if (to.isEmpty()) {
                            throw new ThomasException("Erm when does it end? You need a /to after your /from!");
                        }
                        LocalDateTime fromDate = Task.parseDate(from, "a start date");
                        LocalDateTime toDate = Task.parseDate(to, "an end date");
                        newTask = new EventTask(eventName, fromDate, toDate);
                    }

                    // Shared by all three: the task is only appended, counted
                    // and announced once the branch above returned without throwing.
                    tasks.add(newTask);
                    save(tasks, storage, ui);
                    ui.showAdded(newTask, tasks.size());
                }
                // A switch statement over an enum is not checked for
                // exhaustiveness, so a command added to Command but not handled
                // here would silently do nothing. Fail loudly instead. This
                // cannot be reached from user input: fromKeyword has already
                // rejected any word that is not one of the constants.
                default -> throw new AssertionError("Command not handled: " + command);
                }
            } catch (ThomasException e) {
                ui.showError(e.getMessage());
            }
        }

        // No save here: every command that changes the list has already saved,
        // so the file is current even if the program never reaches this point.
        ui.showGoodbye();
    }
}
