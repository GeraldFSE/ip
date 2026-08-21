# Thomas text-UI test plan

Test cases for the chatbot's text user interface. Each case is run by the
`test-ui` skill, which feeds the case's input lines to the program on standard
input and compares the console output against the expected output.

## How a case is run

* Every case runs the program **from scratch** in its own process. A case never
  sees tasks added by an earlier case, so cases can be reordered or run one at a
  time without their results changing. A case that needs existing tasks adds
  them itself, as part of its own input.
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
acknowledge it with the running task count, and show their own tag and
date/time details in a later `list`.

**Input:**

```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
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
        [D][ ] return book (by: Sunday)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] borrow book
     2. [D][ ] return book (by: Sunday)
     3. [E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________
{{FAREWELL}}
```

### TC9: Dates are kept as free text

**Aim:** A deadline's `/by` text is stored verbatim, with no date parsing, so
text that is not a date at all is still accepted. Guards the split on `/by`
against text containing spaces and punctuation.

**Input:**

```text
deadline do homework /by no idea :-p
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] do homework (by: no idea :-p)
     Now you have 1 task(s) in the list.
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

**Aim:** A missing end time is reported specifically, rather than being lumped
in with a missing start time. Guards the second of the two splits.

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
deadline standby report /by Friday
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
        [D][ ] standby report (by: Friday)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book /by tomorrow
     2. [D][ ] standby report (by: Friday)
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
deadline return book /by Sunday
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
        [D][ ] return book (by: Sunday)
     Now you have 2 task(s) in the list.
    ____________________________________________________________
    ____________________________________________________________
     Erm sorry, what does that mean again?
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [D][X] return book (by: Sunday)
    ____________________________________________________________
    ____________________________________________________________
     HEYY!! The description of a todo cannot be empty!
    ____________________________________________________________
    ____________________________________________________________
     OK, I've marked this task as not done yet:
        [D][ ] return book (by: Sunday)
    ____________________________________________________________
    ____________________________________________________________
     There is no task 9! You only have 2 task(s).
    ____________________________________________________________
    ____________________________________________________________
     Here are the tasks in your list:
     1. [T][ ] read book
     2. [D][ ] return book (by: Sunday)
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
* **The 100-task limit.** Reaching `MAX_TASKS` prints a refusal message. Testing
  it needs 100 lines of input, which is better generated than written by hand.
