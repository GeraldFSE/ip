package thomas.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import thomas.ThomasException;

/**
 * Represents a single task tracked by the chatbot.
 * A task knows its description and whether it is done, and starts out not done.
 * The date formats every dated task shares are defined here.
 */
public class Task {
    /** Format a date must be written in, by the user and in the save file alike */
    public static final DateTimeFormatter DATE_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HHmm", Locale.ENGLISH);

    /** Format a date is shown in, for example "Dec 02 2019, 6:00 PM" */
    public static final DateTimeFormatter DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mm a", Locale.ENGLISH);

    /** Format a whole day is shown in, with no time, for example "Dec 02 2019" */
    public static final DateTimeFormatter DATE_DISPLAY_DAY =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    /** Separator between fields in the save file */
    public static final String FIELD_SEPARATOR = " | ";

    /** Task text exactly as the user typed it */
    protected String description;

    /** Whether this task has been completed */
    protected boolean isDone;

    /**
     * Creates a task that is not yet done.
     *
     * @param description Task text as entered by the user.
     */
    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    /**
     * Returns the date and time named by the given text.
     * Both a date and a time are required. If either is missing or unreadable,
     * a ThomasException is thrown carrying a message meant for the user.
     *
     * @param text Date as written, expected as yyyy-mm-dd HHmm.
     * @param field Name of the date being read, carrying its own article, for
     *              example "a start date".
     * @return Date and time the text names.
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
     * @return "[X]" when done, "[ ]" otherwise.
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
     * Returns this task as shown to the user, status icon first.
     *
     * @return For example "[X] read book".
     */
    @Override
    public String toString() {
        return getStatusIcon() + " " + this.description;
    }

    /**
     * Returns whether this task falls on the given day.
     * A plain task carries no date, so this base version always returns false.
     * Dated subclasses override it.
     *
     * @param day Day being asked about.
     * @return True if this task falls on that day.
     */
    public boolean occursOn(LocalDate day) {
        return false;
    }

    /**
     * Returns this task encoded for the save file.
     * Kept separate from the display format so that restyling the display
     * cannot break loading.
     *
     * @return For example "1 | read book".
     */
    public String toSaveFormat() {
        return (isDone ? "1" : "0") + FIELD_SEPARATOR + this.description;
    }
}
