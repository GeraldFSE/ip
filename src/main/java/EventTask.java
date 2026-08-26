import java.time.LocalDateTime;

/**
 * A task that runs from one date and time to another.
 * <p>
 * Like {@link DeadlineTask}, the start and end are kept as
 * {@link LocalDateTime}s: read in as {@code yyyy-mm-dd HHmm} and shown back in
 * a friendlier form, using the formats {@link Task} holds for both.
 */
public class EventTask extends Task {
    /** When the event starts, as given after {@code /from}. */
    protected LocalDateTime from;

    /** When the event ends, as given after {@code /to}. */
    protected LocalDateTime to;

    /**
     * Creates an event that is not yet done.
     *
     * @param description the task text, without the {@code event} keyword
     * @param from        the date and time the event starts
     * @param to          the date and time the event ends
     */
    public EventTask(String description, LocalDateTime from, LocalDateTime to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event with its type tag and time range.
     *
     * @return for example
     *         {@code "[E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 02 2019, 4:00 PM)"}
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from.format(DATE_DISPLAY_FORMAT)
                + " to: " + to.format(DATE_DISPLAY_FORMAT) + ")";
    }

    /**
     * Returns the event encoded for the save file, tagged {@code E} with the
     * start and end as the fourth and fifth fields.
     * <p>
     * As in {@link DeadlineTask}, the dates are written in the format they are
     * typed in, so the parser can read back whatever is saved.
     *
     * @return for example {@code "E | 0 | project meeting | 2019-12-02 1400 | 2019-12-02 1600"}
     */
    @Override
    public String toSaveFormat() {
        return "E | " + super.toSaveFormat() + " | " + from.format(DATE_INPUT_FORMAT)
                + " | " + to.format(DATE_INPUT_FORMAT);
    }
}