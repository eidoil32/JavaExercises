package magit;

import controller.IntroController;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import languages.LangEN;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class IntroUI extends Application {
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty();
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = IntroUI.class.getResource(Settings.FXML_INTRO_WINDOW);
        loader.setLocation(mainFXML);
        Pane root = loader.load();

        // wire up controller
        IntroController controller = loader.getController();
        controller.setModel(new Magit());
        controller.setFinishStart(isRepositoryExists);

        isRepositoryExists.addListener((observable, oldValue, newValue) -> {
            try {
                MagitUI magit = new MagitUI(controller.getModel(),primaryStage);
            } catch (IOException e) {
                System.out.println("error....");
            }
        });

        // set stage
        primaryStage.setResizable(false);
        primaryStage.setTitle(LangEN.MAGIT_WINDOW_TITLE);
        Scene scene = new Scene(root, Settings.MAGIT_UI_INTRO_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}