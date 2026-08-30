# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: 4/5
* IDE and level of expertise: 4/5

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments as "Comments and Javadoc" below requires, and in the format it sets out.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Comments and Javadoc

All comments are written in English, in American spelling, and without local slang — the code is meant for an international audience. Indent a comment to the level of the code it describes; a trailing comment on the same line as a statement is fine.

### Where header comments are required

Write one for every class, and for every public method. They may be omitted only for getters and setters, for an overridden method whose parent Javadoc applies exactly as it stands, and in test classes. All non-trivial private methods carry one too: if a method is hard to describe in a sentence, that is a sign the method is doing too much, and the comment is what surfaces it.

In test classes the exemption is applied as a rule, not a choice. A `@Test` method never carries a Javadoc comment. The `featureUnderTest_testScenario_expectedBehavior()` name already states the feature, the scenario and the expected result, so a comment above it restates the name in longer form. Where a case needs something the name cannot hold — why a fixture is built a particular way, that an assertion records a known rough edge rather than desired behavior — write it as an inline `//` comment inside the method body, beside the line it explains. The test class itself, and any helper method in it, do carry ordinary Javadoc.

### The format

```java
/**
 * Returns lateral location of the specified position.
 * If the position is unset, NaN is returned.
 *
 * @param x X coordinate of position.
 * @param y Y coordinate of position.
 * @param zone Zone of position.
 * @return Lateral location.
 * @throws IllegalArgumentException If zone is <= 0.
 */
public double computeLocation(double x, double y, int zone)
        throws IllegalArgumentException {
```

Follow it exactly:

* `/**` on its own line, subsequent `*` aligned under the first, a space after each `*`, and no blank line between the block and what it documents.
* The first sentence is a short summary, since Javadoc lifts it into the summary table and index. A method's starts in the third person — `Returns …`, `Sends …`, `Adds …` — never `Return` or `Returning`.
* Any further explanation follows as plain sentences, usually in the form `If …, then …`. Keep it short and targeted: a few sentences, not an essay.
* One blank `*` line between the description and the tag block.
* Every `@param`, `@return` and `@throws` description starts with a capital letter and ends with a period.
* `@return` may be omitted when the method returns nothing, or when the return value is already obvious from the description.
* `@param` is all-or-nothing: document every parameter, or none. Omit them only when every name is self-explanatory or already covered in the description.
* Use `{@inheritDoc}` when an overridden method needs the parent's comment plus a note about how it differs.
* A field's Javadoc goes on one line, with no closing period: `/** Number of connections to this database */`.

### Three project conventions, stricter than the above

* **No inline tags.** Do not write `{@link …}`, `{@code …}`, `<p>`, or any other HTML or inline Javadoc markup. Refer to types and methods in plain prose. This keeps the comments readable as source and avoids links that silently fail to resolve across packages, which had happened more than once before the rule.
* **Explain the decision, not the mechanism.** Where a design choice would otherwise look arbitrary — why a check lives in one class rather than another, why two classes are separate — state the reason in one sentence. Do not restate what the signature already says.
* **Write "to-do", never "todo", in prose.** IDEs list any comment matching a case-insensitive `\btodo\b` in their TODO window, and this project's own vocabulary collides with that marker. Eight comments were being reported as outstanding work before the rule, which is enough false entries to hide a real marker left before submission. The hyphen does not match the pattern. This applies to comment prose only: the keyword literal `"todo"` in `CommandType`, the `TodoTask` class name, the `TODO` enum constant and test strings such as `Parser.parse("todo read book")` are code and stay exactly as they are. Where the prose quotes what the user types, the hyphenated form is knowingly not a command the parser accepts — the example reads as illustrative rather than literal, which was the accepted trade.

When removing an inline tag from a comment, check whether it was the only use of an import; `checkstyleMain` and `checkstyleTest` fail on the unused import that is left behind.

## Testing

Two suites cover this project, and they cover different things. JUnit tests, under `src/test/java/`, test methods directly and are where logic, boundaries and error messages are pinned down. The text-UI plan, `test/ui-test-plan.md`, runs the whole program against typed input and is where what the user actually sees is pinned down. Neither replaces the other.

### JUnit coverage target

Aim to cover the **top ~50% highest-value methods**: the complex ones, the core ones, and the ones where a bug is expensive. This is a target about which methods are worth testing, not a line-coverage percentage — chasing a number by testing one-line delegations to `ArrayList` inflates the figure without catching anything.

Rank a method by what a bug in it would cost:

* **Highest** — anything that loses data the user cannot get back (`Storage`: a save file written or read wrongly loses tasks silently between runs, while every other error is on screen and survives to the next command).
* **High** — logic shared by several callers (`Task.parseDate` is reached from both the parser and the loader, so one bug is felt at the keyboard and in the save file at once), and conversions with off-by-one risk (`TaskList`, where the user's 1-based numbers meet the list's 0-based positions).
* **Worth testing** — anything with a real decision in it: the parser's rules about input shape, an inclusive range test, a constructor that refuses invalid state.
* **Below the line** — one-line delegations, and code whose only job is display. `Ui` and the `Command.execute` methods are deliberately left to the text-UI plan, which already tests exactly that end to end; duplicating it in JUnit costs more than it catches.

Follow Gradle and JUnit conventions: `seedu.duke.Todo` is tested by `seedu.duke.TodoTest` in `src/test/java/seedu/duke/TodoTest.java`. Name test methods `featureUnderTest_testScenario_expectedBehavior()`.

### After every change to code under `src/`

Before reporting the change as done:

1. **Update the JUnit tests.** Add cases for behavior the change introduces, and update cases whose behavior it deliberately alters. If the change touches a method that is above the line described above and has no tests, add them now rather than later — the target is maintained per change, not restored in a sweep afterwards. Say explicitly when an existing assertion was changed, and why.
2. **Update `test/ui-test-plan.md` if needed.** Add a case for behavior the change introduces, and update the expected output of existing cases whose behavior the change deliberately alters. Behavior that is deliberately out of scope belongs under "Not yet covered" rather than being left unmentioned.
3. **Run `./gradlew test checkstyleTest`**, then **run the `test-ui` skill** and report it as that skill describes: show the console transcript rather than summarizing it as "tests passed", stop at the first failure without fixing anything first, and say explicitly whenever an expected output in the plan was edited.

A change is not finished until both suites have been run and reported. If a change genuinely has no observable effect on the text UI (a comment, a Javadoc, a pure rename), say so instead of running that suite.

### Writing tests that are worth having

* **Derive every expectation by reading the source, not by pasting in what a run happened to print.** A test written from a run agrees with the code by construction, bugs included. This applies to JUnit assertions as much as to the plan's expected output.
* **Pin the message, not just the failure.** `assertThrows` alone passes whether the user is told something useful or something wrong.
* **Test both sides of a boundary.** The first item as well as one before it, the last as well as one past it, the day an event starts and ends as well as the days around them. A mistake at the edges survives any test that only asks about the middle.
* **When the code does something you did not expect, find out what it actually does before deciding it is wrong.** If the behavior is defensible, record it in a test whose name says so, with an inline `//` comment in the body saying what would change it — a test that documents a rough edge is how the fix later gets confirmed. If it is a real bug, report it and let the user decide; do not quietly assert the buggy behavior as correct.
* **Say when coverage moves or is lost.** Rewriting a case's expected output is a test silently replaced unless you say so, and a change that makes a path unreachable from one suite should say where that path is covered instead.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
