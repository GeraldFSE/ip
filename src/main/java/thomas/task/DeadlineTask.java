package thomas.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a task that must be done before a given date and time.
 * The due date is kept as a date-time value rather than as the text the user
 * typed, so that it can be read in one format and shown in another.
 */
public class DeadlineTask extends Task {
    /** Date and time the task is due, as given after /by */
    protected LocalDateTime by;

    /**
     * Creates a deadline that is not yet done.
     *
     * @param description Task text, without the deadline keyword.
     * @param by Date and time the task is due.
     */
    public DeadlineTask(String description, LocalDateTime by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline with its type tag and due date.
     *
     * @return For example "[D][ ] return book (by: Dec 02 2019, 6:00 PM)".
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by.format(DATE_DISPLAY_FORMAT) + ")";
    }

    /**
     * Returns whether this deadline falls due on the given day.
     * The time of day is dropped for the comparison, so a deadline at any hour
     * counts as being on that day.
     *
     * @param day Day being asked about.
     * @return True if the task is due on that day.
     */
    @Override
    public boolean occursOn(LocalDate day) {
        return by.toLocalDate().equals(day);
    }

    /**
     * Returns the deadline encoded for the save file, tagged D with the due date
     * as a fourth field.
     * The date is written in the format it is typed in, so that the save file
     * holds nothing the parser cannot read back.
     *
     * @return For example "D | 0 | return book | 2019-12-02 1800".
     */
    @Override
    public String toSaveFormat() {
        return "D | " + super.toSaveFormat() + " | " + by.format(DATE_INPUT_FORMAT);
    }
}
