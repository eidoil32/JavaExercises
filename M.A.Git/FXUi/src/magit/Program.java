package magit;

import controller.IntroController;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import magit.utils.MyScene;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class Program extends Application {
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty();

    @Override
    public void start(Stage primaryStage) throws Exception {
        // load main fxml
        URL mainFXML = Program.class.getResource(Settings.FXML_INTRO_WINDOW);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(mainFXML);
        loader.setResources(Settings.language);
        Pane root = loader.load();

        Magit model = new Magit();
        // wire up controller
        IntroController controller = loader.getController();
        controller.setModel(model);
        controller.setFinishStart(isRepositoryExists);

        Scene scene = new MyScene(root, Settings.MAGIT_UI_INTRO_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);
        isRepositoryExists.addListener((observable, oldValue, newValue) -> {
            try {
                primaryStage.close();
                new MagitUI(controller.getModel(),primaryStage,controller);
            } catch (IOException e) {
                IntroController.showAlert(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage());
            }
        });

        // set stage
        controller.setPrimaryStage(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Settings.setup();
        launch(args);
    }

}