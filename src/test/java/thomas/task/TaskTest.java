package thomas.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import thomas.ThomasException;

/**
 * Tests Task itself: the date reading and formats every task type shares, and
 * the completion state they all carry.
 * The parseDate method is tested hardest, since both the command handling and
 * the save file loader go through it, so a mistake there is felt at the
 * keyboard and in the save file at once. Its failures are already seen through
 * the parser, but only its failures, since the parser puts what comes back into
 * a private field.
 * The display formats are pinned for the same reason. They are what the user
 * reads, and a pattern is easy to get subtly wrong in a way that still compiles
 * and still produces something date-shaped.
 */
public class TaskTest {

    // ---- parseDate: dates it accepts ----

    @Test
    public void parseDate_dateAndTime_returnsThatMoment() throws ThomasException {
        assertEquals(LocalDateTime.of(2019, 12, 2, 18, 0),
                Task.parseDate("2019-12-02 1800", "a deadline date"));
    }

    @Test
    public void parseDate_afternoonTime_readAsTwentyFourHourClock() throws ThomasException {
        // HH is the 24-hour hour, so 1800 is the evening and not 6am.
        assertEquals(18, Task.parseDate("2019-12-02 1800", "a deadline date").getHour());
    }

    @Test
    public void parseDate_midnight_returnsStartOfDay() throws ThomasException {
        assertEquals(LocalDateTime.of(2019, 12, 2, 0, 0),
                Task.parseDate("2019-12-02 0000", "a deadline date"));
    }

    @Test
    public void parseDate_lastMinuteOfDay_returnsThatMoment() throws ThomasException {
        assertEquals(LocalDateTime.of(2019, 12, 2, 23, 59),
                Task.parseDate("2019-12-02 2359", "a deadline date"));
    }

    @Test
    public void parseDate_monthAndMinutesDiffer_bothReadCorrectly() throws ThomasException {
        // mm is minutes and MM is months. Reading a date whose month and minutes differ is what would catch the two
        // being swapped in the pattern; a date such as 2019-12-12 1212 would not.
        LocalDateTime moment = Task.parseDate("2019-03-04 0745", "a deadline date");

        assertEquals(3, moment.getMonthValue());
        assertEquals(45, moment.getMinute());
        assertEquals(4, moment.getDayOfMonth());
        assertEquals(7, moment.getHour());
    }

    // ---- parseDate: dates it refuses ----

    @Test
    public void parseDate_dateWithoutTime_exceptionThrown() {
        // A date alone is refused rather than assumed to mean midnight.
        assertThrows(ThomasException.class, () -> Task.parseDate("2019-12-02", "a deadline date"));
    }

    @Test
    public void parseDate_timeWithoutDate_exceptionThrown() {
        assertThrows(ThomasException.class, () -> Task.parseDate("1800", "a deadline date"));
    }

    @Test
    public void parseDate_unpaddedTime_exceptionThrown() {
        // The hour and minute are four digits, so a three-digit time is not a time.
        assertThrows(ThomasException.class, () -> Task.parseDate("2019-12-02 800", "a deadline date"));
    }

    @Test
    public void parseDate_twelveHourTime_exceptionThrown() {
        assertThrows(ThomasException.class, () -> Task.parseDate("2019-12-02 6pm", "a deadline date"));
    }

    @Test
    public void parseDate_wordsInsteadOfDate_exceptionThrown() {
        assertThrows(ThomasException.class, () -> Task.parseDate("tomorrow", "a deadline date"));
    }

    @Test
    public void parseDate_emptyText_exceptionThrown() {
        assertThrows(ThomasException.class, () -> Task.parseDate("", "a deadline date"));
    }

    @Test
    public void parseDate_impossibleMonth_exceptionThrown() {
        // A well-formed date that does not exist is still refused.
        assertThrows(ThomasException.class, () -> Task.parseDate("2019-13-02 1800", "a deadline date"));
    }

    @Test
    public void parseDate_dayOutsideItsMonth_clampedToLastDayOfMonth() throws ThomasException {
        // A day that does not exist in its month is NOT refused, but quietly moved to the last day of that month,
        // since the date formatter resolves smartly unless told otherwise. This is recorded rather than asserted away,
        // since it is the behavior the user meets today: "deadline submit /by 2019-02-30 1800" is accepted and stored
        // as the 28th, with nothing said. Resolving strictly instead would make this throw, and this case would then
        // confirm the change worked.
        assertEquals(LocalDateTime.of(2019, 2, 28, 18, 0),
                Task.parseDate("2019-02-30 1800", "a deadline date"));
    }

    @Test
    public void parseDate_hourTwentyFour_rollsToNextMidnight() throws ThomasException {
        // Smart resolution likewise rolls hour 24 forward to midnight the next day.
        assertEquals(LocalDateTime.of(2019, 12, 3, 0, 0),
                Task.parseDate("2019-12-02 2400", "a deadline date"));
    }

    @Test
    public void parseDate_unreadableDate_messageNamesTheFieldAndTheText() {
        // The field name is passed in so each date can name itself, and carries its own article so the message reads
        // properly for all three.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Task.parseDate("Mon 2pm", "a start date"));

        assertEquals("I can't read 'Mon 2pm' as a start date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    @Test
    public void parseDate_unreadableEndDate_messageNamesThatField() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Task.parseDate("4pm", "an end date"));

        assertEquals("I can't read '4pm' as an end date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    // ---- the formats ----

    @Test
    public void inputFormat_writingThenReading_returnsTheSameMoment() throws ThomasException {
        // Reading and writing go through the same format, which is what makes whatever is saved something the parser
        // can read back. This holds the two together: if the input format changed, this would fail rather than the
        // save file quietly becoming unreadable.
        LocalDateTime moment = LocalDateTime.of(2019, 12, 2, 18, 0);

        String written = moment.format(Task.DATE_INPUT_FORMAT);

        assertEquals("2019-12-02 1800", written);
        assertEquals(moment, Task.parseDate(written, "a deadline date"));
    }

    @Test
    public void displayFormat_afternoonMoment_showsShortMonthAndTwelveHourTime() {
        // What the user reads: deliberately not the format the date is typed in.
        assertEquals("Dec 02 2019, 6:00 PM",
                LocalDateTime.of(2019, 12, 2, 18, 0).format(Task.DATE_DISPLAY_FORMAT));
    }

    @Test
    public void displayFormat_morningMoment_showsAmMarker() {
        assertEquals("Mar 04 2019, 7:45 AM",
                LocalDateTime.of(2019, 3, 4, 7, 45).format(Task.DATE_DISPLAY_FORMAT));
    }

    @Test
    public void displayDayFormat_wholeDay_showsNoTime() {
        // A whole day has no hour to show, so it has a format of its own.
        assertEquals("Dec 02 2019", LocalDate.of(2019, 12, 2).format(Task.DATE_DISPLAY_DAY));
    }

    // ---- completion state ----

    @Test
    public void getStatusIcon_newTask_isNotDone() {
        assertEquals("[ ]", new Task("read book").getStatusIcon());
    }

    @Test
    public void markAsDone_notDoneTask_becomesDone() {
        Task task = new Task("read book");

        task.markAsDone();

        assertEquals("[X]", task.getStatusIcon());
    }

    @Test
    public void markAsDone_alreadyDoneTask_staysDone() {
        // Marking a task that is already done leaves it done rather than toggling.
        Task task = new Task("read book");

        task.markAsDone();
        task.markAsDone();

        assertEquals("[X]", task.getStatusIcon());
    }

    @Test
    public void unmarkAsDone_doneTask_becomesNotDone() {
        Task task = new Task("read book");
        task.markAsDone();

        task.unmarkAsDone();

        assertEquals("[ ]", task.getStatusIcon());
    }

    @Test
    public void unmarkAsDone_notDoneTask_staysNotDone() {
        // Likewise, unmarking a task that was never done is not a toggle.
        Task task = new Task("read book");

        task.unmarkAsDone();

        assertEquals("[ ]", task.getStatusIcon());
    }

    // ---- display and save format ----

    @Test
    public void toString_notDoneTask_showsEmptyBoxThenDescription() {
        assertEquals("[ ] read book", new Task("read book").toString());
    }

    @Test
    public void toString_doneTask_showsTickedBox() {
        Task task = new Task("read book");
        task.markAsDone();

        assertEquals("[X] read book", task.toString());
    }

    @Test
    public void toSaveFormat_notDoneTask_writesZeroThenDescription() {
        // The save format writes the completion state as a digit rather than as the icon, so the loader never has to
        // strip brackets, and stays separate from the display format so that restyling it cannot break loading.
        assertEquals("0 | read book", new Task("read book").toSaveFormat());
    }

    @Test
    public void toSaveFormat_doneTask_writesOne() {
        Task task = new Task("read book");
        task.markAsDone();

        assertEquals("1 | read book", task.toSaveFormat());
    }

    // ---- occursOn ----

    @Test
    public void occursOn_plainTask_isNeverOnAnyDay() {
        // A plain task carries no date, so it falls on no day. The dated subclasses override this, which is what lets
        // the on command filter the list without asking any task what type it is.
        Task task = new Task("read book");

        assertFalse(task.occursOn(LocalDate.of(2019, 12, 2)));
        assertFalse(task.occursOn(LocalDate.of(1999, 1, 1)));
    }

    @Test
    public void occursOn_todo_isNeverOnAnyDay() {
        assertFalse(new TodoTask("read book").occursOn(LocalDate.of(2019, 12, 2)));
    }

    // ---- TodoTask ----

    @Test
    public void todoToString_notDoneTodo_isTaggedT() {
        assertEquals("[T][ ] read book", new TodoTask("read book").toString());
    }

    @Test
    public void todoToString_doneTodo_isTaggedTAndTicked() {
        Task todo = new TodoTask("read book");
        todo.markAsDone();

        assertEquals("[T][X] read book", todo.toString());
    }

    @Test
    public void todoToSaveFormat_notDoneTodo_isTaggedT() {
        assertEquals("T | 0 | read book", new TodoTask("read book").toSaveFormat());
    }

    @Test
    public void todoToSaveFormat_doneTodo_writesTheDoneFlag() {
        Task todo = new TodoTask("read book");
        todo.markAsDone();

        assertTrue(todo.toSaveFormat().startsWith("T | 1 | "));
    }
}
