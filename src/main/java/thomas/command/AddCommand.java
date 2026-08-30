package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;
import thomas.task.Task;

/**
 * Stores a new task, serving the todo, deadline and event commands.
 * One class serves all three, because once the line has been read they differ
 * only in which kind of task was built, and appending, saving and announcing it
 * are the same either way.
 */
public class AddCommand extends Command {
    /** Task to store, already complete when this command is created */
    private final Task task;

    /**
     * Creates the command for one new task.
     *
     * @param task Task to store.
     */
    public AddCommand(Task task) {
        this.task = task;
    }

    /**
     * Appends the task, saves, and reports the new size of the list.
     *
     * @param tasks List to append to.
     * @param ui Used to confirm the addition.
     * @param storage Used to write the longer list.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        tasks.add(task);
        save(tasks, ui, storage);
        ui.showAdded(task, tasks.size());
    }
}
