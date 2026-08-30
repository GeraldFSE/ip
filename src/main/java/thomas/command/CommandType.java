package thomas.command;

import thomas.ThomasException;

/**
 * The kind of command a typed keyword names.
 * <p>
 * The set of commands is closed, so a word the user typed either maps to one of
 * these constants or is not a command at all. Naming them here means the
 * compiler checks every use: a misspelled {@code case DEADLINE} does not compile,
 * where a misspelled {@code keyword.equals("dedline")} used to compile into a
 * branch that could never run.
 * <p>
 * This is the vocabulary of the language the user types, not a command that can
 * be carried out. It answers "which command is this?" and nothing else.
 */
public enum CommandType {
    BYE("bye"),
    LIST("list"),
    ON("on"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event");

    /**
     * The word the user types for this command.
     * <p>
     * Held as its own field rather than derived from {@link #name()}, so that
     * what the user types stays independent of what the constant is called.
     */
    private final String keyword;

    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the kind of command a typed keyword names.
     * <p>
     * This is the one place that decides whether a word is a command, so by the
     * time a caller has a {@code CommandType} in hand it is known to be valid
     * and only real commands need handling. Matching is case sensitive, as it
     * was when each keyword was compared with {@code equals}.
     *
     * @param keyword The first word of the line the user typed.
     * @return The matching kind of command.
     * @throws ThomasException If no command has that keyword.
     */
    public static CommandType fromKeyword(String keyword) throws ThomasException {
        for (CommandType type : values()) {
            if (type.keyword.equals(keyword)) {
                return type;
            }
        }
        throw new ThomasException("Erm sorry, what does that mean again?");
    }
}
