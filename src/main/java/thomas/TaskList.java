package thomas;

import java.time.LocalDate;
import java.util.ArrayList;

import thomas.task.Task;

/**
 * Represents the tasks the chatbot is keeping, and the operations over them.
 * Wraps a list rather than being one, so that the tasks can only be changed
 * through the operations below and a task number the user typed is always
 * checked before anything indexes into the list.
 * Two numbering schemes meet in this class, so each accessor says in its name
 * which one it takes: positions count from 0, task numbers count from 1.
 */
public class TaskList {
    /** Tasks, in the order they were added */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list, as used on a first run. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list holding tasks that already exist, as loaded from the
     * save file.
     *
     * @param tasks Tasks to start with, in list order, taken over as-is.
     */
    public TaskList(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Returns how many tasks are stored.
     *
     * @return Number of tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns the task at the given position, counting from 0.
     * Used for walking the whole list, where the position is already known to be
     * valid. A number that came from the user goes through getByNumber instead.
     *
     * @param index Position, from 0 to one less than the size.
     * @return Task at that position.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task Task to store.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the position matching a task number the user typed.
     * The user counts tasks from 1 and the list counts from 0, so the number is
     * checked against the tasks that exist and then shifted down by one here,
     * which is the single place that conversion happens.
     *
     * @param taskNumber Number as the user typed it, counting from 1.
     * @return Matching position, counting from 0.
     * @throws ThomasException If no task carries that number.
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
     * @param taskNumber Number as the user typed it, counting from 1.
     * @return Task carrying that number.
     * @throws ThomasException If no task carries that number.
     */
    public Task getByNumber(int taskNumber) throws ThomasException {
        return tasks.get(requirePosition(taskNumber));
    }

    /**
     * Removes the task the user named by its number and returns it.
     * Removing closes the gap, so the tasks after it shift down one and the
     * numbers the user sees never develop holes.
     *
     * @param taskNumber Number as the user typed it, counting from 1.
     * @return Task that was removed.
     * @throws ThomasException If no task carries that number.
     */
    public Task deleteByNumber(int taskNumber) throws ThomasException {
        return tasks.remove(requirePosition(taskNumber));
    }

    /**
     * Returns the positions of the tasks that fall on the given day.
     * Positions are returned rather than the tasks themselves, because each
     * match is shown numbered by where it sits in the whole list, which is the
     * number that mark and delete take.
     *
     * @param day Day being asked about.
     * @return Positions of the matching tasks, counting from 0, in list order.
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
