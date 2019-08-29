package controller.screen.popups;

import controller.screen.main.MainController;
import controller.screen.intro.IntroController;
import exceptions.RepositoryException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import magit.Branch;
import magit.Magit;
import magit.utils.Utilities;
import settings.Settings;

import java.util.LinkedList;
import java.util.List;

public class BranchManagerController {
    private MainController mainController;
    private Magit model;
    private Branch selectedBranch = null;

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
    }

    @FXML
    private void onAddNewBranchButton_Click(ActionEvent event) {
        mainController.onCreateNewBranchMenuItem_Click(event);
    }

    @FXML
    private void onDeleteSelectedBranchButton_Click(ActionEvent event) {
        if (selectedBranch != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            alert.setContentText(String.format(Settings.language.getString("CONFIRM_DELETING_BRANCH"), selectedBranch.getName()));
            alert.getButtonTypes().setAll(Utilities.getYesAndNoButtons());
            alert.showAndWait().ifPresent(type -> {
                if (type.getButtonData() == ButtonBar.ButtonData.YES) {
                    try {
                        model.deleteBranch(selectedBranch.getName());
                        branchListView.getItems().remove(selectedBranch);
                        IntroController.showAlert(Settings.language.getString("BRANCH_DELETE_SUCCESSFULLY"), Alert.AlertType.INFORMATION);
                    } catch (RepositoryException e) {
                        IntroController.showAlert(e.getMessage());
                    }
                }
            });
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(Settings.themeManager.get(Settings.currentTheme));
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
            }
        }

        return branches;
    }

    @SafeVarargs
    private final void updateListViewData(List<Branch> data, boolean isRemoteCannotDelete, ListView<Branch>... listViews) {
        ObservableList<Branch> allBranches = FXCollections.observableList(data);
        listViews[0].setItems(allBranches);
        listViews[0].setCellFactory(lv -> {
            TextFieldListCell<Branch> cell = new TextFieldListCell<>();
            cell.setConverter(new StringConverter<Branch>() {
                @Override
                public String toString(Branch branch) {
                    return branch.getName();
                }

                @Override
                public Branch fromString(String string) {
                    Branch branch = cell.getItem();
                    branch.setName(string);
                    return branch;
                }
            });
            return cell;
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
            mainController.resetBranch(selectedBranch, true);
        }
    }
}