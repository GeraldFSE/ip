package thomas;

/**
 * Signals that the chatbot cannot carry out a command the user typed.
 * <p>
 * This covers only mistakes the user can fix by typing something different --
 * an empty description, a missing {@code /by}, a task number that does not
 * exist. Its message is written for the user to read, so it says what was wrong
 * and, where useful, what to type instead.
 * <p>
 * It extends {@link Exception} rather than {@code RuntimeException} so that it
 * is a <em>checked</em> exception: the compiler then refuses to build any code
 * that throws it without handling it, which is what stops one of these errors
 * from reaching the user as a stack trace.
 */
public class ThomasException extends Exception {
    /**
     * Creates an exception carrying a message meant for the user.
     *
     * @param message what went wrong, phrased for the person typing
     */
    public ThomasException(String message) {
        super(message);
    }
}
