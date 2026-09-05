package thomas;

import java.io.IOException;
import java.util.Collections;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * Represents a dialog box consisting of an ImageView to represent the speaker's face
 * and a label containing text from the speaker.
 */
public class DialogBox extends HBox {
    @FXML
    private Label dialog;
    @FXML
    private ImageView displayPicture;

    private DialogBox(String text, Image img) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainWindow.class.getResource("/view/DialogBox.fxml"));
            fxmlLoader.setController(this);
            fxmlLoader.setRoot(this);
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.setText(text);
        displayPicture.setImage(img);
    }

    /**
     * Flips the dialog box such that the ImageView is on the left and text on the right.
     */
    private void flip() {
        ObservableList<Node> tmp = FXCollections.observableArrayList(this.getChildren());
        Collections.reverse(tmp);
        getChildren().setAll(tmp);
        setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
    }

    /**
     * Returns a dialog box holding what the user typed.
     *
     * @param text The line the user typed.
     * @param img The user's avatar.
     * @return The dialog box to append to the conversation.
     */
    public static DialogBox getUserDialog(String text, Image img) {
        return new DialogBox(text, img);
    }

    /**
     * Colours the bubble by the kind of command that produced the reply.
     * <p>
     * The names are Thomas's own command classes, as
     * {@link Thomas#getCommandType()} reports them. Anything unlisted keeps the
     * plain reply bubble, so a new command needs a case here only if it earns a
     * colour of its own.
     *
     * @param commandType The simple class name of the command that ran.
     */
    private void changeDialogStyle(String commandType) {
        String styleClass = switch (commandType) {
            case "AddCommand" -> "add-label";
            case "MarkCommand", "UnmarkCommand" -> "marked-label";
            case "DeleteCommand" -> "delete-label";
            // Every other reply, an error among them, keeps the plain bubble.
            default -> "";
        };
        if (!styleClass.isEmpty()) {
            dialog.getStyleClass().add(styleClass);
        }
    }

    /**
     * Returns a dialog box holding Thomas's reply, flipped and colored by the
     * command that produced it.
     *
     * @param text Thomas's reply.
     * @param img Thomas's avatar.
     * @param commandType The simple class name of the command that ran.
     * @return The dialog box to append to the conversation.
     */
    public static DialogBox getThomasDialog(String text, Image img, String commandType) {
        DialogBox dialogBox = new DialogBox(text, img);
        dialogBox.flip();
        dialogBox.changeDialogStyle(commandType);
        return dialogBox;
    }

}
