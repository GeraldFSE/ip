package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Shows every stored task, numbered: the {@code list} command.
 */
public class ListCommand extends Command {
    /**
     * Returns the whole task list.
     * <p>
     * Nothing is saved, because nothing changed.
     *
     * @param tasks The tasks to show.
     * @param ui Used to word them.
     * @param storage Unused.
     * @return The numbered list.
     */
    @Override
    public String execute(TaskList tasks, Ui ui, Storage storage) {
        return ui.getTaskListMessage(tasks);
    }
}
