package controller.screen.popups;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import magit.Blob;
import magit.BlobMap;
import magit.utils.MergeProperty;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.eUserMergeChoice;

import java.util.*;

public class MergeWindowController {
    @FXML
    private TextArea activeFileText, ancestorFileText, targetFileText, customFileText;
    @FXML
    private Label targetTitle, activeTitle, ancestorTitle, finalTitle, fileName;
    @FXML
    private Button btnOK, btnCancel, copyTextFromBase, btnCopyTextFromActive, btnCopyTextFromTarget;
    @FXML
    private RadioButton radioButtonAncestor, radioButtonActive, radioButtonTarget, radioButtonCustom;
    @FXML
    private ToggleGroup fileSelector;
    @FXML
    private ListView<Blob> conflictListFiles;

    private MergeProperty conflictFinishProperty;
    private Stage stage;
    private BlobMap[] userChoices;
    private Map<String, BlobMap> blobs;
    private Map<RadioButton, eUserMergeChoice> map = new HashMap<>();
    private Blob currentBlob;
    private Map<eUserMergeChoice, Blob> duplicate;
    private String currentUser;
    private int counter = 0, fileCounter = 0;

    @FXML
    public void initialize() {
        map.put(radioButtonActive, eUserMergeChoice.ACTIVE);
        map.put(radioButtonAncestor, eUserMergeChoice.ANCESTOR);
        map.put(radioButtonCustom, eUserMergeChoice.OTHER);
        map.put(radioButtonTarget, eUserMergeChoice.TARGET);
        radioButtonCustom.setSelected(true);
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
        setEnabling(false);
        this.blobs = blobs;
        BlobMap changesMap = blobs.get(Settings.KEY_CHANGE_MAP);
        if (changesMap.getRandomBlob() == null) {
            return false;
        } else {
            conflictListFiles.getItems().addAll(getAllBlobs(changesMap));
            conflictListFiles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != oldValue && newValue != null) {
                    clearOldData();
                    setEnabling(true);
                    currentBlob = newValue;
                    fileName.setText(currentBlob.getName());
                    duplicate = changesMap.getDuplicate(currentBlob, blobs);
                    activeFileText.setText(getCorrectText(duplicate.get(eUserMergeChoice.ACTIVE), btnCopyTextFromActive));
                    targetFileText.setText(getCorrectText(duplicate.get(eUserMergeChoice.TARGET), btnCopyTextFromTarget));
                    ancestorFileText.setText(getCorrectText(duplicate.get(eUserMergeChoice.ANCESTOR), copyTextFromBase));
                }
            });
            conflictListFiles.setCellFactory(param -> new ListCell<Blob>() {
                @Override
                protected void updateItem(Blob item, boolean empty) {
                    super.updateItem(item, empty);
                    if(empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getName());
                    }
                }
            });
            return true;
        }
    }

    private void setEnabling(boolean flag) {
        btnCopyTextFromActive.setDisable(!flag);
        btnCopyTextFromTarget.setDisable(!flag);
        btnOK.setDisable(!flag);
        copyTextFromBase.setDisable(!flag);
        if (!flag) {
            fileName.setText(Settings.language.getString("FX_MERGE_WINDOW_PLEASE_SELECT_FILE_FROM_LIST"));
        }
    }

    private List<Blob> getAllBlobs(BlobMap blobs) {
        Blob blob = blobs.getRandomBlob();
        List<Blob> blobList = new LinkedList<>();
        while (blob != null) {
            fileCounter++;
            blobList.add(blob);
            blobs.remove(blob);
            blob = blobs.getRandomBlob();
        }

        return blobList;
    }

    private void clearOldData() {
        this.activeFileText.clear();
        this.ancestorFileText.clear();
        this.targetFileText.clear();
        this.fileSelector.selectToggle(null);
        this.customFileText.clear();
    }

    private String getCorrectText(Blob blob, Button button) {
        if (blob == null) {
            Platform.runLater(() -> button.setDisable(true));
            return Settings.language.getString("FX_MERGE_FILE_NOT_EXIST");
        }
        return blob.getContent();
    }

    @FXML
    private void onButtonCancel_Click(ActionEvent event) throws RepositoryException {
        stage.close();
        conflictFinishProperty.setError(eErrorCodes.MARGE_CANCELED);
        conflictFinishProperty.set(-1);
    }

    @FXML
    private void onButtonOK_Click(ActionEvent event) {
        counter++;
        if (counter == fileCounter) {
            onButtonOK_FinishClick(event);
        }
        else if (counter + 1 == fileCounter) {
            blobTreat(false);
            btnOK.setOnAction(this::onButtonOK_FinishClick);
        } else {
            blobTreat(true);
        }
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
                conflictListFiles.getItems().remove(currentBlob);
            } else {
                if (duplicate.get(choice) == null) {
                    userChoices[1].addToMap(currentBlob);
                } else {
                    userChoices[0].addToMap(duplicate.get(choice));
                }
            }
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

    private void getTextFromTextField(TextArea textArea) {
        String content = textArea.getText();
        customFileText.clear();
        customFileText.setText(content);
        radioButtonCustom.setSelected(true);
    }

    @FXML
    private void onCopyTextFromActive_Click(ActionEvent event) {
        getTextFromTextField(activeFileText);
    }

    @FXML
    private void onCopyTextFromBase_Click(ActionEvent event) {
        getTextFromTextField(ancestorFileText);
    }

    @FXML
    private void onCopyTextFromTarget_Click(ActionEvent event) {
        getTextFromTextField(targetFileText);
    }


    public void setConflictFinishProperty(MergeProperty conflictProperty) {
        this.conflictFinishProperty = conflictProperty;
    }
}