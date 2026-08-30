package thomas.task;

/**
 * Represents a task with no date or time attached, such as "borrow book".
 * Adds no state of its own to Task, and exists so that a to-do can be told
 * apart from the other task types and shown with its own [T] tag.
 */
public class TodoTask extends Task {
    /**
     * Creates a to-do that is not yet done.
     *
     * @param description Task text, without the to-do keyword.
     */
    public TodoTask(String description) {
        super(description);
    }

    /**
     * Returns the to-do prefixed with its type tag.
     *
     * @return For example "[T][ ] borrow book".
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }

    /**
     * Returns the to-do encoded for the save file, tagged T.
     *
     * @return For example "T | 0 | borrow book".
     */
    @Override
    public String toSaveFormat() {
        return "T | " + super.toSaveFormat();
    }
}
