package controller;

import exceptions.RepositoryException;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.StringConverter;
import magit.Branch;
import magit.Magit;
import magit.utils.Utilities;
import settings.Settings;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BranchManagerController {
    private Controller mainController;
    private Magit model;
    private Branch selectedBranch = null;

    @FXML
    private Button btn_addNewBranch, btnDeleteSelected;
    @FXML
    private ListView<Branch> branchListView;

    @FXML
    public void initialize() {
        branchListView.setEditable(true);
    }

    @FXML
    private void onAddNewBranchButton_Click(ActionEvent event) {
        StringProperty newBranchName = new SimpleStringProperty();
        newBranchName.addListener(((observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.EMPTY_STRING)) {
                try {
                    model.tryCreateNewBranch(newValue);
                    loadData();
                    branchListView.refresh();
                    IntroController.showAlert(String.format(Settings.language.getString("BRANCH_CREATED_SUCCESSFULLY"),newBranchName.getValue()), Alert.AlertType.INFORMATION);
                } catch (RepositoryException | IOException e) {
                    IntroController.showAlert(e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        }));
        mainController.showPopup(newBranchName, Settings.language.getString("PLEASE_ENTER_BRANCH_NAME"),Settings.language.getString("BRANCH_NAME_HINT"));
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
                        IntroController.showAlert(Settings.language.getString("BRANCH_DELETE_SUCCESSFULLY"),Alert.AlertType.INFORMATION);
                    } catch (RepositoryException e) {
                        IntroController.showAlert(e.getMessage());
                    }
                }
            });
        }
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    public void loadData() {
        List<Branch> branches = new LinkedList<>();
        for (Branch branch : model.getCurrentRepository().getBranches()) {
            if (!branch.isHead()) {
                branches.add(branch);
            }
        }
        ObservableList<Branch> allBranches = FXCollections.observableList(branches);
        branchListView.setItems(allBranches);
        branchListView.setCellFactory(lv -> {
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
        branchListView.setOnMouseClicked(event -> selectedBranch = branchListView.getSelectionModel().getSelectedItem());
    }
}