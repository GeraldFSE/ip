import thomas.ThomasException;
import thomas.task.Task;

/**
 * Removes a task from the list: the {@code delete <number>} command.
 */
public class DeleteCommand extends Command {
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
    public DeleteCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Removes the named task, saves, and reports what is left.
     * <p>
     * The removed task is shown back to the user, so the count reported is the
     * size after the removal.
     *
     * @param tasks   the list to remove from
     * @param ui      used to confirm the removal
     * @param storage where the shortened list is written
     * @throws ThomasException if no task carries that number
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws ThomasException {
        Task removedTask = tasks.deleteByNumber(taskNumber);
        save(tasks, ui, storage);
        ui.showRemoved(removedTask, tasks.size());
    }
}