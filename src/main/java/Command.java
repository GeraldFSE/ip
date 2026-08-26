/**
 * A command Thomas understands.
 * <p>
 * The set of commands is closed, so a word the user typed either maps to one of
 * these constants or is not a command at all. Naming them here means the
 * compiler checks every use: a misspelt {@code case DEADLINE} does not compile,
 * where a misspelt {@code keyword.equals("dedline")} used to compile into a
 * branch that could never run.
 */
public enum Command {
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

    Command(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the command a typed keyword names.
     * <p>
     * This is the one place that decides whether a word is a command, so by the
     * time a caller has a {@code Command} in hand it is known to be valid and
     * only real commands need handling. Matching is case sensitive, as it was
     * when each keyword was compared with {@code equals}.
     *
     * @param keyword the first word of the line the user typed
     * @return the matching command
     * @throws ThomasException if no command has that keyword
     */
    public static Command fromKeyword(String keyword) throws ThomasException {
        for (Command command : values()) {
            if (command.keyword.equals(keyword)) {
                return command;
            }
        }
        throw new ThomasException("Erm sorry, what does that mean again?");
    }
}