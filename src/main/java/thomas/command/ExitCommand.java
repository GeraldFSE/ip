package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Ends the session: the {@code bye} command.
 * <p>
 * The only command that answers true to {@link #isExit()}, which is how the
 * read loop knows to stop without ever testing which command it is holding.
 */
public class ExitCommand extends Command {
    /**
     * Does nothing.
     * <p>
     * Saying goodbye is deliberately not done here. The session also ends when
     * the input runs out with no {@code bye} typed at all, and the farewell must
     * be printed in both cases; it therefore belongs after the read loop, which
     * is the one place both endings pass through. Printing it here as well would
     * say it twice on the way out.
     * <p>
     * There is nothing to save either: {@code bye} changes no tasks, and every
     * command that does change them has already saved.
     *
     * @param tasks   unused
     * @param ui      unused
     * @param storage unused
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        // Nothing to do: stopping is what this command is for, and that is
        // reported through isExit() rather than done here.
    }

    /**
     * Reports that the chatbot should stop.
     *
     * @return always true
     */
    @Override
    public boolean isExit() {
        return true;
    }
}