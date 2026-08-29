import java.time.LocalDate;

/**
 * Shows the tasks falling on one day: the {@code on <day>} command.
 */
public class OnCommand extends Command {
    /** The day being asked about, already read from the typed line. */
    private final LocalDate day;

    /**
     * Creates the command for one day.
     *
     * @param day the day to report on
     */
    public OnCommand(LocalDate day) {
        this.day = day;
    }

    /**
     * Prints the tasks that fall on this command's day.
     * <p>
     * Nothing is saved, because nothing changed.
     *
     * @param tasks   the tasks to search
     * @param ui      used to print the matches
     * @param storage unused
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showTasksOnDay(tasks, day);
    }
}