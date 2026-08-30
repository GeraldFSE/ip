package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.ThomasException;
import thomas.Ui;
import thomas.task.Task;

/**
 * Marks a task not done again: the {@code unmark <number>} command.
 * <p>
 * Deliberately its own class rather than a {@link MarkCommand} carrying a
 * true/false flag. The two differ only in one call and one message, but a
 * boolean argument at the call site says nothing about which way round it is:
 * {@code new MarkCommand(3, false)} has to be looked up, where
 * {@code new UnmarkCommand(3)} reads as what it does.
 */
public class UnmarkCommand extends Command {
    /**
     * The task number the user typed, counting from 1.
     * <p>
     * Held as the number rather than the task itself, because whether any task
     * carries this number cannot be answered until the list is in hand, which
     * is at {@link #execute} rather than when the command is built.
     */
    private final int taskNumber;

    /**
     * Creates the command for one task number.
     *
     * @param taskNumber the number as the user typed it, counting from 1 and not
     *                   yet checked against the list
     */
    public UnmarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Marks the named task not done, saves, and says so.
     *
     * @param tasks   the list holding the task
     * @param ui      used to confirm the change
     * @param storage where the changed list is written
     * @throws ThomasException if no task carries that number
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws ThomasException {
        Task task = tasks.getByNumber(taskNumber);
        task.unmarkAsDone();
        save(tasks, ui, storage);
        ui.showUnmarked(task);
    }
}
