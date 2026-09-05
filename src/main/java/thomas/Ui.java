package thomas;

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
 * Wording a message and showing it are separate here. The {@code get...Message}
 * methods only word one, and a command returns what it gets rather than
 * printing it, because the same words have to reach a console and a dialog
 * bubble and only one of those is a console. The {@code show...} methods print,
 * and are what the console session uses to put a worded message on screen.
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

    /**
     * The two spoken lines of the greeting.
     * <p>
     * Named constants because both front ends open with them and neither owns
     * them: the console prints them under the banner, the window shows them in
     * the first dialog box, and they must not drift apart.
     */
    private static final String GREETING_NAME = "Choo Choo! I'm Thomas!";
    private static final String GREETING_OFFER = "How can I serve you today?";

    /** Reads the user's command lines from standard input. */
    private final Scanner userInput = new Scanner(System.in);

    /**
     * Prints lines as one chatbot block: indented, wrapped in dividers.
     * <p>
     * Public because it is the general-purpose way to say something; the named
     * methods below are the messages that are worded the same way every time.
     *
     * @param lines The text lines to display, without indentation or newlines.
     */
    public void showBlock(String... lines) {
        System.out.print(DIVIDER + "\n");
        for (String line : lines) {
            System.out.print(INDENT + line + "\n");
        }
        System.out.print(DIVIDER + "\n");
    }

    /**
     * Prints a message that was worded elsewhere, as one chatbot block.
     * <p>
     * The counterpart to the {@code get...Message} methods: they say what, this
     * says where. A blank message prints nothing at all, since a command such
     * as {@code bye} has nothing to say and dividers around nothing would look
     * like a fault.
     *
     * @param message The message to print, its lines separated by newlines.
     */
    public void showMessage(String message) {
        if (message.isBlank()) {
            return;
        }
        showBlock(message.split("\n", -1));
    }

    /**
     * Joins lines into one message, one per line.
     * <p>
     * A message carries only its own lines: the indentation and dividers are
     * console furniture, added by {@link #showBlock} at the moment of printing,
     * and a dialog bubble wants neither.
     *
     * @param lines The lines to join.
     * @return The lines separated by newlines.
     */
    private static String joinLines(String... lines) {
        return String.join("\n", lines);
    }

    /**
     * Reports that there is another command to read.
     *
     * @return True if the user has typed another line and the input has not ended.
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
     * @return The line the user typed, exactly as typed.
     */
    public String readCommand() {
        return userInput.nextLine();
    }

    /**
     * Words the greeting the chatbot opens with.
     * <p>
     * The spoken lines only. The banner {@link #showWelcome()} prints above
     * them is console furniture, exactly as the dividers and the indentation
     * are: it is ASCII art, and a dialog box draws its text in a proportional
     * font that would leave the train in pieces.
     *
     * @return The greeting to show the user.
     */
    public String getWelcomeMessage() {
        return joinLines(GREETING_NAME, GREETING_OFFER);
    }

    /** Prints the banner and greeting shown when the chatbot starts. */
    public void showWelcome() {
        // The first five lines are ASCII art spelling "Thomas". Every backslash
        // is written as \\ because a lone \ starts an escape sequence in Java.
        // Each line is passed separately, since showBlock indents one at a time.
        showBlock("  ________                              ",
                " /_  __/ /_  ____  ____ ___  ____ ______",
                "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/",
                " / / / / / / /_/ / / / / / / /_/ (__  ) ",
                "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  ",
                GREETING_NAME,
                GREETING_OFFER);
    }

    /**
     * Words the farewell shown when the chatbot stops.
     *
     * @return The farewell to show the user.
     */
    public String getGoodbyeMessage() {
        return "Until next time! Choo Choo!";
    }

    /**
     * Prints the farewell shown when the chatbot stops.
     * <p>
     * For the console session that ends because the input ran out rather than
     * because {@code bye} was typed; a typed {@code bye} gets the same words
     * from {@link thomas.command.ExitCommand} instead.
     */
    public void showGoodbye() {
        showBlock(getGoodbyeMessage());
    }

    /**
     * Reports a problem with what the user asked for.
     *
     * @param message The explanation to show, already worded for the user.
     */
    public void showError(String message) {
        showBlock(message);
    }

    /**
     * Words the warning that the saved tasks could not be read at all.
     *
     * @param message The reason the file could not be read.
     * @return The warning to show the user.
     */
    public String getLoadingErrorMessage(String message) {
        return joinLines("Uh oh! I could not read your saved tasks: " + message,
                "Starting with an empty list.");
    }

    /**
     * Reports that the saved tasks could not be read at all.
     *
     * @param message The reason the file could not be read.
     */
    public void showLoadingError(String message) {
        showMessage(getLoadingErrorMessage(message));
    }

    /**
     * Words the warning about one save file line that could not be understood.
     *
     * @param message What was wrong with the line.
     * @return The warning to show the user.
     */
    public String getSkippedLineMessage(String message) {
        return "Skipping a line I could not read: " + message;
    }

    /**
     * Reports one save file line that could not be understood, having skipped it.
     *
     * @param message What was wrong with the line.
     */
    public void showSkippedLine(String message) {
        showMessage(getSkippedLineMessage(message));
    }

    /**
     * Words the warning that the task list could not be written to disk.
     *
     * @param message The reason the file could not be written.
     * @return The warning to show the user.
     */
    public String getSavingErrorMessage(String message) {
        return "Uh oh! I could not save your tasks: " + message;
    }

    /**
     * Confirms that a task was added and reports the new size of the list.
     * <p>
     * The three add commands share this acknowledgement, so it lives in one
     * place; {@code task} is a {@link Task}, and polymorphism picks the right
     * {@code toString()} for whichever subclass was actually added.
     *
     * @param task The task just stored.
     * @param taskCount How many tasks are now stored.
     * @return The confirmation to show the user.
     */
    public String getAddedMessage(Task task, int taskCount) {
        return joinLines("Got it. I've added this task:",
                "   " + task,
                "Now you have " + taskCount + " task(s) in the list.");
    }

    /**
     * Confirms that a task was removed and reports the new size of the list.
     *
     * @param task The task just removed.
     * @param taskCount How many tasks are left.
     * @return The confirmation to show the user.
     */
    public String getRemovedMessage(Task task, int taskCount) {
        return joinLines("Noted. I've removed this task:",
                "   " + task,
                "Now you have " + taskCount + " task(s) in the list.");
    }

    /**
     * Confirms that a task is now done.
     *
     * @param task The task just marked.
     * @return The confirmation to show the user.
     */
    public String getMarkedMessage(Task task) {
        return joinLines("Nice! I've marked this task as done:", "   " + task);
    }

    /**
     * Confirms that a task is no longer done.
     *
     * @param task The task just unmarked.
     * @return The confirmation to show the user.
     */
    public String getUnmarkedMessage(Task task) {
        return joinLines("OK, I've marked this task as not done yet:", "   " + task);
    }

    /**
     * Prints the whole task list, numbered from 1.
     *
     * @param tasks The tasks to show, in list order.
     * @return The numbered list to show the user.
     */
    public String getTaskListMessage(TaskList tasks) {
        // Number the tasks for display; tasks itself stays unnumbered.
        // One slot longer than the list to hold the header line, which then
        // shifts every task one place along: entry i shows task i - 1,
        // numbered i.
        String[] entries = new String[tasks.size() + 1];
        entries[0] = "Here are the tasks in your list:";
        for (int i = 1; i <= tasks.size(); i++) {
            entries[i] = i + ". " + tasks.get(i - 1);
        }
        return joinLines(entries);
    }

    /**
     * Words the tasks whose description contains a keyword.
     * <p>
     * Numbered by list position, exactly as {@link #getTasksOnDayMessage} is,
     * so a number shown here is the number {@code mark} and {@code delete}
     * take. The header is included whether or not anything matched, so an empty
     * search says so rather than saying nothing at all.
     *
     * @param tasks The whole task list.
     * @param keyword The text that was searched for.
     * @return The matches to show the user.
     */
    public String getMatchingTasksMessage(TaskList tasks, String keyword) {
        // As in getTasksOnDayMessage: how many tasks match is not known until
        // they have been tested, so the entries are collected rather than sized.
        ArrayList<String> entries = new ArrayList<>();
        entries.add("Here are the matching tasks in your list:");
        for (int position : tasks.positionsMatching(keyword)) {
            entries.add((position + 1) + ". " + tasks.get(position));
        }
        return joinLines(entries.toArray(new String[0]));
    }

    /**
     * Words the tasks that fall on one day.
     * <p>
     * Which tasks match is {@link TaskList}'s question, and it answers with
     * their positions rather than the tasks alone. That is what lets each match
     * keep the number it has in the whole list, so a number shown here is the
     * number {@code mark} and {@code delete} take.
     *
     * @param tasks The whole task list.
     * @param day The day to report on.
     * @return The matches to show the user.
     */
    public String getTasksOnDayMessage(TaskList tasks, LocalDate day) {
        // An ArrayList rather than a sized array as getTaskListMessage uses:
        // how many tasks match is not known until they have been tested.
        ArrayList<String> entries = new ArrayList<>();
        entries.add("Here are the tasks on " + day.format(Task.DATE_DISPLAY_DAY) + ":");
        for (int position : tasks.positionsOn(day)) {
            entries.add((position + 1) + ". " + tasks.get(position));
        }
        return joinLines(entries.toArray(new String[0]));
    }
}
