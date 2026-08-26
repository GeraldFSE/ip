# Thomas text-UI test plan

Test cases for the chatbot's text user interface. Each case is run by the
`test-ui` skill, which feeds the case's input lines to the program on standard
input and compares the console output against the expected output.

## How a case is run

* Every case runs the program **from scratch** in its own process, in its own
  throwaway working directory. A case never sees tasks added by an earlier case,
  whether through memory or through the save file the chatbot writes to
  `./data`, so cases can be reordered or run one at a time without their results
  changing. A case that needs existing tasks adds them itself, as part of its
  own input.
* A case may give **more than one `**Input:**` block**. Each block is a separate
  run of the chatbot, and the runs share that one working directory, so a later
  run starts from the save file the earlier one left behind. The expected output
  is the runs' output end to end, greeting and farewell included for each. This
  is the only way to test saving and loading: a reload only happens when the
  program is started again.
* The comparison is line by line over the program's whole console output,
  including the greeting and the farewell. Indentation is compared; trailing
  spaces at the end of a line and blank lines at the end of the output are not.
* Anything the program writes to standard error (such as a stack trace) counts
  as part of the output, and a non-zero exit status fails the case.
* The session stops at the first failing case.

## How to add a case

Copy an existing case. A case is a `###` heading under `## Test cases` with
three parts: `**Aim:**`, an `**Input:**` code block, and an
`**Expected output:**` code block. In the expected output, a line reading
`{{GREETING}}` or `{{FAREWELL}}` expands to the corresponding block below, so
the banner does not have to be repeated in every case.

A case that needs the chatbot restarted gives a second `**Input:**` block after
the first; its expected output then covers both runs in order, each with its own
greeting and farewell.

## Shared output blocks

### GREETING

```text
    ____________________________________________________________
       ________
      /_  __/ /_  ____  ____ ___  ____ ______
       / / / __ \/ __ \/ __ `__ \/ __ `/ ___/
      / / / / / / /_/ / / / / / / /_/ (__  )
     /_/ /_/ /_/\____/_/ /_/ /_/\__,_/____/
     Choo Choo! I'm Thomas!
     How can I serve you today?
    ____________________________________________________________
```

### FAREWELL

```text
    ____________________________________________________________
     Until next time! Choo Choo!
    ____________________________________________________________
```

## Test cases

### TC1: Greet and exit

**Aim:** The chatbot shows its banner on startup and says goodbye when the user
types `bye`, with nothing in between.

**Input:**

```text
bye
```

**Expected output:**

```text
{{GREETING}}
{{FAREWELL}}
```

### TC2: Added tasks are listed in the order they were added

**Aim:** `list` prints the stored tasks numbered from 1, in the order they were
added, each not done. Guards the numbering loop.

**Input:**

```text
todo read book
todo return book
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [T][ ] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC3: List with no tasks stored

**Aim:** `list` before anything has been added prints the header and nothing
else, rather than failing. Guards the numbering loop against an empty task list,
and the header slot against an array sized for zero tasks.

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
{{FAREWELL}}
```

### TC4: Mark a task as done

**Aim:** `mark <n>` sets task n's status to done, echoes the task back, and the
change is visible in a later `list`. Only the named task changes.

**Input:**

```text
todo read book
todo return book
mark 2
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] return book
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [T][X] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC5: Unmark a task that was done

**Aim:** `unmark <n>` returns a task that was marked done to not done, and the
change is visible in a later `list`.

**Input:**

```text
todo read book
mark 1
unmark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     OK, I've marked this task as not done yet:
        [T][ ] read book
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC6: Marking a task twice leaves it done

**Aim:** `mark` is not a toggle -- marking an already-done task keeps it done.
This is the behaviour that makes `unmark` the only way back.

**Input:**

```text
todo read book
mark 1
mark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC7: Input ends without a `bye`

**Aim:** The chatbot still says goodbye when standard input runs out rather than
ending on a `bye`. Guards the `hasNextLine()` check that ends the read loop.

**Input:**

```text
todo read book
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
```

### TC8: Add one task of each type and list them

**Aim:** `todo`, `deadline` and `event` each store a task of the matching type,
acknowledge it with the running task count, and show their own tag and date
details in a later `list`. The dates are typed as `yyyy-mm-dd HHmm` on a 24-hour
clock and shown back as `MMM dd yyyy, h:mm a` on a 12-hour one, so this also
guards the input and display formats being deliberately different ones.

**Input:**

```text
todo borrow book
deadline return book /by 2019-12-02 1800
event project meeting /from 2019-12-02 1400 /to 2019-12-04 1600
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] borrow book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
     Now you have 3 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] borrow book
     2. [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     3. [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC9: A `/by` that is not a date is rejected

**Aim:** A deadline is now a real date and time, not free text, so text that is
neither is reported rather than stored. Guards the `DateTimeParseException` from
`LocalDateTime.parse` being rethrown as a `ThomasException` instead of reaching
the user as a stack trace, and the message quoting back what was rejected.

The date is the last thing checked, so the whole `/by` text is passed to the
parser: this is also what shows that splitting on `/by` kept the spaces and
punctuation rather than trimming the argument down to its first word.

**Input:**

```text
deadline do homework /by no idea :-p
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     I can't read 'no idea :-p' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
{{FAREWELL}}
```

### TC9a: A date given without a time is rejected

**Aim:** The time is required, not optional. A bare `yyyy-mm-dd` is refused
rather than being quietly taken to mean midnight, so a task never claims a time
the user did not choose. This is the case that separates "the date must parse"
from "the date and the time must both be there" -- a parser accepting the date
half alone would let this through and store `12:00 AM`.

**Input:**

```text
deadline return book /by 2019-12-02
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     I can't read '2019-12-02' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
{{FAREWELL}}
```

### TC9b: A near-miss date is rejected as firmly as nonsense

**Aim:** Only the one accepted form gets through -- a date written the other way
round, or naming a month that does not exist, is refused rather than guessed at.
`2/12/2019 1800` is the shape the user is most likely to reach for, and
`2019-13-01 1800` matches the pattern exactly while naming a thirteenth month,
which a check on shape alone would let through.

**Input:**

```text
deadline return book /by 2/12/2019 1800
deadline return book /by 2019-13-01 1800
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     I can't read '2/12/2019 1800' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
    ____________________________________________________________
     I can't read '2019-13-01 1800' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
{{FAREWELL}}
```

### TC9c: A time outside the 24-hour clock is rejected

**Aim:** The time is read on a 24-hour clock, so `2500` is not a time and
`1860` is not a minute count. Guards the `HH` and `mm` halves of the pattern
against a value of the right width but the wrong range, which is what a check
counting only digits would miss.

**Input:**

```text
deadline return book /by 2019-12-02 2500
deadline return book /by 2019-12-02 1860
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     I can't read '2019-12-02 2500' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
    ____________________________________________________________
     I can't read '2019-12-02 1860' as a deadline date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
{{FAREWELL}}
```

### TC9d: An event's start and end are reported separately when unreadable

**Aim:** The two dates of an event are named individually in the error, so the
user is told which one to fix. Guards the `field` argument passed to the date
parser at each of its call sites; one shared wording would make these two
messages identical.

**Input:**

```text
event project meeting /from Mon 2pm /to 2019-12-04 1600
event project meeting /from 2019-12-02 1400 /to 4pm
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     I can't read 'Mon 2pm' as a start date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
    ____________________________________________________________
     I can't read '4pm' as an end date! Write it as a date and a 24-hour time, like 2019-12-02 1800.
    ____________________________________________________________
{{FAREWELL}}
```

### TC9e: Midnight and noon are shown the right way round

**Aim:** `0000` and `1200` are the two values a 24-hour to 12-hour conversion
gets wrong most easily -- midnight is `12:00 AM`, not `0:00 AM`, and noon is
`12:00 PM`, not `12:00 AM`. Guards the `h` and `a` halves of the display format
at the only two points where they disagree with the input.

**Input:**

```text
deadline sleep /by 2019-12-02 0000
deadline lunch /by 2019-12-02 1200
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] sleep (by: Dec 02 2019, 12:00 AM)
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] lunch (by: Dec 02 2019, 12:00 PM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [D][ ] sleep (by: Dec 02 2019, 12:00 AM)
     2. [D][ ] lunch (by: Dec 02 2019, 12:00 PM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC10: Marking a typed task keeps its type

**Aim:** `mark` works the same on the new task types, and the type tag survives
the status change -- the tag comes from the subclass, the `[X]` from `Task`.

**Input:**

```text
todo join sports club
mark 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] join sports club
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] join sports club
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

### TC11: An unknown command is rejected

**Aim:** A command that is not a known keyword is reported as unknown rather
than stored as a task. Guards the `else` at the end of the command chain.

**Input:**

```text
blah
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Erm sorry, what does that mean again?
    ____________________________________________________________
{{FAREWELL}}
```

### TC12: A todo with no description is rejected

**Aim:** `todo` typed on its own is reported instead of crashing on the missing
argument. Guards the `parts.length < 2` check before `parts[1]` is read.

**Input:**

```text
todo
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     HEYY!! The description of a todo cannot be empty!
    ____________________________________________________________
{{FAREWELL}}
```

### TC13: A todo whose description is only spaces is rejected

**Aim:** A description of nothing but spaces is treated as empty. This is the
case the length check alone misses -- `todo    ` splits into two parts, so only
the `isBlank()` half of the guard catches it.

**Input:**

```text
todo    
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     HEYY!! The description of a todo cannot be empty!
    ____________________________________________________________
{{FAREWELL}}
```

### TC14: A deadline with no `/by` is rejected

**Aim:** A deadline missing its due date is reported rather than crashing on
the second half of the split. Guards `details.length < 2`.

**Input:**

```text
deadline return book
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Are you forgetting something!! When is the deadline!
    ____________________________________________________________
{{FAREWELL}}
```

### TC15: An event with neither marker is rejected

**Aim:** `event` with no `/from` at all is reported. This is the case an
equality check on the split length misses: with no marker the split yields one
part, not two, so the guard has to be `<` rather than `==`.

**Input:**

```text
event project meeting
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Erm when does it start? You need a /from!
    ____________________________________________________________
{{FAREWELL}}
```

### TC16: An event with `/from` but no `/to` is rejected

**Aim:** A missing end date is reported specifically, rather than being lumped
in with a missing start date. Guards the second of the two splits.

The start given here, `Mon 2pm`, is not a valid date either, so this also pins
down the order of the checks: the missing `/to` is reported first because the
dates are only parsed once both markers have been found. Reordering those would
show the "I can't read 'Mon 2pm'" message instead and hide the real mistake.

**Input:**

```text
event project meeting /from Mon 2pm
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Erm when does it end? You need a /to after your /from!
    ____________________________________________________________
{{FAREWELL}}
```

### TC17: `mark` with a non-numeric argument is rejected

**Aim:** A task number that is not a number is reported. Guards the
`NumberFormatException` from `Integer.parseInt` being rethrown as a
`ThomasException` instead of reaching the user as a stack trace.

**Input:**

```text
mark abc
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     WHAT? Why are you passing a non integer?! Give me an INTEGER!!
    ____________________________________________________________
{{FAREWELL}}
```

### TC18: `mark` past the end of the list is rejected

**Aim:** A task number higher than the number of stored tasks is reported. The
range is checked against the tasks that exist, not the array length -- task 9 is
a valid index into the 100-slot array but holds nothing, so a check against the
array would fail later with a `NullPointerException`.

**Input:**

```text
todo read book
mark 9
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     There is no task 9! You only have 1 task(s).
    ____________________________________________________________
{{FAREWELL}}
```

### TC19: `mark 0` is rejected

**Aim:** Task numbers start at 1, so 0 is out of range. Guards the lower half
of the range check, which a check for "too high" alone would let through into a
negative array index.

**Input:**

```text
todo read book
mark 0
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     There is no task 0! You only have 1 task(s).
    ____________________________________________________________
{{FAREWELL}}
```

### TC20: The chatbot keeps going after an error

**Aim:** An error ends only the command that caused it. The next command is
read and carried out normally, which is the point of catching the exception
inside the read loop rather than outside it.

**Input:**

```text
blah
todo read book
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Erm sorry, what does that mean again?
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC21: A rejected command does not change the task list

**Aim:** A command that throws stores nothing and leaves the count alone --
`taskCount++` sits after the branch that can throw, so a later `list` shows
only the tasks that were accepted.

**Input:**

```text
todo read book
deadline return book
event project meeting
todo
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Are you forgetting something!! When is the deadline!
    ____________________________________________________________
    ____________________________________________________________
     Erm when does it start? You need a /from!
    ____________________________________________________________
    ____________________________________________________________
     HEYY!! The description of a todo cannot be empty!
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC22: A rejected add does not disturb the numbering of later tasks

**Aim:** A failed add between two successful ones leaves no gap. The second
accepted task becomes task 2, not task 3 -- `taskCount` is only advanced by an
add that completed.

**Input:**

```text
todo read book
deadline return book
todo join sports club
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Are you forgetting something!! When is the deadline!
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] join sports club
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [T][ ] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

### TC23: A rejected `mark` leaves every task's status alone

**Aim:** A successful `mark` followed by two failing ones changes only the task
that was named. Guards against a rejected command touching a task's `isDone` on
its way to being rejected.

**Input:**

```text
todo read book
todo return book
mark 1
mark 5
mark abc
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     There is no task 5! You only have 2 task(s).
    ____________________________________________________________
    ____________________________________________________________
     WHAT? Why are you passing a non integer?! Give me an INTEGER!!
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] read book
     2. [T][ ] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC24: A rejected `unmark` leaves a done task done

**Aim:** The mirror of TC23 for `unmark` -- a task marked done stays done when a
later `unmark` is rejected, whether the number is out of range or missing.

**Input:**

```text
todo read book
mark 1
unmark 9
unmark
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     There is no task 9! You only have 1 task(s).
    ____________________________________________________________
    ____________________________________________________________
     HEYY!! You need a valid number to unmark
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC25: A task number rejected as out of range is accepted once the task exists

**Aim:** The range check follows the real task count rather than a stale one.
The same `mark 2` fails with one task stored and succeeds after a second is
added, and the message reports the count at the time it was refused.

**Input:**

```text
todo read book
mark 2
todo return book
mark 2
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     There is no task 2! You only have 1 task(s).
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] return book
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [T][X] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC26: Marker words inside a description are left alone

**Aim:** A description may contain `/by`, and a word may contain the letters
"by", without either being mistaken for a marker. Guards the choice to split on
`" /by "` with its surrounding spaces rather than on `"by"` or `"/by"`.

**Input:**

```text
todo read book /by tomorrow
deadline standby report /by 2019-12-06 0900
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book /by tomorrow
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] standby report (by: Dec 06 2019, 9:00 AM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book /by tomorrow
     2. [D][ ] standby report (by: Dec 06 2019, 9:00 AM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC27: Extra spaces around arguments are accepted

**Aim:** Spare spaces after a keyword are not an error -- they are trimmed off.
Guards the `trim()` before `Integer.parseInt`, which rejects a number with
spaces around it, and the `trim()` that keeps a description from being stored
with a leading space.

**Input:**

```text
todo    read book
mark  1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC28: A long session of mixed valid and invalid commands ends in the right state

**Aim:** Errors of every kind, interleaved with commands that succeed, leave
the task list exactly as the successful commands built it. This is the case
that would catch an error path corrupting state in a way the single-command
cases miss.

**Input:**

```text
todo read book
event project meeting /from Mon 2pm
deadline return book /by 2019-12-02 1800
blah
mark 2
todo
unmark 2
mark 9
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Erm when does it end? You need a /to after your /from!
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Erm sorry, what does that mean again?
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [D][X] return book (by: Dec 02 2019, 6:00 PM)
    ____________________________________________________________
    ____________________________________________________________
     HEYY!! The description of a todo cannot be empty!
    ____________________________________________________________
    ____________________________________________________________
     OK, I've marked this task as not done yet:
        [D][ ] return book (by: Dec 02 2019, 6:00 PM)
    ____________________________________________________________
    ____________________________________________________________
     There is no task 9! You only have 2 task(s).
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [D][ ] return book (by: Dec 02 2019, 6:00 PM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC29: A trailing space does not stop `bye` from exiting

**Aim:** `bye ` ends the session like `bye`. Guards the match being made on the
command keyword rather than on the whole input line: a trailing space is
invisible to the user, so matching the whole line rejected a command they had
typed correctly. A command after the `bye ` proves the loop really ended.

**Input:**

```text
bye 
todo this must not be added
```

**Expected output:**

```text
{{GREETING}}
{{FAREWELL}}
```

### TC30: A trailing space does not stop `list` from listing

**Aim:** The same for `list `, which was the other command matched on the whole
line. It lists rather than being reported as unknown.

**Input:**

```text
todo read book
list 
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC31: Delete a task from the middle of the list

**Aim:** `delete <n>` removes task n, echoes back the task it removed, and
reports the new size. The tasks after it close up, so a later `list` numbers
them contiguously with no gap where the removed task was.

**Input:**

```text
todo read book
deadline return book /by 2019-12-02 1800
event project meeting /from 2019-12-02 1400 /to 2019-12-04 1600
todo join sports club
delete 3
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
     Now you have 3 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] join sports club
     Now you have 4 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
     Now you have 3 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     3. [T][ ] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

### TC32: Deleting renumbers the tasks that follow

**Aim:** After a delete, the numbers shift down, so `mark 2` names what used to
be task 3. This is what would break if a delete left a hole rather than closing
the list up.

**Input:**

```text
todo read book
todo return book
todo join sports club
delete 1
mark 2
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] join sports club
     Now you have 3 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [T][ ] read book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] join sports club
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] return book
     2. [T][X] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

### TC33: Delete the only task, leaving the list empty

**Aim:** Deleting down to nothing leaves a list that still works rather than a
list in a broken state. Guards the boundary where the count reaches zero.

**Input:**

```text
todo read book
delete 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [T][ ] read book
     Now you have 0 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
    ____________________________________________________________
{{FAREWELL}}
```

### TC34: `delete` with a bad task number is rejected

**Aim:** `delete` validates its argument the same way `mark` and `unmark` do --
missing, non-numeric, and out of range are each reported, and none of them
removes anything. The closing `list` shows the task still there.

**Input:**

```text
todo read book
delete
delete abc
delete 9
delete 0
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     HEYY!! You need a valid number to delete
    ____________________________________________________________
    ____________________________________________________________
     WHAT? Why are you passing a non integer?! Give me an INTEGER!!
    ____________________________________________________________
    ____________________________________________________________
     There is no task 9! You only have 1 task(s).
    ____________________________________________________________
    ____________________________________________________________
     There is no task 0! You only have 1 task(s).
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC35: Deleting from an empty list is rejected

**Aim:** `delete 1` with nothing stored is reported rather than removing from an
empty list. Guards the range check when the count is zero, where every task
number is out of range.

**Input:**

```text
delete 1
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     There is no task 1! You only have 0 task(s).
    ____________________________________________________________
{{FAREWELL}}
```

### TC36: Adding after a delete continues from the new count

**Aim:** A task added after a delete is numbered from the shortened list, not
from a count that only ever grows. Interleaves adds and deletes so a count kept
separately from the list would drift out of step.

**Input:**

```text
todo read book
todo return book
delete 1
todo join sports club
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] join sports club
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] return book
     2. [T][ ] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

### TC37: A deleted task's done state does not follow its number

**Aim:** Deleting a task shifts the ones after it without disturbing their
completion state. Task 2 is marked done, task 1 is removed, and the task that
becomes 1 is still the one that was marked.

**Input:**

```text
todo read book
todo return book
mark 2
delete 1
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] return book
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][X] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC38: Tasks are still there after the chatbot is restarted

**Aim:** Tasks added in one run are saved and loaded back by the next, so the
list survives the program exiting. This is the whole point of the save file, and
it needs two runs to show at all.

**Input:**

```text
todo read book
deadline return book /by 2019-12-02 1800
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] return book (by: Dec 02 2019, 6:00 PM)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [D][ ] return book (by: Dec 02 2019, 6:00 PM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC39: A task's done state survives a restart

**Aim:** `mark` is saved as well as the task itself, so a task marked done in
one run comes back done. Guards the done flag being written to the save file and
read back, rather than every loaded task starting out not done.

**Input:**

```text
todo read book
todo return book
mark 2
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] return book
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [T][X] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC40: A deleted task does not come back after a restart

**Aim:** `delete` saves the shortened list, so the removed task stays removed.
Guards the save being a fresh write of the whole list rather than an append,
which would leave the deleted task in the file.

**Input:**

```text
todo read book
todo return book
delete 1
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] return book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Noted. I've removed this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC41: An event's start and end both survive a restart

**Aim:** A task type with more than one extra field round-trips completely.
Guards the save format's fourth and fifth fields being written and read back in
the right order -- swapping them would still load, and only the displayed times
would give it away.

**Input:**

```text
event project meeting /from 2019-12-02 1400 /to 2019-12-04 1600
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
     Now you have 1 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
     1. [E][ ] project meeting (from: Dec 02 2019, 2:00 PM to: Dec 04 2019, 4:00 PM)
    ____________________________________________________________
{{FAREWELL}}
```

### TC42: A task is saved even when the session ends without a `bye`

**Aim:** Saving happens when the task is added, not when the program exits, so a
session that ends by input running out has still saved its work. Guards against
the save being attached to the `bye` path alone.

**Input:**

```text
todo read book
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 1 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC43: A save file line that cannot be read is skipped, not fatal

**Aim:** One unreadable line costs only that task: it is reported and skipped,
and the remaining lines still load. A description containing the field separator
`" | "` is the way to produce such a line without editing the file by hand -- it
saves as an extra field, which is the known limitation noted below.

**Input:**

```text
todo a | b
todo read book
bye
```

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] a | b
     Now you have 1 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [T][ ] read book
     Now you have 2 task(s) in the list.
    ____________________________________________________________
{{FAREWELL}}
{{GREETING}}
    ____________________________________________________________
     Skipping a line I could not read: expected 3 fields but found 4: T | 0 | a | b
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

## Not yet covered

Behaviour that is out of scope for the current increment, listed so it is not
mistaken for an oversight. Add cases here as the chatbot grows:

* **Invalid `mark`/`unmark` arguments.** `mark 9` with fewer than 9 tasks, or
  `mark abc`, currently throws an uncaught exception and kills the chatbot.
  There is no error handling to test yet, so there is no case for it.
* **Arguments given to `bye` and `list`.** Matching on the keyword means
  `bye now` and `list all` are accepted, with the extra text ignored. That is
  consistent with the other commands, which also ignore what they do not read,
  but it is tolerated rather than intended, so it is not fixed by a case.
* **A blank input line.** Pressing enter on its own is reported as an unknown
  command. A blank line inside a fenced input block is too easy to mistake for
  formatting, so this one is left to inspection rather than a case.
* **A ceiling on the number of tasks.** There is no longer one to test: the
  tasks are held in an `ArrayList`, which grows as tasks are added, so the
  refusal message that `MAX_TASKS` used to produce is gone.
* **A description containing the save file's field separator.** `todo a | b` is
  accepted and shown correctly, but saves as a line with an extra field, so the
  task is skipped on the next start-up rather than coming back. TC43 pins that
  behaviour down as a reported skip rather than silent truncation, but the task
  is still lost. Fixing it properly means escaping the separator when writing, or
  putting the description last so the rest of the line can be split off before
  it; both are more machinery than this increment needs.
* **Losing the save file mid-session.** The save is written after every change,
  so a chatbot killed outright should lose nothing. Confirming that needs the
  process to be killed rather than fed end-of-input, which the test script has no
  way to do -- TC42 covers the nearest testable case, input simply running out.
* **A saved date that cannot be read.** A date in the save file is parsed
  through the same helper as one the user types, so it is reported and the line
  skipped rather than killing start-up, exactly as TC43 shows for a bad field
  count. There is no case for it because there is no way to produce one through
  the UI: every date the chatbot writes it wrote from a `LocalDate`, so it can
  always read it back. Reaching this needs the file edited by hand, which the
  test script does not do.
* **An event that ends before it starts.** `event x /from 2019-12-05 1800 /to
  2019-12-01 1800` is accepted and stored. Now that both are real points in
  time, comparing them is a one-line check, but rejecting it is a decision about
  what the chatbot should allow rather than a bug in what it does, so it is left
  alone for now.
* **A start and end that disagree about having a time.** Not reachable: a time
  is required on every date, so an event cannot have one end with a time and the
  other without. It would need testing if the time were ever made optional.
* **An unreadable or unwritable save file.** The messages for a save file that
  exists but cannot be read, or a `./data` folder that cannot be created, are
  reachable only by changing file permissions, which the test script does not
  set up.
