package controller.screen.popups;

import controller.screen.intro.IntroController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.Blob;
import magit.eFileTypes;
import magit.utils.MyScene;
import settings.Settings;
import utils.WarpBasicFile;

import java.io.IOException;
import java.text.SimpleDateFormat;

public class FileViewer {
    private Blob blob;
    @FXML private Label fileDateModified, fileEditorName;
    @FXML private TextArea fileContent;
    @FXML private TextField fileName;

    private void update(Blob blob) {
        this.blob = blob;
        fileName.setText(blob.getName());
        fileDateModified.setText(new SimpleDateFormat(Settings.WEB_DATE_FORMAT).format(blob.getDate()));
        fileEditorName.setText(blob.getEditorName());
        fileContent.setText(blob.getContent());
    }

    public static void showFileViewer(MouseEvent event, TreeView<WarpBasicFile> commitFileTree, Class sanjer, Stage primaryStage)
    {
        if (event.getClickCount() == 2) {
            TreeItem<WarpBasicFile> item = commitFileTree.getSelectionModel().getSelectedItem();
            if (item.getValue() != null && item.getValue().getFile().getType() == eFileTypes.FILE) {
                try {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setResources(Settings.language);
                    loader.setLocation(FileViewer.class.getClassLoader().getResource(Settings.FXML_FILE_VIEWER));
                    Parent root = loader.load();
                    FileViewer fileViewer = loader.getController();
                    fileViewer.update(item.getValue().getFile());
                    Stage stage = new Stage();
                    stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
                    stage.setMinWidth(Settings.MAGIT_UI_FILE_VIEWER_WIDTH);
                    stage.setMinHeight(Settings.MAGIT_UI_FILE_VIEWER_HEIGHT);
                    Scene scene = new MyScene(root,Settings.MAGIT_UI_FILE_VIEWER_WIDTH, Settings.MAGIT_UI_FILE_VIEWER_HEIGHT);
                    stage.setScene(scene);
                    stage.initOwner(primaryStage);
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.show();
                } catch (IOException e) {
                    Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_ERROR_OPEN_FILE_DATA")));
                }
            }
        }
    }
}
