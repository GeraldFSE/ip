package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Ends the session, serving the bye command.
 * The only command that reports itself as an exit, which is how the read loop
 * knows to stop without testing which command it is holding.
 */
public class ExitCommand extends Command {
    /**
     * Does nothing.
     * The farewell belongs after the read loop, since the session also ends when
     * the input runs out, and printing it here as well would say it twice.
     * There is nothing to save either, since bye changes no tasks.
     *
     * @param tasks Unused.
     * @param ui Unused.
     * @param storage Unused.
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: stopping is what this command is for, and that is
        // reported through isExit() rather than done here.
    }

    /**
     * Returns that the chatbot should stop.
     *
     * @return Always true.
     */
    @Override
    public boolean isExit() {
        return true;
    }
}
