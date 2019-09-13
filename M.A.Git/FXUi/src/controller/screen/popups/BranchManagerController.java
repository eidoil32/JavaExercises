package controller.screen.popups;

import controller.screen.intro.IntroController;
import controller.screen.main.MainController;
import exceptions.RepositoryException;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import magit.Branch;
import magit.Magit;
import magit.utils.Utilities;
import settings.Settings;

import java.util.LinkedList;
import java.util.List;

public class BranchManagerController {
    private MainController mainController;
    private Magit model;
    private Branch selectedBranch = null, headBranch;

    @FXML
    private GridPane gridPane;
    @FXML
    private ColumnConstraints column_1, column_0;
    @FXML
    private Button btn_addNewBranch, btnDeleteSelected, resetBranchButton;
    @FXML
    private ListView<Branch> branchListView, remoteBranchListView;
    @FXML
    private ScrollPane remoteScrollPane;
    @FXML
    private Label remoteLabel, RTBLabel;

    @FXML
    public void initialize() {
        remoteBranchListView.setEditable(true);
        branchListView.setEditable(true);

        ChangeListener<Branch> listener = (observable, oldValue, newValue) -> mainController.markBranchInTree(newValue);

        remoteBranchListView.getSelectionModel().selectedItemProperty().addListener(listener);
        branchListView.getSelectionModel().selectedItemProperty().addListener(listener);
    }

    @FXML
    private void onAddNewBranchButton_Click(ActionEvent event) {
        mainController.onCreateNewBranchMenuItem_Click();
    }

    @FXML
    private void onDeleteSelectedBranchButton_Click(ActionEvent event) {
        if (selectedBranch != null) {
            Utilities.customAlert(
                    Alert.AlertType.WARNING,
                    type -> {
                        if (type.getButtonData() == ButtonBar.ButtonData.YES) {
                            try {
                                model.deleteBranch(selectedBranch.getName());
                                branchListView.getItems().remove(selectedBranch);
                                IntroController.showAlert(Settings.language.getString("BRANCH_DELETE_SUCCESSFULLY"), Alert.AlertType.INFORMATION);
                            } catch (RepositoryException e) {
                                IntroController.showError(e.getMessage());
                            }
                        }
                    },
                    Utilities.getYesAndNoButtons(),
                    Settings.language.getString("MAGIT_WINDOW_TITLE"),
                    String.format(Settings.language.getString("CONFIRM_DELETING_BRANCH"), selectedBranch.getName()));
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    public void loadData() {
        List<Branch> tempList = model.getCurrentRepository().getActiveBranches(), branches;
        boolean deselect = false;
        if (model.getRemoteRepository() == null) {
            hideSecondColumn();
            branches = tempList;
        } else {
            updateListViewData(removeHead(tempList), false, branchListView, remoteBranchListView);
            branches = model.getCurrentRepository().getRemoteBranches();
            remoteBranchListView.setEditable(false);
            deselect = true;
        }

        updateListViewData(removeHead(branches), deselect, remoteBranchListView, branchListView);
    }

    private void hideSecondColumn() {
        this.column_0.setMinWidth(0);
        this.column_0.setMaxWidth(0);
        this.RTBLabel.setVisible(false);
        this.remoteScrollPane.setVisible(false);
        this.remoteLabel.setText(Settings.language.getString("FX_BRANCH_MANAGER_TITLE"));
        this.column_1.setMinWidth(Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH);
        this.column_1.setFillWidth(true);
    }

    private List<Branch> removeHead(List<Branch> data) {
        List<Branch> branches = new LinkedList<>();

        for (Branch branch : data) {
            if (!branch.isHead()) {
                branches.add(branch);
            } else {
                headBranch = branch;
            }
        }

        return branches;
    }

    @SafeVarargs
    private final void updateListViewData(List<Branch> data, boolean isRemoteCannotDelete, ListView<Branch>... listViews) {
        ObservableList<Branch> allBranches = FXCollections.observableList(data);
        listViews[0].setItems(allBranches);
        listViews[0].setCellFactory(param -> new ListCell<Branch>() {
            @Override
            protected void updateItem(Branch item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
                    setText(null);
                    getStyleClass().remove(Settings.HEAD_BRANCH_CSS_CLASS);
                } else {
                    setText(item.getName());

                    if ((item == model.getCurrentBranch() && !getStyleClass().contains(Settings.HEAD_BRANCH_CSS_CLASS))) {
                        getStyleClass().add(Settings.HEAD_BRANCH_CSS_CLASS);
                    } else {
                        getStyleClass().remove(Settings.HEAD_BRANCH_CSS_CLASS);
                    }
                }
            }
        });

        listViews[0].setOnMouseClicked(event -> {
            listViews[1].getSelectionModel().clearSelection();
            selectedBranch = null;
            if (!isRemoteCannotDelete) {
                selectedBranch = listViews[0].getSelectionModel().getSelectedItem();
                enableOrDisableButtons(false);
            } else
                enableOrDisableButtons(true);
        });
    }

    private void enableOrDisableButtons(boolean flag) {
        this.btnDeleteSelected.setDisable(flag);
        this.resetBranchButton.setDisable(flag);
    }


    @FXML
    void onResetBranchButton_Click(ActionEvent event) {
        if (selectedBranch != null) {
            mainController.resetBranch(selectedBranch, false);
        }
    }
}