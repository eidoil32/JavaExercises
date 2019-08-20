package controller;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import magit.Blob;
import magit.utils.MergeProperty;
import utils.eUserMergeChoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeWindowController {
    @FXML private TextFlow activeFileText,ancestorFileText,  targetFileText;
    @FXML private Label targetTitle, activeTitle, ancestorTitle, finalTitle;
    @FXML private TextArea customFileText;
    @FXML private Button btnOK, btnCancel, copyTextFromBase, btnCopyTextFromActive, btnCopyTextFromTarget;
    @FXML private RadioButton radioButtonAncestor, radioButtonActive, radioButtonTarget, radioButtonCustom;
    @FXML private ToggleGroup fileSelector;

    private MergeProperty mergeProperty, conflictFinishProperty;
    private Stage stage;
    private Map<RadioButton, eUserMergeChoice> map = new HashMap<>();
    private Map<eUserMergeChoice, TextFlow> contentMap = new HashMap<>();

    @FXML
    public void initialize() {
        map.put(radioButtonActive, eUserMergeChoice.ACTIVE);
        map.put(radioButtonAncestor, eUserMergeChoice.ANCESTOR);
        map.put(radioButtonCustom, eUserMergeChoice.OTHER);
        map.put(radioButtonTarget, eUserMergeChoice.TARGET);

        contentMap.put(eUserMergeChoice.ACTIVE, activeFileText);
        contentMap.put(eUserMergeChoice.ANCESTOR, ancestorFileText);
        contentMap.put(eUserMergeChoice.TARGET, targetFileText);
    }

    public void setMergeProperty(MergeProperty mergeProperty) {
        this.mergeProperty = mergeProperty;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setFileText(List<Blob> filesText) {
        activeFileText.getChildren().addAll(new Text(filesText.get(1).getContent()));
        ancestorFileText.getChildren().add(new Text(filesText.get(0).getContent()));
        targetFileText.getChildren().add(new Text(filesText.get(2).getContent()));
    }

    @FXML
    private void onButtonCancel_Click(ActionEvent event) throws RepositoryException {
        stage.close();
        conflictFinishProperty.setError(eErrorCodes.MARGE_CANCELED);
        conflictFinishProperty.set(-1);
    }

    @FXML
    private void onButtonOK_Click(ActionEvent event) {
        if (this.fileSelector.getSelectedToggle() != null) {
            eUserMergeChoice choice = map.get(fileSelector.getSelectedToggle());
            mergeProperty.setChoice(choice);
            if (choice == eUserMergeChoice.OTHER) {
                mergeProperty.setContent(customFileText.getText());
            } else {
                mergeProperty.setContent(((Text)contentMap.get(choice).getChildren().get(0)).getText());
            }
            mergeProperty.set(choice.getNumber());
            conflictFinishProperty.set(conflictFinishProperty.getValue()+1);
            stage.close();
        }
    }

    @FXML
    private void onCopyTextFromActive_Click(ActionEvent event) {
        String content = ((Text)activeFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
    }

    @FXML
    private void onCopyTextFromBase_Click(ActionEvent event) {
        String content = ((Text)ancestorFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
    }

    @FXML
    private void onCopyTextFromTarget_Click(ActionEvent event) {
        String content = ((Text)targetFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
    }


    public void setConflictFinishProperty(MergeProperty mergeProperty) {
        this.conflictFinishProperty = mergeProperty;
    }
}
