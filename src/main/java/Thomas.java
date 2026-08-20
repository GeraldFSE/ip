public class Thomas {
    public static void main(String[] args) {
        // ASCII art spelling "Thomas". Every backslash in the art is written
        // as \\ because a lone \ starts an escape sequence in a Java string.
        String banner = "  ________                              \n"
                + " /_  __/ /_  ____  ____ ___  ____ ______\n"
                + "  / / / __ \\/ __ \\/ __ `__ \\/ __ `/ ___/\n"
                + " / / / / / / /_/ / / / / / / /_/ (__  ) \n"
                + "/_/ /_/ /_/\\____/_/ /_/ /_/\\__,_/____/  \n";
        System.out.println(banner);
    }
}
