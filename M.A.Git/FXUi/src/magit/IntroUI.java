package magit;

import controller.IntroController;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import magit.utils.Utilities;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class IntroUI extends Application {
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty();

    @Override
    public void start(Stage primaryStage) throws Exception {

        // load language file

        // load main fxml
        URL mainFXML = IntroUI.class.getResource(Settings.FXML_INTRO_WINDOW);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(mainFXML);
        loader.setResources(Utilities.getLanguagesBundle());
        Pane root = loader.load();

        Magit model = new Magit();
        // wire up controller
        IntroController controller = loader.getController();
        controller.setModel(model);
        controller.setFinishStart(isRepositoryExists);

        Scene scene = new Scene(root, Settings.MAGIT_UI_INTRO_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);

        isRepositoryExists.addListener((observable, oldValue, newValue) -> {
            try {
                primaryStage.close();
                new MagitUI(controller.getModel(),primaryStage);
            } catch (IOException e) {
                IntroController.showAlert(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage());
            }
        });

        // set stage
        primaryStage.setResizable(false);
        primaryStage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}