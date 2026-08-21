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

### TC2: Add tasks and list them

**Aim:** An unrecognised command is stored as a new task and acknowledged, and
`list` prints the stored tasks numbered from 1, each not done.

**Input:**

```text
read book
return book
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [ ] return book
     Now you have 2 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     1. [ ] read book
     2. [ ] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC3: List with no tasks stored

**Aim:** `list` before anything has been added prints an empty block rather than
failing. Guards the loop that numbers the tasks against an empty task list.

**Input:**

```text
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
    ____________________________________________________________
{{FAREWELL}}
```

### TC4: Mark a task as done

**Aim:** `mark <n>` sets task n's status to done, echoes the task back, and the
change is visible in a later `list`. Only the named task changes.

**Input:**

```text
read book
return book
mark 2
list
bye
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [ ] return book
     Now you have 2 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [X] return book
    ____________________________________________________________
    ____________________________________________________________
     1. [ ] read book
     2. [X] return book
    ____________________________________________________________
{{FAREWELL}}
```

### TC5: Unmark a task that was done

**Aim:** `unmark <n>` returns a task that was marked done to not done, and the
change is visible in a later `list`.

**Input:**

```text
read book
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
        [ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [X] read book
    ____________________________________________________________
    ____________________________________________________________
     OK, I've marked this task as not done yet:
        [ ] read book
    ____________________________________________________________
    ____________________________________________________________
     1. [ ] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC6: Marking a task twice leaves it done

**Aim:** `mark` is not a toggle -- marking an already-done task keeps it done.
This is the behaviour that makes `unmark` the only way back.

**Input:**

```text
read book
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
        [ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [X] read book
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [X] read book
    ____________________________________________________________
    ____________________________________________________________
     1. [X] read book
    ____________________________________________________________
{{FAREWELL}}
```

### TC7: Input ends without a `bye`

**Aim:** The chatbot still says goodbye when standard input runs out rather than
ending on a `bye`. Guards the `hasNextLine()` check that ends the read loop.

**Input:**

```text
read book
```

**Expected output:**

```text
{{GREETING}}
    ____________________________________________________________
     Got it. I've added this task:
        [ ] read book
     Now you have 1 tasks in the list.
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
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [D][ ] return book (by: Sunday)
     Now you have 2 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Got it. I've added this task:
        [E][ ] project meeting (from: Mon 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
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
     Now you have 1 tasks in the list.
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
     Now you have 1 tasks in the list.
    ____________________________________________________________
    ____________________________________________________________
     Nice! I've marked this task as done:
        [T][X] join sports club
    ____________________________________________________________
    ____________________________________________________________
     1. [T][X] join sports club
    ____________________________________________________________
{{FAREWELL}}
```

## Not yet covered

Behaviour that is out of scope for the current increment, listed so it is not
mistaken for an oversight. Add cases here as the chatbot grows:

* **Invalid `mark`/`unmark` arguments.** `mark 9` with fewer than 9 tasks, or
  `mark abc`, currently throws an uncaught exception and kills the chatbot.
  There is no error handling to test yet, so there is no case for it.
* **Malformed add commands.** `todo` with no description, or `deadline read
  book` with no `/by`, currently throws an uncaught exception. Error handling
  arrives in a later increment, so there is no case for it yet.
* **Unrecognised commands.** Anything that is not a known keyword is still
  stored as a plain untyped task (see TC2). This fallback goes away once
  unknown commands become errors.
* **The 100-task limit.** Reaching `MAX_TASKS` prints a refusal message. Testing
  it needs 100 lines of input, which is better generated than written by hand.
