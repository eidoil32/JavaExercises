package magit;

import controller.Controller;
import controller.SettingsController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.utils.MyScene;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class SettingsUI {

    private Magit model;
    private Stage primaryStage;
    private Controller mainController;
    private BooleanProperty languageProperty, themeProperty;
    private StringProperty currentState;

    public SettingsUI(Stage primaryStage, Magit model, Controller mainController, ObservableValue ... values) throws IOException {
        this.model = model;
        this.primaryStage = primaryStage;
        this.mainController = mainController;
        this.languageProperty = (BooleanProperty)values[0];
        this.themeProperty = (BooleanProperty)values[1];
        this.currentState = (StringProperty)values[2];
        start();
    }

    private void start() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = Program.class.getResource(Settings.FXML_SETTINGS_WINDOW);
        loader.setLocation(mainFXML);
        loader.setResources(Settings.language);

        BorderPane root = loader.load();
        SettingsController controller = loader.getController();
        controller.setModel(model);
        controller.setMainController(mainController);
        Scene scene = new MyScene(root, Settings.MAGIT_UI_SETTINGS_MIN_WIDTH, Settings.MAGIT_UI_SETTINGS_MIN_HEIGHT);
        Stage stage = new Stage();
        controller.setStage(stage);
        controller.setLanguageProperty(languageProperty);
        controller.setThemeProperty(themeProperty);
        controller.setCurrentState(currentState);
        stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        stage.setScene(scene);
        stage.setMinWidth(Settings.MAGIT_UI_SETTINGS_MIN_WIDTH);
        stage.setMinHeight(Settings.MAGIT_UI_SETTINGS_MIN_HEIGHT);
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }
}
