package thomas.command;

import thomas.Parser;
import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;
import thomas.task.Task;

/**
 * Stores a new task: the {@code todo}, {@code deadline} and {@code event}
 * commands.
 * <p>
 * One class for all three, because once the line has been read they differ only
 * in which kind of {@link Task} was built -- and appending it, saving and
 * announcing it are the same either way. Which subclass it is stays the task's
 * own business, so a fourth kind of task would need no change here.
 */
public class AddCommand extends Command {
    /**
     * The task to store, built by {@link Parser} from the typed line.
     * <p>
     * Already complete when this command is created, so a task whose arguments
     * did not parse never reaches the list: the failure happens while the
     * command is being built, before there is anything to execute.
     */
    private final Task task;

    /**
     * Creates the command for one new task.
     *
     * @param task the task to store
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Appends the task, saves, and reports the new size of the list.
     *
     * @param tasks   the list to append to
     * @param ui      used to confirm the addition
     * @param storage where the longer list is written
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        tasks.add(task);
        save(tasks, ui, storage);
        ui.showAdded(task, tasks.size());
    }
}
