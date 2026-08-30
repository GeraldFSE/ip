package thomas.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import thomas.ThomasException;

/**
 * Represents a task that runs from one date and time to another.
 * The start and end are kept as date-time values rather than as the text the
 * user typed, so that they can be read in one format and shown in another.
 */
public class EventTask extends Task {
    /** Date and time the event starts, as given after /from */
    protected LocalDateTime from;

    /** Date and time the event ends, as given after /to */
    protected LocalDateTime to;

    /**
     * Creates an event that is not yet done.
     * The check is made here rather than in the command that reads the event, so
     * that the save file is held to the same rule. If the end equals the start
     * the event is allowed, since an event lasting no time says nothing false.
     *
     * @param description Task text, without the event keyword.
     * @param from Date and time the event starts.
     * @param to Date and time the event ends, not before the start.
     * @throws ThomasException If the end falls before the start.
     */
    public EventTask(String description, LocalDateTime from, LocalDateTime to)
            throws ThomasException {
        super(description);
        if (from.isAfter(to)) {
            throw new ThomasException("HUH?! Your event ends before it starts! "
                    + "Check your /from and /to.");
        }
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event with its type tag and time range.
     *
     * @return For example "[E][ ] project meeting (from: Dec 02 2019, 2:00 PM
     *         to: Dec 02 2019, 4:00 PM)".
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DATE_DISPLAY_FORMAT)
                + " to: " + to.format(DATE_DISPLAY_FORMAT) + ")";
    }

    /**
     * Returns whether this event is running on the given day.
     * An event covers every day from its start to its end, and both end days
     * count as part of it.
     *
     * @param day Day being asked about.
     * @return True if the event is running on that day.
     */
    @Override
    public boolean occursOn(LocalDate day) {
        LocalDate start = from.toLocalDate();
        LocalDate end = to.toLocalDate();
        return !day.isBefore(start) && !day.isAfter(end);
    }

    /**
     * Returns the event encoded for the save file, tagged E with the start and
     * end as the fourth and fifth fields.
     * The dates are written in the format they are typed in, so that the parser
     * can read back whatever is saved.
     *
     * @return For example "E | 0 | project meeting | 2019-12-02 1400 |
     *         2019-12-02 1600".
     */
    @Override
    public String toSaveFormat() {
        return "E | " + super.toSaveFormat() + " | " + from.format(DATE_INPUT_FORMAT)
                + " | " + to.format(DATE_INPUT_FORMAT);
    }
}
