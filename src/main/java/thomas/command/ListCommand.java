package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Shows every stored task, numbered: the {@code list} command.
 */
public class ListCommand extends Command {
    /**
     * Prints the whole task list.
     * <p>
     * Nothing is saved, because nothing changed.
     *
     * @param tasks The tasks to show.
     * @param ui Used to print them.
     * @param storage Unused.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTaskList(tasks);
    }
}
