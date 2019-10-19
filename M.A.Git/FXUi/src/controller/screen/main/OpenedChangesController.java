package controller.screen.main;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import magit.Magit;
import magit.utils.Utilities;
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
        initializeListViews();
    }

    private void cleanListViews() {
        newFilesListView.setItems(null);
        deletedFilesListView.setItems(null);
        editedFilesListView.setItems(null);
    }

    private void initializeListViews() {
        newFilesListView.setCellFactory(param -> new ListCell<String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                listViewItemConfig(item, empty, this, Settings.FILE_FOLDER_NEW);
            }
        });
        deletedFilesListView.setCellFactory(param -> new ListCell<String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                listViewItemConfig(item, empty, this, Settings.FILE_FOLDER_DELETE);
            }
        });
        editedFilesListView.setCellFactory(param -> new ListCell<String>(){
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                listViewItemConfig(item, empty, this, Settings.FILE_FOLDER_EDIT);
            }
        });
    }

    private void listViewItemConfig(String item, boolean empty, ListCell<String> cell, String type) {
        if (empty) {
            cell.setText(null);
        } else {
            String[] parts = Utilities.myCustomSplit(item, Settings.TYPE_SPLITTER);
            cell.setText(parts[1]);
            StringBuilder imageName = new StringBuilder(Settings.RESOURCE_IMAGE_PACKAGE);
            if (parts[0].equals(Settings.FILE_TYPE_IN_FOLDER_TABLE))
                imageName.append(Settings.FOLDER_IMAGE_KEY);
            else
                imageName.append(Settings.FILE_IMAGE_KEY);

            imageName.append("_").append(type);
            cell.setGraphic(new ImageView(new Image(imageName.toString() + Settings.IMAGE_PNG_TYPE)));
        }
    }

    public void scanRepository() {
        Task task = new Task() {
            @Override
            protected Void call() {
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
                        IntroController.showError(e.getMessage());
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
        mainController.bindTaskToUIComponents(task, true);
        new Thread(task).start();
    }
}
