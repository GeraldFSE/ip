import java.util.Scanner;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas reads commands from standard input and stores three
 * kinds of task: {@code todo}, {@code deadline ... /by ...} and
 * {@code event ... /from ... /to ...}. The stored tasks are printed on
 * {@code list}, and can be marked done with {@code mark} and not done again
 * with {@code unmark}. It stops when the user types {@code bye} or the input
 * ends.
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

    /** Maximum number of tasks, per this increment's fixed-size-array assumption. */
    private static final int MAX_TASKS = 100;

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

        Task[] tasks = new Task[MAX_TASKS];
        int taskCount = 0;

        // try-with-resources closes the Scanner once the loop ends.
        Scanner userInput = new Scanner(System.in);

        while (userInput.hasNextLine()) {
            try {
                String command = userInput.nextLine();

                // Split on the first space only, so parts[0] is the command
                // keyword and parts[1], when present, is its argument.
                String[] parts = command.split(" ", 2);
                String keyword = parts[0];

                if (keyword.equals("bye")) {
                    break;
                } else if (keyword.equals("list")) {
                    // Number the tasks for display; tasks itself stays unnumbered.
                    // One slot longer than taskCount to hold the header line,
                    // which then shifts every task one place along: entry i
                    // shows task i - 1, numbered i.
                    String[] entries = new String[taskCount + 1];
                    entries[0] = "Here are the tasks in your list:";
                    for (int i = 1; i <= taskCount; i++) {
                        entries[i] = i + ". " + tasks[i - 1];
                    }
                    printBlock(entries);
                } else if (keyword.equals("mark")) {
                    if (parts.length < 2 || parts[1].isBlank()) {
                        throw new ThomasException("HEYY!! You need a valid number to mark");
                    }
                    int markNumber;
                    try {
                        markNumber = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
                    }
                    // Checked against taskCount, not tasks.length: slots past
                    // taskCount are in bounds for the array but still null.
                    if (markNumber < 1 || markNumber > taskCount) {
                        throw new ThomasException("There is no task " + markNumber + "! You only have "
                                + taskCount + " task(s).");
                    }
                    Task taskToMark = tasks[markNumber - 1];
                    taskToMark.markAsDone();
                    printBlock("Nice! I've marked this task as done:", "   " + taskToMark);
                } else if (keyword.equals("unmark")) {
                    if (parts.length < 2 || parts[1].isBlank()) {
                        throw new ThomasException("HEYY!! You need a valid number to unmark");
                    }
                    int unmarkNumber;
                    try {
                        unmarkNumber = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
                    }
                    if (unmarkNumber < 1 || unmarkNumber > taskCount) {
                        throw new ThomasException("There is no task " + unmarkNumber + "! You only have "
                                + taskCount + " task(s).");
                    }
                    Task taskToUnmark = tasks[unmarkNumber - 1];
                    taskToUnmark.unmarkAsDone();
                    printBlock("OK, I've marked this task as not done yet:", "   " + taskToUnmark);
                } else if (keyword.equals("todo") || keyword.equals("deadline")
                        || keyword.equals("event")) {
                    // Only the add commands care that the list is full, so the
                    // check sits here: list, mark, unmark and bye still work.
                    if (taskCount >= MAX_TASKS) {
                        throw new ThomasException("Sorry, I can only remember " + MAX_TASKS + " tasks!");
                    }

                    if (keyword.equals("todo")) {
                        // isBlank() as well as the length check, so "todo    " is
                        // rejected too: that splits into two parts, not one.
                        if (parts.length < 2 || parts[1].isBlank()) {
                            throw new ThomasException("HEYY!! The description of a todo cannot be empty!");
                        }
                        tasks[taskCount] = new TodoTask(parts[1].trim());
                    } else if (keyword.equals("deadline")) {
                        if (parts.length < 2 || parts[1].isBlank()) {
                            throw new ThomasException("HEYY!! The description of a deadline cannot be empty!");
                        }
                        // "return book /by Sunday" -> ["return book", "Sunday"]
                        String[] details = parts[1].split(" /by ", 2);
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
                        tasks[taskCount] = new DeadlineTask(deadlineName, by);
                    } else {
                        if (parts.length < 2 || parts[1].isBlank()) {
                            throw new ThomasException("HEYY!! The description of an event cannot be empty!");
                        }
                        // Split the markers off one at a time rather than together.
                        // Splitting on " /from | /to " at once matches them in any
                        // order, so "/to 4pm /from 2pm" would silently swap the two.
                        // "meeting /from Mon 2pm /to 4pm" -> ["meeting", "Mon 2pm /to 4pm"]
                        String[] afterFrom = parts[1].split(" /from ", 2);
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
                        tasks[taskCount] = new EventTask(eventName, from, to);
                    }

                    // Shared by all three: nothing is counted or announced until
                    // the branch above has stored a task without throwing.
                    taskCount++;
                    printAddedBlock(tasks[taskCount - 1], taskCount);
                } else {
                    throw new ThomasException("Erm sorry, what does that mean again?");
                }
            } catch (ThomasException e) {
                printBlock(e.getMessage());
            }
        }


        printBlock("Until next time! Choo Choo!");
    }
}
