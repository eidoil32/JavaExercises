package magit.utils;

import javafx.scene.Scene;
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
}
