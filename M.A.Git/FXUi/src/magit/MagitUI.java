package magit;

import controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import languages.LangEN;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class MagitUI {
    private Magit model;
    private Stage primaryStage;

    public MagitUI(Magit model, Stage primaryStage) throws IOException {
        this.model = model;
        this.primaryStage = primaryStage;
        start();
    }

    private void start() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = IntroUI.class.getResource(Settings.FXML_APPLICATION);
        loader.setLocation(mainFXML);
        Pane root = loader.load();

        // wire up controller
        Controller controller = loader.getController();
        controller.setModel(new Magit());

        // set stage
        primaryStage.setMinWidth(Settings.MAGIT_UI_MIN_WIDTH);
        primaryStage.setMinHeight(Settings.MAGIT_UI_MIN_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.setTitle(LangEN.MAGIT_WINDOW_TITLE);
        Scene scene = new Scene(root, Settings.MAGIT_UI_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}