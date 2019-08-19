package magit;

import controller.Controller;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import magit.utils.Utilities;
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
        loader.setResources(Utilities.getLanguagesBundle());
        Pane root = loader.load();

        // wire up controller
        Controller controller = loader.getController();
        controller.setModel(model);
        StringProperty currentUserName = new SimpleStringProperty();
        currentUserName.setValue(Settings.language.getString("USER_ADMINISTRATOR"));
        controller.setStringProperty_CurrentUser(currentUserName);
        controller.setStringProperty_CurrentMagitState(new SimpleStringProperty());
        controller.setPrimaryStage(primaryStage);
        controller.initializeTableViewCommit();
        controller.updateBranchesSecondRowData();
        // set stage
        primaryStage.setMinWidth(Settings.MAGIT_UI_MIN_WIDTH);
        primaryStage.setMinHeight(Settings.MAGIT_UI_MIN_HEIGHT);
        primaryStage.setResizable(true);
        primaryStage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        Scene scene = new Scene(root, Settings.MAGIT_UI_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}