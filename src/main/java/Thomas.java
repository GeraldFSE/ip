import java.util.Scanner;

/**
 * Entry point for the Thomas chatbot.
 * <p>
 * In this increment Thomas reads commands from standard input and echoes
 * each one back, stopping when the user types {@code bye} or the input ends.
 */
public class Thomas {
    /** Indentation applied to every line of chatbot text. */
    private static final String INDENT = "     ";

    /**
     * Horizontal rule printed above and below each block of chatbot output.
     * Indented one space less than the text it wraps, as the sample output shows.
     */
    private static final String DIVIDER =
            "    ____________________________________________________________";

    public static void main(String[] args) {
        // ASCII art spelling "Thomas". Every backslash in the art is written
        // as \\ because a lone \ starts an escape sequence in a Java string.
        String banner = INDENT + "  ________                              \n"
                + INDENT + " /_  __/ /_  ____  ____ ___  ____ ______\n"
                + INDENT + "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/\n"
                + INDENT + " / / / / / / /_/ / / / / / / /_/ (__  ) \n"
                + INDENT + "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  \n";

        String greeting = DIVIDER + "\n"
                + banner
                + INDENT + "Chu Chu! I'm Thomas!\n"
                + INDENT + "How can I serve you today?\n"
                + DIVIDER + "\n";

        // print, not println: greeting already ends in a newline.
        System.out.print(greeting);

        Scanner userInput = new Scanner(System.in);

        while (userInput.hasNextLine()) {
            String command = userInput.nextLine();
            if (command.equals("bye")) {
                break;
            }
            String echo = DIVIDER + "\n"
                    + INDENT + command + "\n"
                    + DIVIDER + "\n";

            System.out.print(echo);
        }

        String farewell = INDENT + "Until next time! Chu Chu!\n"
                + DIVIDER + "\n";

        System.out.print(farewell);
    }
}