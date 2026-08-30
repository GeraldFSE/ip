package thomas.command;

import thomas.Storage;
import thomas.TaskList;
import thomas.Ui;

/**
 * Shows the tasks whose description contains a keyword: the
 * {@code find <keyword>} command.
 * <p>
 * Close kin to {@link OnCommand}: both narrow the list and print what is left,
 * and both leave the list untouched. They differ only in what makes a task
 * match, which is why each holds the one thing it searches by rather than
 * sharing a class with a flag saying which kind of search this is.
 */
public class FindCommand extends Command {
    /** The text being searched for, already read from the typed line. */
    private final String keyword;

    /**
     * Creates the command for one keyword.
     *
     * @param keyword the text to look for in task descriptions
     */
    public FindCommand(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Prints the tasks whose description contains this command's keyword.
     * <p>
     * Nothing is saved, because nothing changed.
     *
     * @param tasks   the tasks to search
     * @param ui      used to print the matches
     * @param storage unused
     */
    @Override
    public void execute(TaskList tasks, Ui ui, Storage storage) {
        ui.showMatchingTasks(tasks, keyword);
    }
}
