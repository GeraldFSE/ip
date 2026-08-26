/**
 * A single task tracked by the chatbot.
 * <p>
 * A task knows its description and whether it has been completed. Tasks start
 * out not done; {@link #markAsDone()} and {@link #unmarkAsDone()} switch the
 * completion state.
 */
public class Task {
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
