package magit.utils;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import settings.Settings;
import settings.UTF8Control;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class Utilities {
    public static File fileChooser(String extensionText, String extensionType, Scene scene) {
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(extensionText, extensionType);
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showOpenDialog(scene.getWindow());
    }

    public static File choiceFolderDialog(Scene scene) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        return directoryChooser.showDialog(scene.getWindow());
    }


    public static List<ButtonType> getYesAndNoButtons() {
        List<ButtonType> buttons = new LinkedList<>();
        buttons.add(new ButtonType(Settings.language.getString("BUTTON_NO"), ButtonBar.ButtonData.NO));
        buttons.add(new ButtonType(Settings.language.getString("BUTTON_YES"), ButtonBar.ButtonData.YES));
        return buttons;
    }

    public static ResourceBundle getLanguagesBundle() {
        return ResourceBundle.getBundle(Settings.RESOURCE_FILE, new UTF8Control(new Locale("he_IL")));
    }

    /**
     *
     * @param alertType choose an image for the alert
     * @param function function to action on button clicked
     * @param buttons which buttons the alert will have
     * @param values 2 values is needed, 0 - the title of alert, 1 - the content text of alert.
     */
    public static void customAlert(Alert.AlertType alertType, Consumer<ButtonType> function, List<ButtonType> buttons, String ... values) {
        Alert alert = new Alert(alertType);
        alert.setTitle(values[0]);
        alert.setContentText(values[1]);
        alert.getButtonTypes().setAll(buttons);
        alert.showAndWait().ifPresent(function);
    }
}