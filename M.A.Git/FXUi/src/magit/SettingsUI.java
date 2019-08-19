package magit;

import controller.Controller;
import controller.SettingsController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.utils.Utilities;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class SettingsUI {

    private Magit model;
    private Stage primaryStage;
    private Controller mainController;

    public SettingsUI(Stage primaryStage, Magit model, Controller mainController) throws IOException {
        this.model = model;
        this.primaryStage = primaryStage;
        this.mainController = mainController;
        start();
    }

    private void start() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = IntroUI.class.getResource(Settings.FXML_SETTINGS_WINDOW);
        loader.setLocation(mainFXML);
        loader.setResources(Utilities.getLanguagesBundle());

        BorderPane root = loader.load();
        SettingsController controller = loader.getController();
        controller.setModel(model);
        controller.setMainController(mainController);
        Scene scene = new Scene(root, Settings.MAGIT_UI_SETTINGS_MIN_WIDTH, Settings.MAGIT_UI_SETTINGS_MIN_HEIGHT);
        Stage stage = new Stage();
        stage.setOnHiding(event -> {
            // change language or style
        });
        stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        stage.setScene(scene);
        stage.setMinWidth(Settings.MAGIT_UI_SETTINGS_MIN_WIDTH);
        stage.setMinHeight(Settings.MAGIT_UI_SETTINGS_MIN_HEIGHT);
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }
}
