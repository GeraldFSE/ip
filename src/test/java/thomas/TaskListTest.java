package thomas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import thomas.task.DeadlineTask;
import thomas.task.EventTask;
import thomas.task.Task;
import thomas.task.TodoTask;

/**
 * Tests TaskList.
 * The class holds no resources and talks to nothing, so a case is a list built
 * by hand, one call and one assertion. What makes it worth testing is that two
 * numbering schemes meet here, the user counting from 1 and the list from 0, and
 * the conversion between them happens in this class and nowhere else. An
 * off-by-one would mark or delete the wrong task, which is the kind of mistake
 * that survives a run-through where every number falls in the middle.
 * The boundaries are therefore tested on both sides throughout: the first and
 * last task as well as one past each end, and the days an event starts and ends
 * as well as the days around them.
 * The size, get and add methods are one-line delegations and are not tested for
 * their own sake, appearing only as the way the other cases are set up.
 */
public class TaskListTest {

    /** Day used throughout, with the days either side of it for the range tests */
    private static final LocalDate DEC_02 = LocalDate.of(2019, 12, 2);
    private static final LocalDate DEC_03 = LocalDate.of(2019, 12, 3);
    private static final LocalDate DEC_04 = LocalDate.of(2019, 12, 4);

    /**
     * Returns a list holding the given tasks, in the order given.
     * The tasks are taken as arguments rather than added one by one in each
     * test, so that a case reads as the list it is about rather than as the
     * steps that built it.
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
     * Returns how a list's tasks show themselves, in order, for comparing lists.
     *
     * @param list List to read.
     * @return One entry per task, in list order.
     */
    private static List<String> contentsOf(TaskList list) {
        List<String> descriptions = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            descriptions.add(list.get(i).toString());
        }
        return descriptions;
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

    /**
     * Returns an event running from 2pm on one day to 4pm on another.
     *
     * @param description Task text.
     * @param start Day the event starts.
     * @param end Day the event ends.
     * @return New event.
     * @throws ThomasException If the end day falls before the start day.
     */
    private static Task eventFrom(String description, LocalDate start, LocalDate end)
            throws ThomasException {
        return new EventTask(description, start.atTime(14, 0), end.atTime(16, 0));
    }

    // ---- construction ----

    @Test
    public void constructor_noArguments_listIsEmpty() {
        assertEquals(0, new TaskList().size());
    }

    /**
     * The constructor taking tasks is how the save file is loaded, so the list
     * must start out holding exactly what it was handed, in the same order.
     */
    @Test
    public void constructor_existingTasks_holdsThemInOrder() {
        ArrayList<Task> loaded = new ArrayList<>();
        loaded.add(new TodoTask("read book"));
        loaded.add(new TodoTask("return book"));

        TaskList list = new TaskList(loaded);

        assertEquals(2, list.size());
        assertEquals(List.of("[T][ ] read book", "[T][ ] return book"), contentsOf(list));
    }

    @Test
    public void add_toEmptyList_appendsTask() {
        TaskList list = new TaskList();
        Task task = new TodoTask("read book");

        list.add(task);

        assertEquals(1, list.size());
        assertSame(task, list.get(0));
    }

    /** Tasks are kept in the order they were added, which is the order shown. */
    @Test
    public void add_toNonEmptyList_appendsToTheEnd() {
        TaskList list = listOf(new TodoTask("read book"));
        Task second = new TodoTask("return book");

        list.add(second);

        assertSame(second, list.get(1));
    }

    // ---- getByNumber ----

    /** Task 1 is position 0: the conversion this class exists to make. */
    @Test
    public void getByNumber_firstTask_returnsTaskAtPositionZero() throws ThomasException {
        Task first = new TodoTask("read book");
        TaskList list = listOf(first, new TodoTask("return book"));

        assertSame(first, list.getByNumber(1));
    }

    /** The last task is numbered size(), not size() - 1. */
    @Test
    public void getByNumber_lastTask_returnsTaskAtEnd() throws ThomasException {
        Task last = new TodoTask("return book");
        TaskList list = listOf(new TodoTask("read book"), last);

        assertSame(last, list.getByNumber(2));
    }

    @Test
    public void getByNumber_middleTask_returnsThatTask() throws ThomasException {
        Task middle = new TodoTask("return book");
        TaskList list = listOf(new TodoTask("read book"), middle, new TodoTask("buy book"));

        assertSame(middle, list.getByNumber(2));
    }

    /**
     * Zero is a position, not a task number, so it must be rejected rather than
     * quietly reaching the first task.
     */
    @Test
    public void getByNumber_zero_exceptionThrown() {
        TaskList list = listOf(new TodoTask("read book"));

        ThomasException e = assertThrows(ThomasException.class, () -> list.getByNumber(0));
        assertEquals("There is no task 0! You only have 1 task(s).", e.getMessage());
    }

    @Test
    public void getByNumber_negativeNumber_exceptionThrown() {
        TaskList list = listOf(new TodoTask("read book"));

        ThomasException e = assertThrows(ThomasException.class, () -> list.getByNumber(-1));
        assertEquals("There is no task -1! You only have 1 task(s).", e.getMessage());
    }

    /** One past the end: the number of tasks is the largest number that works. */
    @Test
    public void getByNumber_onePastEnd_exceptionThrown() {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        ThomasException e = assertThrows(ThomasException.class, () -> list.getByNumber(3));
        assertEquals("There is no task 3! You only have 2 task(s).", e.getMessage());
    }

    /**
     * The range is checked against the tasks that exist, so every number fails
     * on an empty list rather than any of them reaching the underlying list.
     */
    @Test
    public void getByNumber_emptyList_exceptionThrown() {
        TaskList list = new TaskList();

        ThomasException e = assertThrows(ThomasException.class, () -> list.getByNumber(1));
        assertEquals("There is no task 1! You only have 0 task(s).", e.getMessage());
    }

    /** Reading a task must not remove it, unlike deleteByNumber below. */
    @Test
    public void getByNumber_validNumber_listUnchanged() throws ThomasException {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        list.getByNumber(1);

        assertEquals(List.of("[T][ ] read book", "[T][ ] return book"), contentsOf(list));
    }

    // ---- deleteByNumber ----

    @Test
    public void deleteByNumber_firstTask_returnsThatTask() throws ThomasException {
        Task first = new TodoTask("read book");
        TaskList list = listOf(first, new TodoTask("return book"));

        assertSame(first, list.deleteByNumber(1));
    }

    @Test
    public void deleteByNumber_validNumber_listShrinks() throws ThomasException {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        list.deleteByNumber(1);

        assertEquals(1, list.size());
    }

    /**
     * Removing closes the gap, so the tasks after the deleted one shift down and
     * the numbering never develops holes. Deleting the middle of three is what
     * shows this: task 3 must become task 2, not stay at 3.
     */
    @Test
    public void deleteByNumber_middleTask_laterTasksShiftDown() throws ThomasException {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"),
                new TodoTask("buy book"));

        list.deleteByNumber(2);

        assertEquals(List.of("[T][ ] read book", "[T][ ] buy book"), contentsOf(list));
        assertEquals("[T][ ] buy book", list.getByNumber(2).toString());
    }

    @Test
    public void deleteByNumber_lastTask_earlierTasksKeepTheirNumbers() throws ThomasException {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        list.deleteByNumber(2);

        assertEquals(List.of("[T][ ] read book"), contentsOf(list));
        assertEquals("[T][ ] read book", list.getByNumber(1).toString());
    }

    /** Deleting task 1 repeatedly empties the list, one task at a time. */
    @Test
    public void deleteByNumber_everyTask_leavesEmptyList() throws ThomasException {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        list.deleteByNumber(1);
        list.deleteByNumber(1);

        assertEquals(0, list.size());
    }

    @Test
    public void deleteByNumber_onePastEnd_exceptionThrown() {
        TaskList list = listOf(new TodoTask("read book"));

        ThomasException e = assertThrows(ThomasException.class, () -> list.deleteByNumber(2));
        assertEquals("There is no task 2! You only have 1 task(s).", e.getMessage());
    }

    @Test
    public void deleteByNumber_zero_exceptionThrown() {
        TaskList list = listOf(new TodoTask("read book"));

        ThomasException e = assertThrows(ThomasException.class, () -> list.deleteByNumber(0));
        assertEquals("There is no task 0! You only have 1 task(s).", e.getMessage());
    }

    @Test
    public void deleteByNumber_emptyList_exceptionThrown() {
        TaskList list = new TaskList();

        ThomasException e = assertThrows(ThomasException.class, () -> list.deleteByNumber(1));
        assertEquals("There is no task 1! You only have 0 task(s).", e.getMessage());
    }

    /** A rejected delete must leave the list exactly as it was. */
    @Test
    public void deleteByNumber_numberOutOfRange_listUnchanged() {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        assertThrows(ThomasException.class, () -> list.deleteByNumber(5));

        assertEquals(List.of("[T][ ] read book", "[T][ ] return book"), contentsOf(list));
    }

    // ---- positionsOn ----

    @Test
    public void positionsOn_emptyList_returnsNoPositions() {
        assertTrue(new TaskList().positionsOn(DEC_02).isEmpty());
    }

    /** A todo carries no date, so it falls on no day at all. */
    @Test
    public void positionsOn_todosOnly_returnsNoPositions() {
        TaskList list = listOf(new TodoTask("read book"), new TodoTask("return book"));

        assertTrue(list.positionsOn(DEC_02).isEmpty());
    }

    @Test
    public void positionsOn_deadlineDueThatDay_returnsItsPosition() {
        TaskList list = listOf(deadlineOn("return book", DEC_02));

        assertEquals(List.of(0), list.positionsOn(DEC_02));
    }

    @Test
    public void positionsOn_deadlineDueAnotherDay_returnsNoPositions() {
        TaskList list = listOf(deadlineOn("return book", DEC_02));

        assertTrue(list.positionsOn(DEC_03).isEmpty());
    }

    /** The time of day is dropped, so a deadline at any hour counts as on that day. */
    @Test
    public void positionsOn_deadlineAtMidnightAndLastMinute_bothMatch() {
        TaskList list = listOf(
                new DeadlineTask("first thing", DEC_02.atTime(0, 0)),
                new DeadlineTask("last thing", DEC_02.atTime(23, 59)));

        assertEquals(List.of(0, 1), list.positionsOn(DEC_02));
    }

    /** An event covers every day it spans, so the day it starts is one of them. */
    @Test
    public void positionsOn_eventFirstDay_returnsItsPosition() throws ThomasException {
        TaskList list = listOf(eventFrom("project meeting", DEC_02, DEC_04));

        assertEquals(List.of(0), list.positionsOn(DEC_02));
    }

    /** The middle of an event matches, which the wrong range test also gets right. */
    @Test
    public void positionsOn_eventMiddleDay_returnsItsPosition() throws ThomasException {
        TaskList list = listOf(eventFrom("project meeting", DEC_02, DEC_04));

        assertEquals(List.of(0), list.positionsOn(DEC_03));
    }

    /** Both ends count, so the last day matches too -- the case a strict range test drops. */
    @Test
    public void positionsOn_eventLastDay_returnsItsPosition() throws ThomasException {
        TaskList list = listOf(eventFrom("project meeting", DEC_02, DEC_04));

        assertEquals(List.of(0), list.positionsOn(DEC_04));
    }

    @Test
    public void positionsOn_dayBeforeEventStarts_returnsNoPositions() throws ThomasException {
        TaskList list = listOf(eventFrom("project meeting", DEC_03, DEC_04));

        assertTrue(list.positionsOn(DEC_02).isEmpty());
    }

    @Test
    public void positionsOn_dayAfterEventEnds_returnsNoPositions() throws ThomasException {
        TaskList list = listOf(eventFrom("project meeting", DEC_02, DEC_03));

        assertTrue(list.positionsOn(DEC_04).isEmpty());
    }

    /**
     * The positions returned are positions in the whole list, not the count of
     * matches so far. Non-matching tasks are placed first and in between, so
     * numbering the matches 1, 2, 3 would give [0, 1] and be caught here.
     */
    @Test
    public void positionsOn_severalMatches_returnsWholeListPositions() throws ThomasException {
        TaskList list = listOf(
                new TodoTask("read book"),
                deadlineOn("return book", DEC_02),
                deadlineOn("pay fine", DEC_04),
                eventFrom("project meeting", DEC_02, DEC_02));

        assertEquals(List.of(1, 3), list.positionsOn(DEC_02));
    }

    /** Matches come back in list order, whatever order the dates fall in. */
    @Test
    public void positionsOn_matchesOutOfDateOrder_returnsPositionsInListOrder() {
        TaskList list = listOf(
                new DeadlineTask("evening", DEC_02.atTime(20, 0)),
                new DeadlineTask("morning", DEC_02.atTime(8, 0)));

        assertEquals(List.of(0, 1), list.positionsOn(DEC_02));
    }

    /** Asking which tasks fall on a day must not change the list. */
    @Test
    public void positionsOn_anyDay_listUnchanged() {
        TaskList list = listOf(new TodoTask("read book"), deadlineOn("return book", DEC_02));

        list.positionsOn(DEC_02);

        assertEquals(List.of("[T][ ] read book",
                "[D][ ] return book (by: Dec 02 2019, 6:00 PM)"), contentsOf(list));
    }
}
