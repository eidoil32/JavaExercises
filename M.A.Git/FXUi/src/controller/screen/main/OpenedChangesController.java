package controller.screen.main;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import magit.Magit;
import settings.Settings;
import utils.MapKeys;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class OpenedChangesController {
    private Magit model;
    private MainController mainController;
    private ListView<String> newFilesListView, deletedFilesListView, editedFilesListView;
    private TitledPane newFileTab, editedFileTab, deletedFileTab;

    @SafeVarargs
    public OpenedChangesController(MainController mainController, ListView<String> ... listViews) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.newFilesListView = listViews[0];
        this.deletedFilesListView = listViews[1];
        this.editedFilesListView = listViews[2];
        this.newFileTab = mainController.getFilesTab(Settings.FX_NEW_TAB_KEY);
        this.editedFileTab = mainController.getFilesTab(Settings.FX_EDIT_TAB_KEY);
        this.deletedFileTab = mainController.getFilesTab(Settings.FX_DELETED_TAB_KEY);
    }

    public void cleanListViews() {
        newFilesListView.setItems(null);
        deletedFilesListView.setItems(null);
        editedFilesListView.setItems(null);
    }

    public void scanRepository() {
        Task task = new Task() {
            @Override
            protected Void call() throws Exception {
                try {
                    updateProgress(0, 3);
                    Map<MapKeys, List<String>> data = model.showCurrentStatus();
                    Platform.runLater(() -> {
                        boolean alreadyOpenOne = false;
                        ObservableList<String> content = FXCollections.observableList(data.get(MapKeys.LIST_NEW));
                        newFilesListView.setItems(content);
                        updateProgress(1, 3);
                        if (content.size() != 0) {
                            mainController.getFilesTab(Settings.FX_NEW_TAB_KEY).setExpanded(true);
                            alreadyOpenOne = true;
                        }
                        updateProgress(2, 3);
                        content = FXCollections.observableList(data.get(MapKeys.LIST_CHANGED));
                        editedFilesListView.setItems(content);
                        if (content.size() != 0 && !alreadyOpenOne) {
                            mainController.getFilesTab(Settings.FX_EDIT_TAB_KEY).setExpanded(true);
                            alreadyOpenOne = true;
                        }

                        content = FXCollections.observableList(data.get(MapKeys.LIST_DELETED));
                        updateProgress(3, 3);
                        deletedFilesListView.setItems(content);
                        if (content.size() != 0 && !alreadyOpenOne) {
                            mainController.getFilesTab(Settings.FX_DELETED_TAB_KEY).setExpanded(true);
                        }
                    });
                    updateMessage(Settings.language.getString("FX_SHOW_STATUS_FOUND_SOMETHING"));
                } catch (RepositoryException | MyFileException | IOException e) {
                    if (e instanceof RepositoryException) {
                        if (((RepositoryException) e).getCode() == eErrorCodes.NOTHING_TO_SEE) {
                            Platform.runLater(() -> cleanListViews());
                        }
                    }
                    Platform.runLater(() -> {
                        IntroController.showAlert(e.getMessage());
                        newFileTab.setExpanded(false);
                        if (editedFileTab.isExpanded())
                            editedFileTab.setExpanded(false);
                        if (deletedFileTab.isExpanded())
                            deletedFileTab.setExpanded(false);
                    });
                }
                return null;
            }
        };
        mainController.bindTaskToUIComponents(task);
        new Thread(task).start();
    }
}
