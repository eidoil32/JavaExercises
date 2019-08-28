package magit.utils;

import javafx.scene.Parent;
import javafx.scene.Scene;
import settings.Settings;

public class MyScene extends Scene {
    public MyScene(Parent root, double width, double height) {
        super(root, width, height);
        getStylesheets().add(Settings.themeManager.get(Settings.currentTheme));
    }
}
