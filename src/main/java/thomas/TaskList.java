package thomas;

import java.time.LocalDate;

import java.util.ArrayList;

import thomas.task.Task;

/**
 * The tasks the chatbot is keeping, and the operations over them.
 * <p>
 * This wraps an {@link ArrayList} rather than being one, so that the list can
 * only be changed through the operations below. That is what lets the
 * user-facing rules live here instead of being repeated by every caller: a task
 * number the user typed is checked against the list that actually exists before
 * anything indexes into it.
 * <p>
 * Two numbering schemes meet in this class, so each accessor says in its name
 * which one it takes. {@link #get(int)} takes a position counting from 0, as
 * {@code ArrayList} does, and is for walking the list. {@link #getByNumber(int)}
 * and {@link #deleteByNumber(int)} take the number the user sees, counting from
 * 1, and are for carrying out commands. Conversion between the two happens here
 * and nowhere else.
 */
public class TaskList {
    /**
     * The tasks, in the order they were added.
     * <p>
     * An {@code ArrayList} rather than a {@code Task[]}: it grows as tasks are
     * added, so there is no fixed ceiling to enforce, and removing closes the
     * gap left by a deleted task instead of leaving a hole to shuffle by hand.
     */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list, as used on a first run. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding tasks that already exist, as loaded from the
     * save file.
     *
     * @param tasks the tasks to start with, in list order; taken over as-is
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Returns how many tasks are stored.
     *
     * @return the number of tasks
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the task at a position, counting from 0.
     * <p>
     * For walking the whole list, where the position is already known to be
     * valid. A number that came from the user goes through
     * {@link #getByNumber(int)} instead, which checks it.
     *
     * @param index the position, from 0 to {@code size() - 1}
     * @return the task at that position
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to store
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Checks a task number the user typed and turns it into a position.
     * <p>
     * The user counts tasks from 1 and the list counts from 0, so the number is
     * checked against the tasks that exist and then shifted down by one here --
     * the single place that conversion happens. The range is checked against
     * the current size rather than any capacity: a number past the end must be
     * reported, not passed to {@link ArrayList#get(int)}.
     *
     * @param taskNumber the number as the user typed it, counting from 1
     * @return the matching position, counting from 0
     * @throws ThomasException if no task carries that number
     */
    private int requirePosition(int taskNumber) throws ThomasException {
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ThomasException("There is no task " + taskNumber + "! You only have "
                    + tasks.size() + " task(s).");
        }
        return taskNumber - 1;
    }

    /**
     * Returns the task the user named by its number.
     *
     * @param taskNumber the number as the user typed it, counting from 1
     * @return the task carrying that number
     * @throws ThomasException if no task carries that number
     */
    public Task getByNumber(int taskNumber) throws ThomasException {
        return tasks.get(requirePosition(taskNumber));
    }

    /**
     * Removes the task the user named by its number and returns it.
     * <p>
     * The removed task is returned so it can be shown back to the user. Removing
     * closes the gap: everything after it shifts down one, so the numbering
     * stays contiguous and the numbers the user sees never develop holes.
     *
     * @param taskNumber the number as the user typed it, counting from 1
     * @return the task that was removed
     * @throws ThomasException if no task carries that number
     */
    public Task deleteByNumber(int taskNumber) throws ThomasException {
        return tasks.remove(requirePosition(taskNumber));
    }

    /**
     * Returns the positions of the tasks that fall on a given day.
     * <p>
     * Positions rather than the tasks themselves, because the caller shows each
     * match numbered by where it sits in the whole list, not by where it sits
     * among the matches: a number shown must be the number {@code mark} and
     * {@code delete} take. Handing back only the tasks would lose that, and
     * numbering the matches 1, 2, 3 would read more tidily and send the user to
     * the wrong task.
     * <p>
     * Which tasks match is decided by {@link Task#occursOn(LocalDate)}, so this
     * never asks a task what type it is: a new dated task type overrides that
     * method and is filtered correctly here without this loop changing.
     *
     * @param day the day being asked about
     * @return the positions of the matching tasks, counting from 0, in list order
     */
    public ArrayList<Integer> positionsOn(LocalDate day) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).occursOn(day)) {
                positions.add(i);
            }
        }
        return positions;
    }
}