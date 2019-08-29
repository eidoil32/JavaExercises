package magit;

import controller.Controller;
import controller.IntroController;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import magit.utils.MyBooleanProperty;
import magit.utils.MyScene;
import settings.Settings;
import settings.UTF8Control;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MagitUI {
    private Magit model;
    private Stage primaryStage;
    private MyBooleanProperty languageProperty = new MyBooleanProperty(), themeProperty = new MyBooleanProperty();
    private IntroController introController;

    public MagitUI(Magit model, Stage primaryStage, IntroController introController) throws IOException {
        this.model = model;
        this.primaryStage = primaryStage;
        this.introController = introController;
        start();
    }

    private void start() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = Program.class.getResource(Settings.FXML_APPLICATION);
        loader.setLocation(mainFXML);
        loader.setResources(Settings.language);
        Pane root = loader.load();

        // wire up controller
        Controller controller = loader.getController();
        controller.setModel(model);
        StringProperty currentUserName = new SimpleStringProperty(Settings.language.getString("USER_ADMINISTRATOR"));
        currentUserName.addListener(((observable, oldValue, newValue) -> {
            model.setCurrentUser(newValue);
        }));
        controller.setStringProperty_CurrentUser(currentUserName);
        StringProperty currentState = new SimpleStringProperty();
        controller.setStringProperty_CurrentMagitState(currentState);
        currentState.setValue(String.format(Settings.language.getString("LOAD_REPOSITORY_SUCCESS"), model.getCurrentRepository().getName()));
        controller.initializeTableViewCommit();
        controller.updateBranchesSecondRowData();
        controller.updateRepositoryHistory();
        controller.setLanguageProperty(languageProperty);
        controller.setThemeProperty(themeProperty);
        controller.setIntroController(introController);
        languageProperty.setValueListener(value -> {
            try {
                ResourceBundle.clearCache();
                Settings.language = ResourceBundle.getBundle(Settings.RESOURCE_FILE, new UTF8Control(new Locale(Settings.currentLanguage)));
                start();
            } catch (IOException e) {
                System.out.println("error....");
            }
        });
        themeProperty.setValueListener(value -> {
            try {
                start();
            } catch (IOException e) {
                System.out.println("error....");
            }
        });
        // set stage
        Stage stage = new Stage();
        primaryStage.hide();
        controller.setPrimaryStage(stage);
        stage.setMinWidth(Settings.MAGIT_UI_MIN_WIDTH);
        stage.setMinHeight(Settings.MAGIT_UI_MIN_HEIGHT);
        stage.setResizable(true);
        stage.initStyle(StageStyle.DECORATED);
        stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        Scene scene = new MyScene(root, Settings.MAGIT_UI_MIN_WIDTH, Settings.MAGIT_UI_INTRO_MIN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }
}