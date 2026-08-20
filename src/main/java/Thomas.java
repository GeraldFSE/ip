/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas simply greets the user and exits.
 */
public class Thomas {
    /** Horizontal rule printed above and below each block of chatbot output. */
    private static final String DIVIDER =
            "____________________________________________________________";

    public static void main(String[] args) {
        // ASCII art spelling "Thomas". Every backslash in the art is written
        // as \\ because a lone \ starts an escape sequence in a Java string.
        String banner = "  ________                              \n"
                + " /_  __/ /_  ____  ____ ___  ____ ______\n"
                + "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/\n"
                + " / / / / / / /_/ / / / / / / /_/ (__  ) \n"
                + "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  \n";

        String greeting = DIVIDER + "\n"
                + banner
                + "Chu Chu! I'm Thomas!\n"
                + "How can I serve you today?\n"
                + DIVIDER + "\n";

        String farewell = "Until next time! Chu Chu!\n"
                + DIVIDER + "\n";

        // print, not println: both strings already end in a newline.
        System.out.print(greeting + farewell);
    }
}