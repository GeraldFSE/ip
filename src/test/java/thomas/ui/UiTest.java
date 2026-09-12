package thomas.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import thomas.task.DeadlineTask;
import thomas.task.Task;
import thomas.task.TaskList;
import thomas.task.TodoTask;

/**
 * Tests the headers of the three listings Ui words.
 * Wording is otherwise left to the text-UI plan, which drives the whole program
 * and pins down every line the user sees. This file is deliberately narrow, and
 * is here because of one defect that suite caught too late.
 * The three listings share one private method, which takes the header to open
 * with as an argument. A merge once left that argument declared, passed by all
 * three callers, and read by none, so every listing opened with the {@code list}
 * header. Nothing failed to compile, checkstyle passed, and no JUnit case
 * touched it, which left a full run of the text-UI plan as the only thing that
 * could notice -- and that plan had been carrying the right expected output,
 * correctly failing, unrun.
 * What is pinned here is therefore only what the compiler cannot see: that each
 * listing opens with its own header, and that the numbers shown are positions in
 * the whole list rather than positions among the matches. Both belong to the
 * shared method, and neither is worth restating for every line it prints.
 */
public class UiTest {

    /** Day used throughout, with one either side to make the filtering real */
    private static final LocalDate DEC_02 = LocalDate.of(2019, 12, 2);
    private static final LocalDate DEC_03 = LocalDate.of(2019, 12, 3);

    /**
     * Returns a list holding the given tasks, in the order given.
     *
     * @param tasks Tasks to hold, in list order.
     * @return List holding those tasks.
     */
    private static TaskList listOf(Task... tasks) {
        TaskList list = new TaskList();
        for (Task task : tasks) {
            list.add(task);
        }
        return list;
    }

    /**
     * Returns a deadline due at 6pm on the given day.
     *
     * @param description Task text.
     * @param day Day the task is due.
     * @return New deadline.
     */
    private static Task deadlineOn(String description, LocalDate day) {
        return new DeadlineTask(description, day.atTime(18, 0));
    }

    // ---- each listing opens with its own header ----

    @Test
    public void getTaskListMessage_severalTasks_opensWithTheListHeader() {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("buy milk"));

        assertEquals("Here are the tasks in your list:\n1. [T][ ] read book\n2. [T][ ] buy milk",
                new Ui().getTaskListMessage(list));
    }

    @Test
    public void getMatchingTasksMessage_matches_opensWithTheMatchingHeader() {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("buy milk"));

        // Not the list header: a search that said "here are the tasks in your
        // list" would read as though nothing had been filtered at all.
        assertEquals("Here are the matching tasks in your list:\n1. [T][ ] read book",
                new Ui().getMatchingTasksMessage(list, "read"));
    }

    @Test
    public void getTasksOnDayMessage_matches_opensWithTheDayInTheHeader() {
        TaskList list = listOf(deadlineOn("return book", DEC_02));

        assertEquals("Here are the tasks on Dec 02 2019:\n"
                        + "1. [D][ ] return book (by: Dec 02 2019, 6:00 PM)",
                new Ui().getTasksOnDayMessage(list, DEC_02));
    }

    @Test
    public void getTasksOnDayMessage_anotherDay_namesThatDayInstead() {
        TaskList list = listOf(deadlineOn("return book", DEC_03));

        // The day is part of the header, so two days must not word alike.
        assertEquals("Here are the tasks on Dec 03 2019:\n"
                        + "1. [D][ ] return book (by: Dec 03 2019, 6:00 PM)",
                new Ui().getTasksOnDayMessage(list, DEC_03));
    }

    // ---- a header is shown even when nothing matches ----

    @Test
    public void getTaskListMessage_emptyList_isTheHeaderAlone() {
        assertEquals("Here are the tasks in your list:", new Ui().getTaskListMessage(listOf()));
    }

    @Test
    public void getMatchingTasksMessage_noMatches_isTheHeaderAlone() {
        TaskList list = listOf(new TodoTask("read book"));

        // Saying so beats saying nothing: an empty block would read as a fault.
        assertEquals("Here are the matching tasks in your list:",
                new Ui().getMatchingTasksMessage(list, "swim"));
    }

    @Test
    public void getTasksOnDayMessage_noTasksThatDay_isTheHeaderAlone() {
        TaskList list = listOf(deadlineOn("return book", DEC_03));

        assertEquals("Here are the tasks on Dec 02 2019:",
                new Ui().getTasksOnDayMessage(list, DEC_02));
    }

    // ---- the numbers shown are positions in the whole list ----

    @Test
    public void getMatchingTasksMessage_matchesWithGaps_numbersByListPosition() {
        TaskList list = listOf(new TodoTask("borrow book"), new TodoTask("swim"),
                new TodoTask("read book"));

        // 1 and 3, not 1 and 2: the number on screen is the number mark and
        // delete take, so the task in between has to leave a gap.
        assertEquals("Here are the matching tasks in your list:\n"
                        + "1. [T][ ] borrow book\n3. [T][ ] read book",
                new Ui().getMatchingTasksMessage(list, "book"));
    }

    @Test
    public void getTasksOnDayMessage_matchesWithGaps_numbersByListPosition() {
        TaskList list = listOf(new TodoTask("borrow book"), deadlineOn("return book", DEC_03),
                deadlineOn("pay fine", DEC_02));

        // Only the third task falls on the day, and it keeps the number 3.
        assertEquals("Here are the tasks on Dec 02 2019:\n"
                        + "3. [D][ ] pay fine (by: Dec 02 2019, 6:00 PM)",
                new Ui().getTasksOnDayMessage(list, DEC_02));
    }
}
