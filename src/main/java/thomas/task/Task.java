package thomas.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import thomas.ThomasException;

/**
 * A single task tracked by the chatbot.
 * <p>
 * A task knows its description and whether it has been completed. Tasks start
 * out not done; {@link #markAsDone()} and {@link #unmarkAsDone()} switch the
 * completion state.
 * <p>
 * The two date formats every dated task shares live here, so the subclasses
 * that carry dates and the parser that reads them cannot drift apart.
 */
public class Task {
    /**
     * How a date must be written, both by the user and in the save file, for
     * example {@code 2019-12-02 1800}.
     * <p>
     * A date and a time are both required. {@code HH} is the hour on a 24-hour
     * clock, so no am/pm marker is needed -- {@code hh} would be the 12-hour
     * hour and ambiguous without one. Case matters throughout: {@code mm} is
     * minutes and {@code MM} months, which is why the minutes here follow a
     * {@code HH} and the months follow a {@code yyyy}.
     * <p>
     * Reading and writing through the same format means whatever is saved is
     * always something the parser can read back.
     * <p>
     * The locale is pinned because {@code ofPattern} otherwise follows whatever
     * the machine is set to, which would change the month names and am/pm
     * markers depending on where the chatbot is run.
     */
    public static final DateTimeFormatter DATE_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm", Locale.ENGLISH);

    /**
     * How a date is shown back to the user, for example
     * {@code "Dec 02 2019, 6:00 PM"}.
     * <p>
     * Deliberately not the format it is typed in: storing a real date rather
     * than the user's text is what makes showing it a different way possible.
     * {@code MMM} is the short month name, {@code dd} the zero-padded day,
     * {@code h} the 12-hour hour and {@code a} the am/pm marker.
     */
    public static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mm a", Locale.ENGLISH);

    /**
     * How a whole day is shown, with no time, for example {@code "Dec 02 2019"}.
     * <p>
     * Separate from {@link #DATE_DISPLAY_FORMAT} rather than reusing it,
     * because a {@link java.time.LocalDate} carries no hour or minute:
     * formatting one with a pattern that asks for them throws
     * {@code UnsupportedTemporalTypeException} at run time, and nothing about
     * it fails to compile.
     */
    public static final DateTimeFormatter DATE_DISPLAY_DAY =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /**
     * What separates one field from the next in the save file.
     * <p>
     * Named here, beside the {@link #toSaveFormat()} that writes it, so that the
     * parser can refuse a description containing it without holding a second
     * copy of the same literal. {@link thomas.Storage} splits on it as a regular
     * expression, where the {@code |} must be escaped, so it keeps its own
     * spelling of it rather than using this one.
     */
    public static final String FIELD_SEPARATOR = " | ";

    /** The task text exactly as the user typed it. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task that is not yet done.
     *
     * @param description The task text as entered by the user.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Turns a date written in {@link #DATE_INPUT_FORMAT} into a
     * {@link LocalDateTime}.
     * <p>
     * Kept in the same class as the format it reads, so the two cannot drift
     * apart, and so both callers -- the command handling and the save file
     * loader -- share one reader rather than each growing their own.
     * <p>
     * A date and a time are both required: a date on its own is refused rather
     * than being assumed to mean midnight, so a task never claims a time the
     * user did not choose.
     * <p>
     * The parser's own exception is rethrown as a {@link ThomasException} so
     * that callers have one kind of user error to report and no Java class name
     * reaches the user.
     *
     * @param text The date as written, expected as {@code yyyy-mm-dd HHmm}.
     * @param field Which date this is, named as it should read in the message
     *              and so carrying its own article, for example
     *              {@code "a deadline date"} against {@code "an end date"}.
     * @return The date and time the text names.
     * @throws ThomasException If the text is not a date and time in that form.
     */
    public static LocalDateTime parseDate(String text, String field) throws ThomasException {
        try {
            return LocalDateTime.parse(text, DATE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ThomasException("I can't read '" + text + "' as " + field
                    + "! Write it as a date and a 24-hour time, like 2019-12-02 1800.");
        }
    }


    /**
     * Returns the icon showing whether this task is done.
     *
     * @return {@code "[X]"} when done, {@code "[ ]"} otherwise.
     */
    public String getStatusIcon() {
        return (isDone ? "[X]" : "[ ]"); // mark done task with X
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not completed. */
    public void unmarkAsDone() {
        this.isDone = false;
    }

    /**
     * Returns this task as it should be shown to the user: status icon first,
     * then the description.
     * <p>
     * Keeping the format here means callers never assemble it themselves, so a
     * future task type can change how it appears by overriding this method
     * alone.
     *
     * @return For example {@code "[X] read book"}.
     */
    @Override
    public String toString() {
        return getStatusIcon() + " " + this.description;
    }

    /**
     * Returns whether this task falls on a given day.
     * <p>
     * A plain task carries no date, so it is never on any particular day and
     * this base version always says no. The dated subclasses override it, which
     * is what lets the {@code on} command filter the list without asking any
     * task what type it is: adding another dated task type means overriding
     * this method, not extending a chain of {@code instanceof} checks.
     *
     * @param day The day being asked about.
     * @return True if this task falls on that day.
     */
    public boolean occursOn(LocalDate day) {
        return false;
    }

    /**
     * Returns whether this task's description contains a keyword.
     * <p>
     * Asking the task rather than reading its description keeps
     * {@code description} to itself, exactly as {@link #occursOn(LocalDate)}
     * does for the dates: {@link thomas.TaskList} can filter without any task
     * type having to expose what it holds.
     * <p>
     * The match is on the description alone, not on {@link #toString()}, so a
     * search never hits the type tag or a formatted date. Searching for
     * {@code "D"} finds the tasks with a D in their text, not every deadline.
     * <p>
     * Matching is case sensitive, as command keywords are. A search that
     * ignored case would be friendlier, and is the obvious next change here.
     *
     * @param keyword the text to look for, as the user typed it
     * @return true if the description contains that text
     */
    public boolean matches(String keyword) {
        return description.contains(keyword);
    }

    /**
     * Returns this task encoded for the save file.
     * <p>
     * This is deliberately separate from {@link #toString()}: that one is for
     * the person reading the screen and is free to change, while this one is
     * the format {@code Thomas} parses back on start-up. Keeping them apart
     * means restyling the display cannot break loading.
     * <p>
     * Fields are separated by {@code " | "}, and the completion state is a
     * digit rather than {@code "[X]"} so the parser never has to strip
     * brackets. Subclasses prefix their type letter and append their own
     * fields, exactly as they do for {@code toString()}.
     *
     * @return For example {@code "1 | read book"}.
     */
    public String toSaveFormat() {
        return (isDone ? "1" : "0") + FIELD_SEPARATOR + this.description;
    }
}
