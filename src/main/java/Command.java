import java.io.IOException;

import thomas.Storage;
import thomas.TaskList;
import thomas.ThomasException;
import thomas.Ui;

/**
 * One command, understood and ready to be carried out.
 * <p>
 * A {@code Command} is built by {@link Parser} from a line the user typed, with
 * whatever arguments that command takes already read and checked. Carrying it
 * out is then {@link #execute} -- so the work of understanding a line and the
 * work of doing what it asks are separated, and neither has to be read to
 * follow the other.
 * <p>
 * Each command is its own subclass rather than a branch of a switch. What that
 * buys is that everything about a command sits in one file: {@link Thomas} asks
 * for a command and runs it without knowing which one it got, and adding a new
 * one means writing a class rather than editing the loop that runs them all.
 * <p>
 * The three collaborators are handed to {@link #execute} rather than held as
 * fields, because a command is built fresh for every line and would otherwise
 * carry three references it uses once. It also keeps plain what each command is
 * allowed to touch: the tasks, the screen, and the save file.
 */
public abstract class Command {
    /**
     * Carries out this command.
     *
     * @param tasks   the task list to read or change
     * @param ui      how to tell the user what happened
     * @param storage where to write the tasks when they change
     * @throws ThomasException if the command cannot be carried out, for example
     *                         because it names a task that does not exist --
     *                         which cannot be known until the list is in hand
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws ThomasException;

    /**
     * Returns whether the chatbot should stop after this command.
     * <p>
     * False for almost every command, so that is the answer here and only
     * {@link ExitCommand} overrides it. Asking the command means the read loop
     * never tests which command it is holding.
     *
     * @return true if this command ends the session
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Saves the task list, reporting a failure instead of crashing.
     * <p>
     * Inherited by the commands that change the list, which is what makes the
     * save automatic and keeps it worded once. {@link Storage#save} throws
     * rather than printing, because it has no business talking to the user;
     * catching the {@link IOException} here means a save failure costs the user
     * a warning rather than the session.
     *
     * @param tasks   the tasks to write
     * @param ui      used to report a failed save
     * @param storage where to write them
     */
    protected void save(TaskList tasks, Ui ui, Storage storage) {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}