import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Makes sense of one line the user typed.
 * <p>
 * A {@code Parser} is built from a single command line and splits it once, so
 * everything that knows the shape of the input -- where the keyword ends, that
 * a deadline is written {@code /by} and an event {@code /from ... /to ...},
 * which mistake each complaint names -- is settled here. Callers ask for the
 * command and then for the arguments it takes, and never see the raw text.
 * <p>
 * Nothing is parsed unless it is asked for: a {@code list} carries no argument,
 * and reading one out of it would be inventing work. That is why the arguments
 * are separate methods rather than fields filled in by the constructor.
 * <p>
 * Every failure leaves as a {@link ThomasException} carrying a message meant for
 * the user, so the caller has one kind of error to report and no Java class name
 * ever reaches the screen.
 */
public class Parser {
    /** The command the line names. */
    private final Command command;

    /**
     * The line split into keyword and argument.
     * <p>
     * Split on the first space only, so {@code parts[0]} is the command keyword
     * and {@code parts[1]}, when present, is everything after it -- descriptions
     * contain spaces, so the rest of the line must stay in one piece.
     */
    private final String[] parts;

    /**
     * Reads a line far enough to know which command it is.
     * <p>
     * An unrecognised keyword is rejected here, so a caller holding a
     * {@code Parser} is already holding a real command. The arguments are left
     * alone until asked for, since which of them the line should carry depends
     * on that command.
     *
     * @param line the line exactly as the user typed it
     * @throws ThomasException if the first word is not a command
     */
    public Parser(String line) throws ThomasException {
        this.parts = line.split(" ", 2);
        this.command = Command.fromKeyword(parts[0]);
    }

    /**
     * Returns the command this line names.
     *
     * @return the command, known to be one Thomas handles
     */
    public Command getCommand() {
        return command;
    }

    /**
     * Returns this command's argument, rejecting a command given without one.
     * <p>
     * {@code parts} holds a single element when the user typed a bare keyword
     * such as {@code todo}. Checking {@code isBlank()} as well covers the other
     * case the length misses: {@code "todo    "} splits into two parts, the
     * second all spaces.
     * <p>
     * Returning the argument rather than only checking it keeps the indexing
     * into {@code parts} in one place, instead of every caller reaching back for
     * {@code parts[1]} after asking whether it exists.
     *
     * @param message what to tell the user when the argument is missing
     * @return the argument, with surrounding spaces removed
     * @throws ThomasException if there is no argument, or it is only spaces
     */
    private String requireArgument(String message) throws ThomasException {
        if (parts.length < 2 || parts[1].isBlank()) {
            throw new ThomasException(message);
        }
        return parts[1].trim();
    }

    /**
     * Reads the task number given to {@code mark}, {@code unmark} or
     * {@code delete}.
     * <p>
     * Only that the argument is a whole number is settled here. Whether a task
     * actually carries that number is {@link TaskList}'s to answer, since only
     * the list knows how many tasks there are; a parser never sees the list.
     *
     * @param action the command being run, used to word the missing-argument
     *               message, for example {@code "mark"}
     * @return the number the user typed, counting from 1 and not yet checked
     *         against the list
     * @throws ThomasException if the number is missing or is not a whole number
     */
    public int parseTaskNumber(String action) throws ThomasException {
        String argument = requireArgument("HEYY!! You need a valid number to " + action);

        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new ThomasException("WHAT? Why are you passing a non integer?! Give me an INTEGER!!");
        }
    }

    /**
     * Reads the whole day given to {@code on}.
     * <p>
     * Separate from {@link Task#parseDate} because the two read different
     * things. A task happens at a moment, so it needs a date and a time;
     * {@code on} asks about a whole day, so a time would be meaningless there
     * and is refused rather than ignored. Returning a {@link LocalDate} rather
     * than a {@link LocalDateTime} is what says so in the type.
     *
     * @return the day the argument names
     * @throws ThomasException if the day is missing or is not written as
     *                         {@code yyyy-mm-dd}
     */
    public LocalDate parseDay() throws ThomasException {
        String text = requireArgument("HEYY!! Which day do you want to see?");
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ThomasException("I can't read '" + text + "' as a day! "
                    + "Write it as 2019-12-02.");
        }
    }

    /**
     * Builds the task that {@code todo}, {@code deadline} or {@code event}
     * describes.
     * <p>
     * The three add commands are read through one method because the caller
     * treats them alike: whichever was typed, it ends with a task to append and
     * announce. Which subclass comes back is this method's business, and the
     * caller only handles a {@link Task}.
     * <p>
     * A task is returned only if every part of it parsed, so a half-built task
     * never escapes to be added to the list.
     *
     * @return the task the line describes
     * @throws ThomasException if a description, marker or date is missing or unreadable
     */
    public Task parseNewTask() throws ThomasException {
        return switch (command) {
        case TODO -> parseTodo();
        case DEADLINE -> parseDeadline();
        case EVENT -> parseEvent();
        // Reached only by calling this for a command that adds no task, which
        // is a mistake in the caller rather than anything the user did.
        default -> throw new AssertionError("Not an add command: " + command);
        };
    }

    /**
     * Builds the task {@code todo <description>} describes.
     *
     * @return the new todo
     * @throws ThomasException if the description is missing
     */
    private Task parseTodo() throws ThomasException {
        return new TodoTask(requireArgument("HEYY!! The description of a todo cannot be empty!"));
    }

    /**
     * Builds the task {@code deadline <description> /by <date>} describes.
     *
     * @return the new deadline
     * @throws ThomasException if the description, the marker or the date is missing
     *                         or the date cannot be read
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
        return new DeadlineTask(description, byDate);
    }

    /**
     * Builds the task {@code event <description> /from <date> /to <date>}
     * describes.
     *
     * @return the new event
     * @throws ThomasException if the description, either marker or either date is
     *                         missing, a date cannot be read, or the event ends
     *                         before it starts
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
        return new EventTask(description, fromDate, toDate);
    }
}