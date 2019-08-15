package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import magit.Magit;

public class Controller {
    @FXML private ImageView refreshTable;
    @FXML private MenuButton repositoryListMenuBtn, branchListMenuBtn;
    @FXML private Button commitBtn, scanRepositoryBtn;
    @FXML private ListView<?> newFilesListView, editedFilesListView, deletedFilesListView,
            diffDetailsListView, fileTreeDetailsListView, commitDetailsListView;
    @FXML private TableView<?> commitTable;
    @FXML private Tab commitTab, diffTab, fileTreeTab;
    @FXML private Label commitCommentLabel, executeCommandProgress, currentUser;
    private Magit model;

    @FXML private void onCommitButtonClick(ActionEvent event) {

    }

    @FXML private void onCommitTableRowClick(MouseEvent event) {

    }

    @FXML private void onScanRepositoryButtonClick(ActionEvent event) {

    }

    public void setModel(Magit model) {
        this.model = model;
    }
}