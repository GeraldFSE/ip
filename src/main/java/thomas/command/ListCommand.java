package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Shows every stored task, numbered, serving the list command.
 */
public class ListCommand extends Command {
    /**
     * Prints the whole task list.
     * Nothing is saved, since nothing changed.
     *
     * @param tasks Tasks to show.
     * @param ui Used to print them.
     * @param storage Unused.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList(tasks);
    }
}
