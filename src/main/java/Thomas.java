import java.util.Scanner;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas reads commands from standard input, storing each
 * one as a task and printing the stored tasks on the {@code list} command.
 * Stored tasks can be marked done with {@code mark} and not done again with
 * {@code unmark}. It stops when the user types {@code bye} or the input ends.
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
        try (Scanner userInput = new Scanner(System.in)) {
            while (userInput.hasNextLine()) {
                String command = userInput.nextLine();

                // Split on the first space only, so parts[0] is the command
                // keyword and parts[1], when present, is its argument.
                String[] parts = command.split(" ", 2);
                String keyword = parts[0];

                if (command.equals("bye")) {
                    break;
                } else if (command.equals("list")) {
                    // Number the tasks for display; tasks itself stays unnumbered.
                    String[] entries = new String[taskCount];
                    for (int i = 0; i < taskCount; i++) {
                        entries[i] = (i + 1) + ". " + tasks[i];
                    }
                    printBlock(entries);
                } else if (keyword.equals("mark")) {
                    Task task = tasks[Integer.parseInt(parts[1]) - 1];
                    task.markAsDone();
                    printBlock("Nice! I've marked this task as done:", "   " + task);
                } else if (keyword.equals("unmark")) {
                    Task task = tasks[Integer.parseInt(parts[1]) - 1];
                    task.unmarkAsDone();
                    printBlock("OK, I've marked this task as not done yet:", "   " + task);
                } else if (taskCount >= MAX_TASKS) {
                    printBlock("Sorry, I can only remember " + MAX_TASKS + " tasks!");
                } else {
                    tasks[taskCount] = new Task(command);
                    taskCount++;
                    printBlock("added: " + command);
                }
            }
        }

        printBlock("Until next time! Choo Choo!");
    }
}
