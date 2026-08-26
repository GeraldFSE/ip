import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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

    /** The task text exactly as the user typed it. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /**
     * Creates a task that is not yet done.
     *
     * @param description the task text as entered by the user
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the icon showing whether this task is done.
     *
     * @return {@code "[X]"} when done, {@code "[ ]"} otherwise
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
     * @return for example {@code "[X] read book"}
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
     * @param day the day being asked about
     * @return true if this task falls on that day
     */
    public boolean occursOn(LocalDate day) {
        return false;
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
     * @return for example {@code "1 | read book"}
     */
    public String toSaveFormat() {
        return (isDone ? "1" : "0") + " | " + this.description;
    }
}
