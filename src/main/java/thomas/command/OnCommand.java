package thomas.command;

import java.time.LocalDate;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Shows the tasks falling on one day, serving the on command.
 */
public class OnCommand extends Command {
    /** Day being asked about, already read from the typed line */
    private final LocalDate day;

    /**
     * Creates the command for one day.
     *
     * @param day Day to report on.
     */
    public OnCommand(LocalDate day) {
        this.day = day;
    }

    /**
     * Prints the tasks that fall on this command's day.
     * Nothing is saved, since nothing changed.
     *
     * @param tasks Tasks to search.
     * @param ui Used to print the matches.
     * @param storage Unused.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOnDay(tasks, day);
    }
}
