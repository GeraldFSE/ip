package thomas.command;

import java.io.IOException;

import thomas.Storage;
import thomas.TaskList;
import thomas.ThomasException;
import thomas.Ui;

/**
 * Represents one command, understood and ready to be carried out.
 * A command is built from a typed line with its arguments already read and
 * checked, so that understanding a line and doing what it asks stay separate.
 * Each command is its own subclass rather than a branch of a switch, so that
 * adding a command means writing a class rather than editing the loop that runs
 * them all.
 * The three collaborators are handed to execute rather than held as fields,
 * which keeps plain what each command is allowed to touch.
 */
public abstract class Command {
    /**
     * Carries out this command.
     *
     * @param tasks Task list to read or change.
     * @param ui Used to tell the user what happened.
     * @param storage Used to write the tasks when they change.
     * @throws ThomasException If the command cannot be carried out, for example
     *                         because it names a task that does not exist.
     */
    public abstract void execute(TaskList tasks, Ui ui, Storage storage) throws ThomasException;

    /**
     * Returns whether the chatbot should stop after this command.
     * False for almost every command, so only the exit command overrides it.
     * Asking the command means the read loop never tests which one it holds.
     *
     * @return True if this command ends the session.
     */
    public boolean isExit() {
        return false;
    }

    /**
     * Saves the task list, reporting a failure instead of crashing.
     * Inherited by the commands that change the list, which makes the save
     * automatic and keeps it worded once. If the save fails the user is warned
     * rather than losing the session.
     *
     * @param tasks Tasks to write.
     * @param ui Used to report a failed save.
     * @param storage Used to write them.
     */
    protected void save(TaskList tasks, Ui ui, Storage storage) {
        try {
            storage.save(tasks);
        } catch (IOException e) {
            ui.showSavingError(e.getMessage());
        }
    }
}
