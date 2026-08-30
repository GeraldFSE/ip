package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.ThomasException;
import thomas.Ui;
import thomas.task.Task;

/**
 * Marks a task done, serving the mark command.
 */
public class MarkCommand extends Command {
    /** Task number the user typed, counting from 1 and not yet checked */
    private final int taskNumber;

    /**
     * Creates the command for one task number.
     *
     * @param taskNumber Number as the user typed it, counting from 1 and not yet
     *                   checked against the list.
     */
    public MarkCommand(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    /**
     * Marks the named task done, saves, and says so.
     *
     * @param tasks List holding the task.
     * @param ui Used to confirm the change.
     * @param storage Used to write the changed list.
     * @throws ThomasException If no task carries that number.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) throws ThomasException {
        Task task = tasks.getByNumber(taskNumber);
        task.markAsDone();
        save(tasks, ui, storage);
        ui.showMarked(task);
    }
}
