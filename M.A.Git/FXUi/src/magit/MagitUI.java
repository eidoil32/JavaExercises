package magit;

import controller.Controller;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import magit.utils.MyBooleanProperty;
import settings.Settings;
import settings.UTF8Control;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MagitUI {
    private Magit model;
    private Stage primaryStage;
    private MyBooleanProperty languageProperty = new MyBooleanProperty();

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
        loader.setResources(Settings.language);
        Pane root = loader.load();

        // wire up controller
        Controller controller = loader.getController();
        controller.setModel(model);
        StringProperty currentUserName = new SimpleStringProperty(Settings.language.getString("USER_ADMINISTRATOR"));
        controller.setStringProperty_CurrentUser(currentUserName);
        StringProperty currentState = new SimpleStringProperty();
        controller.setStringProperty_CurrentMagitState(currentState);
        currentState.setValue(String.format(Settings.language.getString("LOAD_REPOSITORY_SUCCESS"), model.getCurrentRepository().getName()));
        controller.setPrimaryStage(primaryStage);
        controller.initializeTableViewCommit();
        controller.updateBranchesSecondRowData();
        controller.setLanguageProperty(languageProperty);

        languageProperty.setValueListener(value -> {
            try {
                ResourceBundle.clearCache();
                Settings.language = ResourceBundle.getBundle(Settings.RESOURCE_FILE, new UTF8Control(new Locale(Settings.currentLanguage)));
                start();
            } catch (IOException e) {
                System.out.println("error....");
            }
        });

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