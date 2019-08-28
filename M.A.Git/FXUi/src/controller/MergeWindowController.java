package controller;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import magit.Blob;
import magit.BlobMap;
import magit.utils.MergeProperty;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.eUserMergeChoice;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MergeWindowController {
    @FXML
    private TextFlow activeFileText, ancestorFileText, targetFileText;
    @FXML
    private Label targetTitle, activeTitle, ancestorTitle, finalTitle, fileName;
    @FXML
    private TextArea customFileText;
    @FXML
    private Button btnOK, btnCancel, copyTextFromBase, btnCopyTextFromActive, btnCopyTextFromTarget;
    @FXML
    private RadioButton radioButtonAncestor, radioButtonActive, radioButtonTarget, radioButtonCustom;
    @FXML
    private ToggleGroup fileSelector;

    private MergeProperty conflictFinishProperty;
    private Stage stage;
    private BlobMap[] userChoices;
    private Map<String, BlobMap> blobs;
    private Map<RadioButton, eUserMergeChoice> map = new HashMap<>();
    private Map<eUserMergeChoice, TextFlow> contentMap = new HashMap<>();
    private BlobMap changesMap;
    private Blob currentBlob;
    private Map<eUserMergeChoice, Blob> duplicate;
    private String currentUser;

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

    public void setUserChoiceArray(BlobMap[] userChoices) {
        this.userChoices = userChoices;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean setFilesToCheck(Map<String, BlobMap> blobs) {
        this.blobs = blobs;
        this.changesMap = blobs.get(Settings.KEY_CHANGE_MAP);
        this.currentBlob = changesMap.getRandomBlob();
        if (this.currentBlob != null) {
            clearOldData();
            this.fileName.setText(currentBlob.getName());
            changesMap.remove(this.currentBlob);
            this.duplicate = changesMap.getDuplicate(this.currentBlob, blobs);
            this.activeFileText.getChildren().add(getCorrectText(duplicate.get(eUserMergeChoice.ACTIVE), btnCopyTextFromActive));
            this.targetFileText.getChildren().add(getCorrectText(duplicate.get(eUserMergeChoice.TARGET), btnCopyTextFromTarget));
            this.ancestorFileText.getChildren().add(getCorrectText(duplicate.get(eUserMergeChoice.ANCESTOR), copyTextFromBase));
            this.customFileText.clear();
            if (changesMap.getRandomBlob() != null) {
                btnOK.setText(Settings.language.getString("FX_MERGE_WINDOW_NEXT_FILE"));
            } else {
                btnOK.setOnAction(this::onButtonOK_FinishClick);
            }
        } else {
            return false;
        }
        return true;
    }

    private void clearOldData() {
        this.activeFileText.getChildren().clear();
        this.ancestorFileText.getChildren().clear();
        this.targetFileText.getChildren().clear();
        this.fileSelector.selectToggle(null);
    }

    private Text getCorrectText(Blob blob, Button button) {
        if (blob == null) {
            Platform.runLater(() -> button.setDisable(true));
            return new Text(Settings.language.getString("FX_MERGE_FILE_NOT_EXIST"));
        }
        return new Text(blob.getContent());
    }

    @FXML
    private void onButtonCancel_Click(ActionEvent event) throws RepositoryException {
        stage.close();
        conflictFinishProperty.setError(eErrorCodes.MARGE_CANCELED);
        conflictFinishProperty.set(-1);
    }

    @FXML
    private void onButtonOK_Click(ActionEvent event) {
        blobTreat(true);
    }

    private void blobTreat(boolean hasNext) {
        if (this.fileSelector.getSelectedToggle() != null) {
            eUserMergeChoice choice = map.get(fileSelector.getSelectedToggle());
            if (choice == eUserMergeChoice.OTHER) {
                Blob temp = getBaseFile();
                temp.setContent(customFileText.getText());
                temp.setSHA_ONE(DigestUtils.sha1Hex(temp.getContent()));
                temp.setDate(new Date());
                temp.setEditorName(currentUser);
                userChoices[0].addToMap(temp);
            } else {
                if (duplicate.get(choice) == null) {
                    userChoices[1].addToMap(currentBlob);
                } else {
                    userChoices[0].addToMap(duplicate.get(choice));
                }
            }
            if (hasNext)
                setFilesToCheck(blobs);
        }
    }

    private Blob getBaseFile() {
        Blob temp = this.duplicate.get(eUserMergeChoice.ANCESTOR);
        if (temp == null) {
            temp = this.duplicate.get(eUserMergeChoice.TARGET);
            if (temp == null) {
                temp = this.duplicate.get(eUserMergeChoice.ACTIVE);
            }
        }

        return temp;
    }

    private void onButtonOK_FinishClick(ActionEvent event) {
        blobTreat(false);
        conflictFinishProperty.set(1);
        stage.close();
    }

    @FXML
    private void onCopyTextFromActive_Click(ActionEvent event) {
        String content = ((Text) activeFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
        radioButtonCustom.setSelected(true);
    }

    @FXML
    private void onCopyTextFromBase_Click(ActionEvent event) {
        String content = ((Text) ancestorFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
        radioButtonCustom.setSelected(true);

    }

    @FXML
    private void onCopyTextFromTarget_Click(ActionEvent event) {
        String content = ((Text) targetFileText.getChildren().get(0)).getText();
        customFileText.clear();
        customFileText.setText(content);
        radioButtonCustom.setSelected(true);
    }


    public void setConflictFinishProperty(MergeProperty conflictProperty) {
        this.conflictFinishProperty = conflictProperty;
    }
}
