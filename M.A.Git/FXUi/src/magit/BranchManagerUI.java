package magit;

import controller.BranchManagerController;
import controller.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.utils.MyScene;
import settings.Settings;

import java.io.IOException;
import java.net.URL;

public class BranchManagerUI {
    private Magit model;
    private Stage primaryStage;
    private Controller mainController;

    public BranchManagerUI(Stage primaryStage, Magit model, Controller mainController) throws IOException {
        this.model = model;
        this.primaryStage = primaryStage;
        this.mainController = mainController;
        start();
    }

    private void start() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        // load main fxml
        URL mainFXML = Program.class.getResource(Settings.FXML_BRANCH_MANAGER);
        loader.setLocation(mainFXML);
        loader.setResources(Settings.language);
        Pane root = loader.load();
        BranchManagerController controller = loader.getController();
        controller.setModel(model);
        controller.setMainController(mainController);
        controller.loadData();
        Scene scene = new MyScene(root, Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH, Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT);
        scene.getStylesheets().add(Settings.themeManager.get(Settings.currentTheme));
        Stage stage = new Stage();
        stage.setMinHeight(Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT + 50);
        stage.setMinWidth(Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH + 50);
        stage.setOnHiding(event -> {
            mainController.updateBranchesMenuButton();
            mainController.initializeTableViewCommit();
        });
        stage.setTitle(Settings.language.getString("BRANCH_MANAGER_TITLE"));
        stage.setScene(scene);
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }
}
