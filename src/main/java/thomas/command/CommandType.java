package thomas.command;

import thomas.ThomasException;

/**
 * Represents the kind of command a typed keyword names.
 * The set of commands is closed, so a word the user typed either maps to one of
 * these constants or is not a command at all. Naming them here means the
 * compiler checks every use.
 * This is the vocabulary of the language the user types, not a command that can
 * be carried out.
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

    /** Word the user types for this command */
    private final String keyword;

    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the kind of command a typed keyword names.
     * This is the one place that decides whether a word is a command, so a
     * caller holding a command type knows it is valid. Matching is case
     * sensitive.
     *
     * @param keyword First word of the line the user typed.
     * @return Matching kind of command.
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
