package controller;

import bindings.StringBind;
import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import exceptions.eErrorCodesXML;
import javafx.beans.property.BooleanProperty;
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
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import languages.LangEN;
import magit.IntroUI;
import magit.Magit;
import magit.Repository;
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
    void onCreateNewRepositoryButtonClick(ActionEvent event) {
        Scene scene = ((Node) event.getSource()).getScene();
        File selectedDirectory = choiceFolderDialog(scene);
        if (selectedDirectory == null) {
            //No Directory selected
        } else {
            ShowSimpleDialog(scene, new ILoader() {
                @Override
                public void execute(String name, File target, Magit magit) {
                    try {
                        magit.createNewRepository(name,target);
                    } catch (IOException | RepositoryException e) {
                        showAlert(scene,e.getMessage());
                    }
                }
            },selectedDirectory);
        }
    }

    private File choiceFolderDialog(Scene scene) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        return directoryChooser.showDialog(scene.getWindow());
    }

    private void ShowSimpleDialog(Scene scene, ILoader command, File target) {
        Stage stage = new Stage();
        Pane root;
        try {
            StringBind repositoryName = new StringBind();
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = IntroUI.class.getResource(Settings.FXML_DIALOG_BOX);
            loader.setLocation(mainFXML);
            root = loader.load();
            repositoryName.valueProperty().addListener((observable, oldValue, newValue) -> {
                command.execute(newValue, target, model);
                stage.close();
                isRepositoryExists.setValue(true);
            });
            DialogController dialogController = loader.getController();
            dialogController.setQuestion(LangEN.PLEASE_ENTER_REPOSITORY_NAME);
            dialogController.setProperty(repositoryName);
            stage.setTitle(LangEN.MAGIT_WINDOW_TITLE);
            stage.setScene(new Scene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Scene scene, String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(LangEN.ERROR_MAGIT_TITLE);
        alert.setContentText(errorMessage);
        ButtonType dismissButton = new ButtonType(LangEN.DISMISS_BTN, ButtonBar.ButtonData.YES);
        alert.getButtonTypes().setAll(dismissButton);
        alert.showAndWait().ifPresent(type -> {  });

    }

    @FXML
    void onLoadRepositoryButtonClick(ActionEvent event) {
        Scene scene = ((Node)event.getSource()).getScene();
        File selectedFolder = choiceFolderDialog(scene);
        if(selectedFolder != null) {
            try {
                if(model.changeRepo(selectedFolder.toString())) {
                    isRepositoryExists.setValue(true);
                } else {
                    showAlert(scene,LangEN.LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT);
                }
            } catch (IOException | RepositoryException e) {
                showAlert(scene,e.getMessage());
            }
        }
    }

    @FXML
    void onLoadXMLRepositoryButtonClick(ActionEvent event) {
        Scene scene = ((Node)event.getSource()).getScene();
        FileChooser fileChooser = new FileChooser();

        // Set extension filter
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(LangEN.XML_FILE_REQUEST, "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);

        // Show open file dialog
        File file = fileChooser.showOpenDialog(scene.getWindow());
        if (file != null) {
            String extension = FilenameUtils.getExtension(file.getName());
            if(extension.equals(Settings.XML_EXTENSION)) {
                while(true) {
                    try {
                        InputStream inputStream = new FileInputStream(file);
                        MagitRepository magitRepository = FileManager.deserializeFrom(inputStream);
                        model.basicCheckXML(magitRepository);
                        model.setCurrentRepository(Repository.XML_RepositoryFactory(magitRepository));
                        model.afterXMLLayout();
                        isRepositoryExists.setValue(true);
                        break;
                    } catch (IOException | MyFileException | RepositoryException e) {
                        showAlert(scene, e.getMessage());
                    } catch (MyXMLException e) {
                        if (e.getCode() == eErrorCodesXML.ALREADY_EXIST_FOLDER) {
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle(LangEN.MAGIT_WINDOW_TITLE);
                            alert.setContentText(LangEN.XML_DELETE_AND_START_NEW_REPOSITORY);
                            ButtonType yesButton = new ButtonType(LangEN.BUTTON_YES, ButtonBar.ButtonData.YES);
                            ButtonType noButton = new ButtonType(LangEN.BUTTON_NO, ButtonBar.ButtonData.NO);
                            alert.getButtonTypes().setAll(yesButton, noButton);
                            alert.showAndWait().ifPresent(type -> {
                                if (type.getButtonData().equals(ButtonBar.ButtonData.YES)) {
                                    model.deleteOldMagitFolder(e.getAdditionalData());
                                } else {
                                    System.out.println(type.getButtonData());
                                }
                            });
                        } else {
                            showAlert(scene, e.getMessage());
                            break;
                        }
                    } catch (JAXBException e) {
                        showAlert(scene, LangEN.XML_PARSE_FAILED);
                        break;
                    }
                }
            }
        }
    }

    public void setFinishStart(BooleanProperty isRepositoryExists) {
        this.isRepositoryExists = isRepositoryExists;
    }
}