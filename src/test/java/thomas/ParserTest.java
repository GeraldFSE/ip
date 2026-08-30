package thomas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import thomas.command.AddCommand;
import thomas.command.DeleteCommand;
import thomas.command.ExitCommand;
import thomas.command.FindCommand;
import thomas.command.ListCommand;
import thomas.command.MarkCommand;
import thomas.command.OnCommand;
import thomas.command.UnmarkCommand;

/**
 * Tests the parse method.
 * It is tested ahead of any other method because it is a pure function of one
 * string: it reads no file, prints nothing and keeps no state between calls, so
 * a test can hand it a line and check what comes back without any setting up or
 * tearing down. It is also where every rule about the shape of the input lives,
 * which makes it the one place a typo in a separator or a message is caught.
 * Two things are checked of each line. A line that is understood must produce
 * the right kind of command, and a line that is not must fail with the message
 * the user is meant to read, so these tests pin the wording as well as the fact
 * of the failure. The commands keep their parsed arguments private, so the tests
 * assert the command's type and leave what it does with those arguments to the
 * text-UI tests.
 */
public class ParserTest {

    // ---- commands taking no argument ----

    @Test
    public void parse_byeCommand_returnsExitCommand() throws ThomasException {
        assertInstanceOf(ExitCommand.class, Parser.parse("bye"));
    }

    @Test
    public void parse_listCommand_returnsListCommand() throws ThomasException {
        assertInstanceOf(ListCommand.class, Parser.parse("list"));
    }

    @Test
    public void parse_listWithTrailingText_returnsListCommand() throws ThomasException {
        // A keyword taking no argument ignores whatever follows it, because the argument is only read by the commands
        // that need one.
        assertInstanceOf(ListCommand.class, Parser.parse("list everything"));
    }

    // ---- unrecognized input ----

    @Test
    public void parse_unknownKeyword_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("blah"));
        assertEquals("Erm sorry, what does that mean again?", e.getMessage());
    }

    @Test
    public void parse_emptyInput_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse(""));
        assertEquals("Erm sorry, what does that mean again?", e.getMessage());
    }

    @Test
    public void parse_wrongCaseKeyword_exceptionThrown() {
        // Matching is case sensitive, so "List" is not the list command.
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("List"));
        assertEquals("Erm sorry, what does that mean again?", e.getMessage());
    }

    // ---- to-do ----

    @Test
    public void parse_todoWithDescription_returnsAddCommand() throws ThomasException {
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book"));
    }

    @Test
    public void parse_todoWithoutDescription_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("todo"));
        assertEquals("HEYY!! The description of a todo cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_todoBlankDescription_exceptionThrown() {
        // A keyword followed by spaces only splits into two parts, so the blank check rather than the length check is
        // what has to catch this.
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("todo    "));
        assertEquals("HEYY!! The description of a todo cannot be empty!", e.getMessage());
    }

    // ---- deadline ----

    @Test
    public void parse_deadlineWithDescriptionAndDate_returnsAddCommand() throws ThomasException {
        assertInstanceOf(AddCommand.class, Parser.parse("deadline return book /by 2019-12-02 1800"));
    }

    @Test
    public void parse_deadlineWithoutArguments_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("deadline"));
        assertEquals("HEYY!! The description of a deadline cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_deadlineWithoutByMarker_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline return book 2019-12-02 1800"));
        assertEquals("Are you forgetting something!! When is the deadline!", e.getMessage());
    }

    @Test
    public void parse_deadlineDescriptionEndingInBy_exceptionThrown() {
        // The separator carries a leading space so that a word merely ending in "by" is not mistaken for the marker.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline put it on standby 2019-12-02 1800"));
        assertEquals("Are you forgetting something!! When is the deadline!", e.getMessage());
    }

    @Test
    public void parse_deadlineMarkerOnly_exceptionThrown() {
        // A line that is nothing but the marker has no space in front of it once trimmed, so the split fails even
        // though the marker is there: the missing part is the description.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline /by 2019-12-02 1800"));
        assertEquals("HEYY!! The description of a deadline cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_deadlineBlankDescription_exceptionThrown() {
        // Here the split does succeed, and the empty description is caught afterwards.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline    /by 2019-12-02 1800"));
        assertEquals("HEYY!! The description of a deadline cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_deadlineBlankDate_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline return book /by   "));
        assertEquals("Are you forgetting something!! When is the deadline!", e.getMessage());
    }

    @Test
    public void parse_deadlineUnreadableDate_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline return book /by tomorrow"));
        assertEquals("I can't read 'tomorrow' as a deadline date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    @Test
    public void parse_deadlineDateWithoutTime_exceptionThrown() {
        // A date is not enough on its own: the time is required too.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline return book /by 2019-12-02"));
        assertEquals("I can't read '2019-12-02' as a deadline date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    // ---- event ----

    @Test
    public void parse_eventWithBothDates_returnsAddCommand() throws ThomasException {
        assertInstanceOf(AddCommand.class, Parser.parse(
                "event project meeting /from 2019-12-02 1400 /to 2019-12-02 1600"));
    }

    @Test
    public void parse_eventWithoutArguments_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("event"));
        assertEquals("HEYY!! The description of an event cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_eventWithoutFromMarker_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /to 2019-12-02 1600"));
        assertEquals("Erm when does it start? You need a /from!", e.getMessage());
    }

    @Test
    public void parse_eventWithoutToMarker_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from 2019-12-02 1400"));
        assertEquals("Erm when does it end? You need a /to after your /from!", e.getMessage());
    }

    @Test
    public void parse_eventMarkersInWrongOrder_exceptionThrown() {
        // The markers are split off one at a time, so writing them the wrong way round is rejected rather than being
        // silently swapped.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /to 2019-12-02 1600 /from 2019-12-02 1400"));
        assertEquals("Erm when does it end? You need a /to after your /from!", e.getMessage());
    }

    @Test
    public void parse_eventMarkerOnly_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event /from 2019-12-02 1400 /to 2019-12-02 1600"));
        assertEquals("HEYY!! The description of an event cannot be empty!", e.getMessage());
    }

    @Test
    public void parse_eventBlankStartDate_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from   /to 2019-12-02 1600"));
        assertEquals("Erm when does it start? You need a /from!", e.getMessage());
    }

    @Test
    public void parse_eventBlankEndDate_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from 2019-12-02 1400 /to   "));
        assertEquals("Erm when does it end? You need a /to after your /from!", e.getMessage());
    }

    @Test
    public void parse_eventUnreadableStartDate_exceptionThrown() {
        // Each date names itself in its own message, so the user knows which one to fix.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from Mon 2pm /to 2019-12-02 1600"));
        assertEquals("I can't read 'Mon 2pm' as a start date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    @Test
    public void parse_eventUnreadableEndDate_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from 2019-12-02 1400 /to 4pm"));
        assertEquals("I can't read '4pm' as an end date! "
                + "Write it as a date and a 24-hour time, like 2019-12-02 1800.", e.getMessage());
    }

    @Test
    public void parse_eventEndingBeforeItStarts_exceptionThrown() {
        // An event running backwards is refused, though not by the parser. The check is the event constructor, so that
        // loading the save file is held to it too, and the parser lets the exception through.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event project meeting /from 2019-12-02 1600 /to 2019-12-02 1400"));
        assertEquals("HUH?! Your event ends before it starts! Check your /from and /to.",
                e.getMessage());
    }

    @Test
    public void parse_eventStartingWhenItEnds_returnsAddCommand() throws ThomasException {
        // An event lasting no time is odd but says nothing false, so it is allowed.
        assertInstanceOf(AddCommand.class, Parser.parse(
                "event project meeting /from 2019-12-02 1400 /to 2019-12-02 1400"));
    }

    // ---- descriptions holding the save file's field separator ----

    @Test
    public void parse_todoDescriptionWithSeparator_exceptionThrown() {
        // A description containing " | " is refused as it is typed, because it could not survive being saved: the task
        // would be accepted and listed, then lost on the next run. All three add commands are checked, since the rule
        // is one helper called from three places rather than one inherited check.
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("todo read book | and return it"));
        assertEquals("HEYY!! A description can't contain ' | ' -- "
                + "that's how I keep your tasks in the save file.", e.getMessage());
    }

    @Test
    public void parse_deadlineDescriptionWithSeparator_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("deadline read book | and return it /by 2019-12-02 1800"));
        assertEquals("HEYY!! A description can't contain ' | ' -- "
                + "that's how I keep your tasks in the save file.", e.getMessage());
    }

    @Test
    public void parse_eventDescriptionWithSeparator_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () ->
                Parser.parse("event talk | and lunch /from 2019-12-02 1400 /to 2019-12-02 1600"));
        assertEquals("HEYY!! A description can't contain ' | ' -- "
                + "that's how I keep your tasks in the save file.", e.getMessage());
    }

    @Test
    public void parse_todoDescriptionWithBarePipe_returnsAddCommand() throws ThomasException {
        // Only the separator as the save file writes it is refused. A bare pipe saves and loads back correctly, so
        // refusing it too would take away a character the format has no trouble with.
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book|and return it"));
    }

    @Test
    public void parse_todoDescriptionWithHalfSpacedPipe_returnsAddCommand() throws ThomasException {
        // A pipe with a space on one side only is likewise not the separator.
        assertInstanceOf(AddCommand.class, Parser.parse("todo read book |and return it"));
    }

    // ---- mark, unmark and delete ----

    @Test
    public void parse_markWithNumber_returnsMarkCommand() throws ThomasException {
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 2"));
    }

    @Test
    public void parse_unmarkWithNumber_returnsUnmarkCommand() throws ThomasException {
        assertInstanceOf(UnmarkCommand.class, Parser.parse("unmark 2"));
    }

    @Test
    public void parse_deleteWithNumber_returnsDeleteCommand() throws ThomasException {
        assertInstanceOf(DeleteCommand.class, Parser.parse("delete 2"));
    }

    @Test
    public void parse_markNumberOutsideList_returnsMarkCommand() throws ThomasException {
        // Whether a task carries that number is the list's to answer, so a number outside the list still parses; only
        // the digits are checked here.
        assertInstanceOf(MarkCommand.class, Parser.parse("mark 999"));
    }

    @Test
    public void parse_markWithoutNumber_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("mark"));
        assertEquals("HEYY!! You need a valid number to mark", e.getMessage());
    }

    @Test
    public void parse_deleteWithoutNumber_exceptionThrown() {
        // Each of the three names itself in the missing-argument message.
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("delete"));
        assertEquals("HEYY!! You need a valid number to delete", e.getMessage());
    }

    @Test
    public void parse_markNonInteger_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("mark two"));
        assertEquals("WHAT? Why are you passing a non integer?! Give me an INTEGER!!", e.getMessage());
    }

    @Test
    public void parse_markDecimalNumber_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("mark 1.5"));
        assertEquals("WHAT? Why are you passing a non integer?! Give me an INTEGER!!", e.getMessage());
    }

    // ---- find ----

    @Test
    public void parse_findWithKeyword_returnsFindCommand() throws ThomasException {
        assertInstanceOf(FindCommand.class, Parser.parse("find book"));
    }

    /**
     * The whole argument is the keyword, so a search of several words looks for
     * the phrase rather than for any one of them. Splitting on spaces here would
     * quietly change what the user asked for.
     */
    @Test
    public void parse_findWithSeveralWords_returnsFindCommand() throws ThomasException {
        assertInstanceOf(FindCommand.class, Parser.parse("find read book"));
    }

    @Test
    public void parse_findWithoutKeyword_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("find"));
        assertEquals("HEYY!! What am I looking for? Give me a keyword!", e.getMessage());
    }

    /** A keyword of spaces alone is as missing as no keyword at all. */
    @Test
    public void parse_findBlankKeyword_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("find    "));
        assertEquals("HEYY!! What am I looking for? Give me a keyword!", e.getMessage());
    }

    // ---- on ----

    @Test
    public void parse_onWithDay_returnsOnCommand() throws ThomasException {
        assertInstanceOf(OnCommand.class, Parser.parse("on 2019-12-02"));
    }

    @Test
    public void parse_onWithoutDay_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("on"));
        assertEquals("HEYY!! Which day do you want to see?", e.getMessage());
    }

    @Test
    public void parse_onUnreadableDay_exceptionThrown() {
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("on tomorrow"));
        assertEquals("I can't read 'tomorrow' as a day! Write it as 2019-12-02.", e.getMessage());
    }

    @Test
    public void parse_onDayWithTime_exceptionThrown() {
        // The on command asks about a whole day, so a time is refused.
        ThomasException e = assertThrows(ThomasException.class, () -> Parser.parse("on 2019-12-02 1800"));
        assertEquals("I can't read '2019-12-02 1800' as a day! Write it as 2019-12-02.", e.getMessage());
    }
}
