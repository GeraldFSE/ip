package thomas;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;
    @FXML
    private Button sendButton;

    private Thomas thomas;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/User.png"));
    private Image thomasImage = new Image(this.getClass().getResourceAsStream("/images/Thomas.png"));

    /** Keeps the newest dialog box in view as the conversation grows. */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Injects the chatbot the window talks to, and shows its greeting.
     * <p>
     * The greeting waits for the chatbot rather than going in
     * {@link #initialize()}: it is Thomas's first words, and until this method
     * runs there is no Thomas to say them. Any complaint about the save file
     * arrives with it, which is the only chance the user gets to hear that the
     * tasks on screen are not the tasks on disk.
     *
     * @param thomas The chatbot that answers what the user types.
     */
    public void setThomas(Thomas thomas) {
        this.thomas = thomas;
        dialogContainer.getChildren().add(
                DialogBox.getThomasDialog(thomas.getStartupMessage(), thomasImage, ""));
    }

    /**
     * Answers one typed line: appends a dialog box echoing it and another
     * holding Thomas's reply, then clears the input field.
     * <p>
     * A {@code bye} needs no special reply: its farewell arrives as the
     * response like any other, and all that is left is to close the window
     * afterwards.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText();
        String response = thomas.getResponse(input);
        String commandType = thomas.getCommandType();
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                DialogBox.getThomasDialog(response, thomasImage, commandType)
        );
        userInput.clear();

        if (thomas.isDone()) {
            // Closing straight away would take the window down before the
            // farewell is ever painted, so it is given time to be read first.
            PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
            pause.setOnFinished(event -> Platform.exit());
            pause.play();
        }
    }
}
