/**
 * A task that runs from one point in time to another.
 * <p>
 * Like {@link DeadlineTask}, the start and end are kept as the text the user
 * typed rather than being parsed into real dates.
 */
public class EventTask extends Task {
    /** When the event starts, as typed after {@code /from}. */
    protected String from;

    /** When the event ends, as typed after {@code /to}. */
    protected String to;

    /**
     * Creates an event that is not yet done.
     *
     * @param description the task text, without the {@code event} keyword
     * @param from        the start date/time as free text, for example {@code "Mon 2pm"}
     * @param to          the end date/time as free text, for example {@code "4pm"}
     */
    public EventTask(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Returns the event with its type tag and time range.
     *
     * @return for example {@code "[E][ ] project meeting (from: Mon 2pm to: 4pm)"}
     */
    @Override
    public String toString() {
        return "[E]" + super.toString() + " (from: " + from + " to: " + to + ")";
    }

    /**
     * Returns the event encoded for the save file, tagged {@code E} with the
     * start and end as the fourth and fifth fields.
     *
     * @return for example {@code "E | 0 | project meeting | Mon 2pm | 4pm"}
     */
    @Override
    public String toSaveFormat() {
        return "E | " + super.toSaveFormat() + " | " + from + " | " + to;
    }
}