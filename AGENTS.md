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
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Testing

After every change to code under `src/`, and before reporting the change as done:

1. **Update `test/ui-test-plan.md` if needed.** Add a case for behaviour the change introduces, and update the expected output of existing cases whose behaviour the change deliberately alters. Derive expected output by reading the source, not by pasting in what a run happened to print — a test written from a run agrees with the code by construction, bugs included. Behaviour that is deliberately out of scope belongs under "Not yet covered" rather than being left unmentioned.
2. **Run the `test-ui` skill**, and report the run as that skill describes: show the console transcript rather than summarising it as "tests passed", stop at the first failure without fixing anything first, and say explicitly whenever an expected output in the plan was edited.

A change is not finished until the suite has been run and reported. If a change genuinely has no observable effect on the text UI (a comment, a Javadoc, a pure rename), say so instead of running the suite.

## Git

Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
