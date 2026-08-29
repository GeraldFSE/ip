import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Scanner;

import thomas.task.Task;

/**
 * Deals with everything the user sees and types.
 * <p>
 * All console reading and writing lives here, so the rest of the program never
 * calls {@link System#out} or builds a {@link Scanner} of its own. That keeps
 * the look of the chatbot -- the indentation, the dividers, the wording of each
 * message -- in one file, and means the command handling can be read without
 * stepping over formatting code.
 * <p>
 * A {@code Ui} owns the {@link Scanner} over standard input, so the object is
 * created once and reused for the whole session rather than per command; a new
 * {@code Scanner} each time would buffer ahead and lose input.
 */
public class Ui {
    /** Indentation applied to every line of chatbot text. */
    private static final String INDENT = "     ";

    /**
     * Horizontal rule printed above and below each block of chatbot output.
     * Indented one space less than the text it wraps, as the sample output shows.
     */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    /** Reads the user's command lines from standard input. */
    private final Scanner userInput = new Scanner(System.in);

    /**
     * Prints lines as one chatbot block: indented, wrapped in dividers.
     * <p>
     * Public because it is the general-purpose way to say something; the named
     * methods below are the messages that are worded the same way every time.
     *
     * @param lines the text lines to display, without indentation or newlines
     */
    public void showBlock(String... lines) {
        System.out.print(DIVIDER + "\n");
        for (String line : lines) {
            System.out.print(INDENT + line + "\n");
        }
        System.out.print(DIVIDER + "\n");
    }

    /**
     * Reports that there is another command to read.
     *
     * @return true if the user has typed another line and the input has not ended
     */
    public boolean hasNextCommand() {
        return userInput.hasNextLine();
    }

    /**
     * Reads one command line.
     * <p>
     * {@code nextLine()}, not {@code next()}: a command such as
     * {@code todo read book} is one line, and {@code next()} would hand back one
     * word at a time.
     *
     * @return the line the user typed, exactly as typed
     */
    public String readCommand() {
        return userInput.nextLine();
    }

    /** Prints the banner and greeting shown when the chatbot starts. */
    public void showWelcome() {
        // The first five lines are ASCII art spelling "Thomas". Every backslash
        // is written as \\ because a lone \ starts an escape sequence in Java.
        showBlock("  ________                              ",
                " /_  __/ /_  ____  ____ ___  ____ ______",
                "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/",
                " / / / / / / /_/ / / / / / / /_/ (__  ) ",
                "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  ",
                "Choo Choo! I'm Thomas!",
                "How can I serve you today?");
    }

    /** Prints the farewell shown when the chatbot stops. */
    public void showGoodbye() {
        showBlock("Until next time! Choo Choo!");
    }

    /**
     * Reports a problem with what the user asked for.
     *
     * @param message the explanation to show, already worded for the user
     */
    public void showError(String message) {
        showBlock(message);
    }

    /**
     * Reports that the saved tasks could not be read at all.
     *
     * @param message the reason the file could not be read
     */
    public void showLoadingError(String message) {
        showBlock("Uh oh! I could not read your saved tasks: " + message,
                "Starting with an empty list.");
    }

    /**
     * Reports one save file line that could not be understood, having skipped it.
     *
     * @param message what was wrong with the line
     */
    public void showSkippedLine(String message) {
        showBlock("Skipping a line I could not read: " + message);
    }

    /**
     * Reports that the task list could not be written to disk.
     *
     * @param message the reason the file could not be written
     */
    public void showSavingError(String message) {
        showBlock("Uh oh! I could not save your tasks: " + message);
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
    public void showAdded(Task task, int taskCount) {
        showBlock("Got it. I've added this task:",
                "   " + task,
                "Now you have " + taskCount + " task(s) in the list.");
    }

    /**
     * Confirms that a task was removed and reports the new size of the list.
     *
     * @param task      the task just removed
     * @param taskCount how many tasks are left
     */
    public void showRemoved(Task task, int taskCount) {
        showBlock("Noted. I've removed this task:",
                "   " + task,
                "Now you have " + taskCount + " task(s) in the list.");
    }

    /**
     * Confirms that a task is now done.
     *
     * @param task the task just marked
     */
    public void showMarked(Task task) {
        showBlock("Nice! I've marked this task as done:", "   " + task);
    }

    /**
     * Confirms that a task is no longer done.
     *
     * @param task the task just unmarked
     */
    public void showUnmarked(Task task) {
        showBlock("OK, I've marked this task as not done yet:", "   " + task);
    }

    /**
     * Prints the whole task list, numbered from 1.
     *
     * @param tasks the tasks to show, in list order
     */
    public void showTaskList(TaskList tasks) {
        // Number the tasks for display; tasks itself stays unnumbered.
        // One slot longer than the list to hold the header line, which then
        // shifts every task one place along: entry i shows task i - 1,
        // numbered i.
        String[] entries = new String[tasks.size() + 1];
        entries[0] = "Here are the tasks in your list:";
        for (int i = 1; i <= tasks.size(); i++) {
            entries[i] = i + ". " + tasks.get(i - 1);
        }
        showBlock(entries);
    }

    /**
     * Prints the tasks that fall on one day.
     * <p>
     * Which tasks match is {@link TaskList}'s question, and it answers with
     * their positions rather than the tasks alone. That is what lets each match
     * keep the number it has in the whole list, so a number shown here is the
     * number {@code mark} and {@code delete} take.
     *
     * @param tasks the whole task list
     * @param day   the day to report on
     */
    public void showTasksOnDay(TaskList tasks, LocalDate day) {
        // An ArrayList rather than a sized array as showTaskList uses: how many
        // tasks match is not known until they have been tested.
        ArrayList<String> entries = new ArrayList<>();
        entries.add("Here are the tasks on " + day.format(Task.DATE_DISPLAY_DAY) + ":");
        for (int position : tasks.positionsOn(day)) {
            entries.add((position + 1) + ". " + tasks.get(position));
        }
        showBlock(entries.toArray(new String[0]));
    }
}