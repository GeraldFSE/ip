package thomas;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import thomas.command.AddCommand;
import thomas.command.Command;
import thomas.command.CommandType;
import thomas.command.DeleteCommand;
import thomas.command.ExitCommand;
import thomas.command.ListCommand;
import thomas.command.MarkCommand;
import thomas.command.OnCommand;
import thomas.command.UnmarkCommand;
import thomas.task.DeadlineTask;
import thomas.task.EventTask;
import thomas.task.Task;
import thomas.task.TodoTask;

/**
 * Makes sense of one line the user typed and returns the command it asks for.
 * Everything that knows the shape of the input is settled here, so that no
 * other class ever sees the raw text.
 * Every failure leaves as a ThomasException carrying a message meant for the
 * user, so that the caller has one kind of error to report.
 */
public class Parser {
    /** Kind of command the line names */
    private final CommandType commandType;

    /** Line split once into keyword and argument, on the first space only */
    private final String[] parts;

    /**
     * Creates a parser for one typed line, reading it far enough to know which
     * command it is.
     * An unrecognized keyword is rejected here, so that the arguments are only
     * ever read for a command that really exists.
     *
     * @param line Line exactly as the user typed it.
     * @throws ThomasException If the first word is not a command.
     */
    private Parser(String line) throws ThomasException {
        this.parts = line.split(" ", 2);
        this.commandType = CommandType.fromKeyword(parts[0]);
    }

    /**
     * Returns the command a typed line asks for, arguments and all.
     * Each command is built with what it needs already read and checked, so that
     * carrying it out afterwards does no parsing. If the line cannot be
     * understood it is rejected here, rather than producing a command that would
     * fail halfway through doing something.
     *
     * @param fullCommand Line exactly as the user typed it.
     * @return Command that line asks for.
     * @throws ThomasException If the line is not a command Thomas understands, or
     *                         its arguments are missing or unreadable.
     */
    public static Command parse(String fullCommand) throws ThomasException {
        Parser parser = new Parser(fullCommand);
        return switch (parser.commandType) {
            case BYE -> new ExitCommand();
            case LIST -> new ListCommand();
            case ON -> new OnCommand(parser.parseDay());
            case MARK -> new MarkCommand(parser.parseTaskNumber("mark"));
            case UNMARK -> new UnmarkCommand(parser.parseTaskNumber("unmark"));
            case DELETE -> new DeleteCommand(parser.parseTaskNumber("delete"));
            // The three add commands differ only in the task they build, which
            // parseNewTask settles, so one AddCommand serves all three.
            case TODO, DEADLINE, EVENT -> new AddCommand(parser.parseNewTask());
        };
    }

    /**
     * Returns this command's argument, rejecting a command given without one.
     * A bare keyword such as "to-do" leaves no argument, and a keyword followed
     * by spaces alone leaves a blank one. Both are refused.
     *
     * @param message What to tell the user when the argument is missing.
     * @return Argument, with surrounding spaces removed.
     * @throws ThomasException If there is no argument, or it is only spaces.
     */
    private String requireArgument(String message) throws ThomasException {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new ThomasException(message);
        }
        return parts[1].trim();
    }

    /**
     * Returns a description, refusing one that holds the save file's field
     * separator.
     * A description containing the separator cannot survive being saved, since
     * it writes a line with an extra field that the loader refuses. Refusing it
     * as it is typed turns a task lost on the next run into an answer the user
     * can act on.
     * Refused here rather than in the task itself, because the save file cannot
     * hold such a description, so typing is the only way one can be made.
     *
     * @param description Description, already trimmed.
     * @return That same description.
     * @throws ThomasException If it contains the field separator.
     */
    private static String requireSeparatorFree(String description) throws ThomasException {
        if (description.contains(Task.FIELD_SEPARATOR)) {
            throw new ThomasException("HEYY!! A description can't contain '" + Task.FIELD_SEPARATOR
                    + "' -- that's how I keep your tasks in the save file.");
        }
        return description;
    }

    /**
     * Returns the task number given to mark, unmark or delete.
     * Only that the argument is a whole number is settled here. Whether a task
     * carries that number is the task list's to answer, since a parser never
     * sees the list.
     *
     * @param action Command being run, used to word the missing-argument
     *               message, for example "mark".
     * @return Number the user typed, counting from 1 and not yet checked against
     *         the list.
     * @throws ThomasException If the number is missing or is not a whole number.
     */
    private int parseTaskNumber(String action) throws ThomasException {
        String argument = requireArgument("HEYY!! You need a valid number to " + action);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
        }
    }

    /**
     * Returns the whole day given to the on command.
     * A day carries no time of day, so a time given here is refused rather than
     * ignored.
     *
     * @return Day the argument names.
     * @throws ThomasException If the day is missing or is not written as
     *                         yyyy-mm-dd.
     */
    private LocalDate parseDay() throws ThomasException {
        String text = requireArgument("HEYY!! Which day do you want to see?");
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ThomasException("I can't read '" + text + "' as a day! "
                    + "Write it as 2019-12-02.");
        }
    }

    /**
     * Returns the task that a to-do, deadline or event line describes.
     * The three add commands are read through one method because the caller
     * treats them alike. A task is returned only if every part of it parsed, so
     * a half-built task never reaches the list.
     *
     * @return Task the line describes.
     * @throws ThomasException If a description, marker or date is missing or
     *                         unreadable.
     */
    private Task parseNewTask() throws ThomasException {
        return switch (commandType) {
            case TODO -> parseTodo();
            case DEADLINE -> parseDeadline();
            case EVENT -> parseEvent();
            // Reached only by calling this for a command that adds no task, which
            // is a mistake in the caller rather than anything the user did.
            default -> throw new AssertionError("Not an add command: " + commandType);
        };
    }

    /**
     * Returns the task a to-do line describes.
     *
     * @return New to-do.
     * @throws ThomasException If the description is missing, or contains the
     *                         save file's field separator.
     */
    private Task parseTodo() throws ThomasException {
        return new TodoTask(requireSeparatorFree(
                requireArgument("HEYY!! The description of a todo cannot be empty!")));
    }

    /**
     * Returns the task a deadline line describes.
     *
     * @return New deadline.
     * @throws ThomasException If the description, the marker or the date is
     *                         missing, the date cannot be read, or the
     *                         description contains the save file's field
     *                         separator.
     */
    private Task parseDeadline() throws ThomasException {
        String arguments = requireArgument("HEYY!! The description of a deadline cannot be empty!");

        // "return book /by 2019-12-02 1800"
        //     -> ["return book", "2019-12-02 1800"]
        String[] details = arguments.split(" /by ", 2);
        if (details.length < 2) {
            // Two different mistakes arrive here, because the separator carries
            // a leading space so that a word such as "standby" is not mistaken
            // for a marker. requireArgument has already trimmed the argument,
            // so a line that is nothing but "/by ..." has no space in front of
            // the marker for the separator to match: the marker is there, and
            // it is the description in front of it that is missing.
            if (arguments.startsWith("/by")) {
                throw new ThomasException("HEYY!! The description of a deadline cannot be empty!");
            }
            throw new ThomasException("Are you forgetting something!! When is the deadline!");
        }

        String description = details[0].trim();
        String by = details[1].trim();
        if (description.isEmpty()) {
            throw new ThomasException("HEYY!! The description of a deadline cannot be empty!");
        }
        // The marker can be present with nothing after it: "... /by  ".
        if (by.isEmpty()) {
            throw new ThomasException("Are you forgetting something!! When is the deadline!");
        }

        LocalDateTime byDate = Task.parseDate(by, "a deadline date");
        return new DeadlineTask(requireSeparatorFree(description), byDate);
    }

    /**
     * Returns the task an event line describes.
     *
     * @return New event.
     * @throws ThomasException If the description, either marker or either date
     *                         is missing, a date cannot be read, the description
     *                         contains the save file's field separator, or the
     *                         event ends before it starts.
     */
    private Task parseEvent() throws ThomasException {
        String arguments = requireArgument("HEYY!! The description of an event cannot be empty!");

        // Split the markers off one at a time rather than together. Splitting on
        // " /from | /to " at once matches them in any order, so
        // "/to 4pm /from 2pm" would silently swap the two.
        // "meeting /from Mon 2pm /to 4pm" -> ["meeting", "Mon 2pm /to 4pm"]
        String[] afterFrom = arguments.split(" /from ", 2);
        if (afterFrom.length < 2) {
            // As in parseDeadline above: a line that begins with the marker has
            // a /from, and it is the description in front of it that is missing.
            if (arguments.startsWith("/from")) {
                throw new ThomasException("HEYY!! The description of an event cannot be empty!");
            }
            throw new ThomasException("Erm when does it start? You need a /from!");
        }

        // "Mon 2pm /to 4pm" -> ["Mon 2pm", "4pm"]
        String[] afterTo = afterFrom[1].split(" /to ", 2);
        if (afterTo.length < 2) {
            throw new ThomasException("Erm when does it end? You need a /to after your /from!");
        }

        String description = afterFrom[0].trim();
        String from = afterTo[0].trim();
        String to = afterTo[1].trim();
        if (description.isEmpty()) {
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
        return new EventTask(requireSeparatorFree(description), fromDate, toDate);
    }
}
