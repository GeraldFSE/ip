import java.util.ArrayList;
import java.util.Scanner;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas reads commands from standard input and stores three
 * kinds of task: {@code todo}, {@code deadline ... /by ...} and
 * {@code event ... /from ... /to ...}. The stored tasks are printed on
 * {@code list}, can be marked done with {@code mark} and not done again with
 * {@code unmark}, and removed with {@code delete}. It stops when the user types
 * {@code bye} or the input ends.
 */
public class Thomas {
    /** Indentation applied to every line of chatbot text. */
    private static final String INDENT = "     ";

    /**
     * Horizontal rule printed above and below each block of chatbot output.
     * Indented one space less than the text it wraps, as the sample output shows.
     */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    /**
     * Prints lines as one chatbot block: indented, wrapped in dividers.
     *
     * @param lines the text lines to display, without indentation or newlines
     */
    private static void printBlock(String... lines) {
        System.out.print(DIVIDER + "\n");
        for (String line : lines) {
            System.out.print(INDENT + line + "\n");
        }
        System.out.print(DIVIDER + "\n");
    }

    /**
     * Confirms that a task was added and reports the new size of the list.
     * <p>
     * The three add commands share this acknowledgement, so it lives in one
     * place; {@code task} is a {@link Task}, and polymorphism picks the right
     * {@code toString()} for whichever subclass was actually added.
     *
     * @param task      the task just stored
     * @param taskCount how many tasks are now stored
     */
    private static void printAddedBlock(Task task, int taskCount) {
        printBlock("Got it. I've added this task:",
                "   " + task,
                "Now you have " + taskCount + " task(s) in the list.");
    }

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
     * Turns the argument of {@code mark}, {@code unmark} or {@code delete} into
     * an index into the task list.
     * <p>
     * The user counts tasks from 1 and the list counts from 0, so the number is
     * checked against the tasks that exist and then shifted down by one here --
     * the single place that conversion happens.
     * <p>
     * The range is checked against {@code taskCount} rather than any capacity:
     * a number past the end must be reported, not passed to
     * {@link java.util.ArrayList#get(int)}.
     *
     * @param parts     the command line split into keyword and argument
     * @param taskCount how many tasks are currently stored
     * @param action    the command being run, used to word the missing-argument
     *                  message, for example {@code "mark"}
     * @return the index of the task the user named
     * @throws ThomasException if the number is missing, not a number, or out of range
     */
    private static int parseTaskIndex(String[] parts, int taskCount, String action)
            throws ThomasException {
        String argument = requireArgument(parts, "HEYY!! You need a valid number to " + action);

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            // Rethrown as a ThomasException so the read loop has one kind of
            // user error to report, and no Java class name reaches the user.
            throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
        }

        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new ThomasException("There is no task " + taskNumber + "! You only have "
                    + taskCount + " task(s).");
        }
        return taskNumber - 1;
    }

    /**
     * Runs the chatbot until the user says {@code bye} or the input ends.
     *
     * @param args command line arguments; unused
     */
    public static void main(String[] args) {
        // The first five lines are ASCII art spelling "Thomas". Every backslash
        // is written as \\ because a lone \ starts an escape sequence in Java.
        printBlock("  ________                              ",
                " /_  __/ /_  ____  ____ ___  ____ ______",
                "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/",
                " / / / / / / /_/ / / / / / / /_/ (__  ) ",
                "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  ",
                "Choo Choo! I'm Thomas!",
                "How can I serve you today?");

        // An ArrayList rather than a Task[]: it grows as tasks are added, so
        // there is no fixed ceiling to enforce, and remove() closes the gap
        // left by a deleted task instead of leaving a hole to shuffle by hand.
        ArrayList<Task> tasks = new ArrayList<>();

        // try-with-resources closes the Scanner once the loop ends.
        Scanner userInput = new Scanner(System.in);

        // Labelled so the BYE case can end the loop. A plain break inside a
        // switch leaves the switch, not the loop, which would silently read on
        // past a bye instead of stopping.
        readLoop:
        while (userInput.hasNextLine()) {
            try {
                String line = userInput.nextLine();

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
                case LIST -> {
                    // Number the tasks for display; tasks itself stays unnumbered.
                    // One slot longer than the list to hold the header line,
                    // which then shifts every task one place along: entry i
                    // shows task i - 1, numbered i.
                    String[] entries = new String[tasks.size() + 1];
                    entries[0] = "Here are the tasks in your list:";
                    for (int i = 1; i <= tasks.size(); i++) {
                        entries[i] = i + ". " + tasks.get(i - 1);
                    }
                    printBlock(entries);
                }
                case MARK -> {
                    Task taskToMark = tasks.get(parseTaskIndex(parts, tasks.size(), "mark"));
                    taskToMark.markAsDone();
                    printBlock("Nice! I've marked this task as done:", "   " + taskToMark);
                }
                case UNMARK -> {
                    Task taskToUnmark = tasks.get(parseTaskIndex(parts, tasks.size(), "unmark"));
                    taskToUnmark.unmarkAsDone();
                    printBlock("OK, I've marked this task as not done yet:", "   " + taskToUnmark);
                }
                case DELETE -> {
                    // remove() returns the task it took out, so it can be shown
                    // back to the user, and closes the gap: everything after it
                    // shifts down one and the numbering stays contiguous.
                    Task removedTask = tasks.remove(parseTaskIndex(parts, tasks.size(), "delete"));
                    printBlock("Noted. I've removed this task:",
                            "   " + removedTask,
                            "Now you have " + tasks.size() + " task(s) in the list.");
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
                        // "return book /by Sunday" -> ["return book", "Sunday"]
                        String[] details = arguments.split(" /by ", 2);
                        if (details.length < 2) {
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
                        newTask = new DeadlineTask(deadlineName, by);
                    } else {
                        String arguments = requireArgument(parts,
                                "HEYY!! The description of an event cannot be empty!");
                        // Split the markers off one at a time rather than together.
                        // Splitting on " /from | /to " at once matches them in any
                        // order, so "/to 4pm /from 2pm" would silently swap the two.
                        // "meeting /from Mon 2pm /to 4pm" -> ["meeting", "Mon 2pm /to 4pm"]
                        String[] afterFrom = arguments.split(" /from ", 2);
                        if (afterFrom.length < 2) {
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
                        newTask = new EventTask(eventName, from, to);
                    }

                    // Shared by all three: the task is only appended, counted
                    // and announced once the branch above returned without throwing.
                    tasks.add(newTask);
                    printAddedBlock(newTask, tasks.size());
                }
                // A switch statement over an enum is not checked for
                // exhaustiveness, so a command added to Command but not handled
                // here would silently do nothing. Fail loudly instead. This
                // cannot be reached from user input: fromKeyword has already
                // rejected any word that is not one of the constants.
                default -> throw new AssertionError("Command not handled: " + command);
                }
            } catch (ThomasException e) {
                printBlock(e.getMessage());
            }
        }


        printBlock("Until next time! Choo Choo!");
    }
}
