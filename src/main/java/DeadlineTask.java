/**
 * A task that must be done before a given point in time.
 * <p>
 * The due date is kept as the text the user typed. Parsing it into a real date
 * is a later increment, so anything the user writes is accepted as-is.
 */
public class DeadlineTask extends Task {
    /** When the task is due, exactly as the user typed it after {@code /by}. */
    protected String by;

    /**
     * Creates a deadline that is not yet done.
     *
     * @param description the task text, without the {@code deadline} keyword
     * @param by          the due date/time as free text, for example {@code "Sunday"}
     */
    public DeadlineTask(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Returns the deadline with its type tag and due date.
     *
     * @return for example {@code "[D][ ] return book (by: Sunday)"}
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}