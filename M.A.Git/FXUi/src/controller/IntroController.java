package controller;

import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import exceptions.eErrorCodesXML;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import magit.IntroUI;
import magit.Magit;
import magit.Repository;
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
    private Magit model;
    private BooleanProperty isRepositoryExists;

    public void setModel(Magit model) {
        this.model = model;
    }

    public Magit getModel() {
        return model;
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
            URL mainFXML = IntroUI.class.getResource(Settings.FXML_DIALOG_BOX);
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
            stage.setScene(new Scene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT));
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
            loadExistsRepository(model, selectedFolder, isRepositoryExists);
        }
    }

    public static void loadExistsRepository(Magit model, File selectedFolder, BooleanProperty isRepositoryExists) {
        Task loadRepository = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    if (model.changeRepo(selectedFolder.toString())) {
                        Platform.runLater(() -> isRepositoryExists.setValue(true));
                    } else {
                        Platform.runLater(() -> showAlert(Settings.language.getString("LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT")));
                    }
                } catch (IOException | RepositoryException e) {
                    Platform.runLater(() -> showAlert(e.getMessage()));
                }
                return null;
            }
        };
        new Thread(loadRepository).start();
    }

    @FXML
    public void onLoadXMLRepositoryButtonClick(ActionEvent event) {
        // Show open file dialog
        File file = Utilities.fileChooser(Settings.language.getString("XML_FILE_REQUEST"), Settings.XML_FILE_REQUEST_TYPE, ((Node) event.getSource()).getScene());

        if (file != null) {
            loadXMLRepository(file, model, isRepositoryExists);
        }
    }

    public static void loadXMLRepository(File file, Magit model, BooleanProperty isRepositoryExists) {
        String extension = FilenameUtils.getExtension(file.getName());
        if (extension.equals(Settings.XML_EXTENSION)) {
            while (true) {
                try {
                    InputStream inputStream = new FileInputStream(file);
                    MagitRepository magitRepository = FileManager.deserializeFrom(inputStream);
                    model.basicCheckXML(magitRepository);
                    model.setCurrentRepository(Repository.XML_RepositoryFactory(magitRepository));
                    model.afterXMLLayout();
                    isRepositoryExists.setValue(true);
                    break;
                } catch (IOException | MyFileException | RepositoryException e) {
                    showAlert(e.getMessage());
                } catch (MyXMLException e) {
                    if (e.getCode() == eErrorCodesXML.ALREADY_EXIST_FOLDER) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
                        alert.setContentText(Settings.language.getString("XML_DELETE_AND_START_NEW_REPOSITORY"));
                        ButtonType yesButton = new ButtonType(Settings.language.getString("BUTTON_YES"), ButtonBar.ButtonData.YES);
                        ButtonType noButton = new ButtonType(Settings.language.getString("BUTTON_NO"), ButtonBar.ButtonData.NO);
                        alert.getButtonTypes().setAll(yesButton, noButton);
                        alert.showAndWait().ifPresent(type -> {
                            if (type.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                                model.deleteOldMagitFolder(e.getAdditionalData());
                            }
                        });
                    } else {
                        showAlert(e.getMessage());
                        break;
                    }
                } catch (JAXBException e) {
                    showAlert(Settings.language.getString("XML_PARSE_FAILED"));
                    break;
                }
            }
        }
    }

    public void setFinishStart(BooleanProperty isRepositoryExists) {
        this.isRepositoryExists = isRepositoryExists;
    }
}