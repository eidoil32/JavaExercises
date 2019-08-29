package controller.screen.intro;

import controller.screen.popups.DialogController;
import controller.ILoader;
import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import exceptions.eErrorCodesXML;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import magit.Magit;
import magit.Program;
import magit.Repository;
import magit.utils.MyScene;
import magit.utils.Utilities;
import org.apache.commons.io.FilenameUtils;
import settings.Settings;
import utils.FileManager;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class IntroController {
    @FXML
    private Button loadRepositoryBtn, createNewRepositoryBtn, loadXMLRepositoryBtn;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private ImageView closeButton;
    @FXML
    private AnchorPane topBar;

    private Magit model;
    private BooleanProperty isRepositoryExists, loadXMLBooleanProperty = new SimpleBooleanProperty();
    private Stage primaryStage;
    private double mouseXCords, mouseYCords;

    public void setModel(Magit model) {
        this.model = model;
    }

    public Magit getModel() {
        return model;
    }

    @FXML
    public void initialize() {
        closeButton.setOnMouseClicked((event -> {
            primaryStage.close();
        }));

        topBar.setOnMouseDragged((event -> {
            primaryStage.setX(event.getScreenX() - mouseXCords);
            primaryStage.setY(event.getScreenY() - mouseYCords);
        }));

        topBar.setOnMouseClicked((event -> {
            this.mouseXCords = event.getSceneX();
            this.mouseYCords = event.getSceneY();
        }));
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    public void onCreateNewRepositoryButtonClick(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        File selectedDirectory = Utilities.choiceFolderDialog(scene);
        if (selectedDirectory != null) {
            createNewRepository(selectedDirectory, model, isRepositoryExists);
        }
    }

    public static void createNewRepository(File selectedDirectory, Magit model, BooleanProperty isRepositoryExists) {
        ShowSimpleDialog((name, target, magit) -> {
            try {
                magit.createNewRepository(name, target);
            } catch (IOException | RepositoryException e) {
                showAlert(e.getMessage());
            }
        }, selectedDirectory, model, isRepositoryExists);
    }

    private static void ShowSimpleDialog(ILoader command, File target, Magit model, BooleanProperty isRepositoryExists) {
        Stage stage = new Stage();
        Pane root;
        try {
            StringProperty repositoryName = new SimpleStringProperty();
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Program.class.getResource(Settings.FXML_DIALOG_BOX);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            repositoryName.addListener((observable, oldValue, newValue) -> {
                command.execute(newValue, target, model);
                stage.close();
                isRepositoryExists.setValue(true);
            });
            DialogController dialogController = loader.getController();
            dialogController.setQuestion(Settings.language.getString("PLEASE_ENTER_REPOSITORY_NAME"));
            dialogController.setProperty(repositoryName);
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinWidth(Settings.MAGIT_UI_DIALOG_BOX_WIDTH + 50);
            stage.setMinHeight(Settings.MAGIT_UI_DIALOG_BOX_HEIGHT + 50);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showAlert(String errorMessage, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(Settings.language.getString("ERROR_MAGIT_TITLE"));
        alert.setContentText(errorMessage);
        ButtonType dismissButton = new ButtonType(Settings.language.getString("DISMISS_BTN"), ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(dismissButton);
        alert.showAndWait().ifPresent(type -> {
        });
    }

    public static void showAlert(String errorMessage) {
        showAlert(errorMessage, Alert.AlertType.ERROR);
    }

    @FXML
    public void onLoadRepositoryButtonClick(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        File selectedFolder = Utilities.choiceFolderDialog(scene);
        if (selectedFolder != null) {
            loadExistsRepository(model, selectedFolder, isRepositoryExists,this.progressBar);
        }
    }

    public static void loadExistsRepository(Magit model, File selectedFolder, BooleanProperty isRepositoryExists, ProgressBar progressBar) {
        Task loadRepository = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    updateProgress(0,1);
                    if (model.changeRepo(selectedFolder.toString())) {
                        Platform.runLater(() -> isRepositoryExists.setValue(true));
                        updateProgress(1,1);
                    } else {
                        Platform.runLater(() -> showAlert(Settings.language.getString("LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT")));
                    }
                } catch (IOException | RepositoryException e) {
                    Platform.runLater(() -> showAlert(e.getMessage()));
                }
                return null;
            }
        };
        bindComponentToTask(loadRepository,progressBar);
        new Thread(loadRepository).start();
    }

    @FXML
    public void onLoadXMLRepositoryButtonClick(ActionEvent event) {
        // Show open file dialog
        Scene scene;
        if (event.getSource() instanceof MenuItem) {
            scene = ((MenuItem) event.getTarget()).getParentPopup().getScene();
        } else {
            scene = ((Node) event.getSource()).getScene();
        }
        File file = Utilities.fileChooser(Settings.language.getString("XML_FILE_REQUEST"), Settings.XML_FILE_REQUEST_TYPE, scene);
        loadXMLFromFile(file, progressBar, (name, target, magit) -> { });
    }

    public void loadXMLFromFile(File file, ProgressBar progressBar, ILoader updateData) {
        int loadXMLLevels = 6;
        Task loadXML = new Task<Void>() {
            @Override
            protected Void call() {
                if (file != null) {
                    loadXMLBooleanProperty.addListener(((observable, oldValue, newValue) -> {
                        if (newValue) {
                            loadXMLFromFile(file, progressBar, updateData);
                        } else {
                            updateProgress(1, 1);
                        }
                    }));

                    updateProgress(0, loadXMLLevels);
                    String extension = FilenameUtils.getExtension(file.getName());
                    if (extension.equals(Settings.XML_EXTENSION)) {
                        try {
                            updateProgress(1, loadXMLLevels);
                            InputStream inputStream = new FileInputStream(file);
                            MagitRepository magitRepository = FileManager.deserializeFrom(inputStream);
                            updateProgress(2, loadXMLLevels);
                            model.basicCheckXML(magitRepository);
                            updateProgress(3, loadXMLLevels);
                            model.setCurrentRepository(Repository.XML_RepositoryFactory(magitRepository));
                            updateProgress(4, loadXMLLevels);
                            model.afterXMLLayout();
                            updateProgress(5, loadXMLLevels);
                            Platform.runLater(() -> isRepositoryExists.setValue(true));
                            updateData.execute("",file,model);
                        } catch (IOException | MyFileException | RepositoryException e) {
                            Platform.runLater(() -> showAlert(e.getMessage()));
                        } catch (MyXMLException e) {
                            if (e.getCode() == eErrorCodesXML.ALREADY_EXIST_FOLDER) {
                                Platform.runLater(() -> folderNotEmpty(e, model, loadXMLBooleanProperty));
                            } else {
                                Platform.runLater(() -> showAlert(e.getMessage()));
                            }
                        } catch (JAXBException e) {
                            Platform.runLater(() -> showAlert(Settings.language.getString("XML_PARSE_FAILED")));
                        }
                    }
                    updateProgress(6, loadXMLLevels);
                }
                return null;
            }
        };
        bindComponentToTask(loadXML, progressBar);
        new Thread(loadXML).start();
    }

    private static void bindComponentToTask(Task task, ProgressBar progressBar) {
        progressBar.progressProperty().bind(task.progressProperty());
    }

    public static void folderNotEmpty(MyXMLException e, Magit model, BooleanProperty tempProperty) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        alert.setContentText(Settings.language.getString("XML_DELETE_AND_START_NEW_REPOSITORY"));
        alert.getButtonTypes().setAll(Utilities.getYesAndNoButtons());
        alert.showAndWait().ifPresent(type -> {
            if (type.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                model.deleteOldMagitFolder(e.getAdditionalData());
                Platform.runLater(() -> tempProperty.set(true));
            } else {
                Platform.runLater(() -> tempProperty.set(false));
            }
        });
    }

    public void setFinishStart(BooleanProperty isRepositoryExists) {
        this.isRepositoryExists = isRepositoryExists;
    }

    public BooleanProperty getLoadXMLBooleanProperty() {
        return this.isRepositoryExists;
    }
}