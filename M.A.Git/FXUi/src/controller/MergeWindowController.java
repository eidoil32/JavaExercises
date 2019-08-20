package controller;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import magit.Blob;
import magit.utils.MergeProperty;
import magit.utils.MyBooleanProperty;
import utils.eUserMergeChoice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeWindowController {
    @FXML private TextFlow targetTitle, activeFileText, activeTitle, ancestorFileText, ancestorTitle, finalTitle, targetFileText;
    @FXML private TextArea customFileText;
    @FXML private Button btnOK, btnCancel;
    @FXML private RadioButton radioButtonAncestor, radioButtonActive, radioButtonTarget, radioButtonCustom;
    @FXML private ToggleGroup fileSelector;

    private MergeProperty mergeProperty;
    private Stage stage;
    private Map<RadioButton, eUserMergeChoice> map = new HashMap<>();
    private Map<eUserMergeChoice, TextFlow> contentMap = new HashMap<>();
    private MyBooleanProperty booleanProperty;

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
        activeFileText.getChildren().add(new Text(filesText.get(1).getContent()));
        ancestorFileText.getChildren().add(new Text(filesText.get(0).getContent()));
        targetFileText.getChildren().add(new Text(filesText.get(2).getContent()));
    }

    @FXML
    void onButtonCancel_Click(ActionEvent event) throws RepositoryException {
        stage.close();
        mergeProperty.setError(eErrorCodes.MARGE_CANCELED);
        mergeProperty.set(-1);
        booleanProperty.set(false);
    }

    @FXML
    void onButtonOK_Click(ActionEvent event) {
        if (this.fileSelector.getSelectedToggle() != null) {
            eUserMergeChoice choice = map.get(fileSelector.getSelectedToggle());
            mergeProperty.setChoice(choice);
            if (choice == eUserMergeChoice.OTHER) {
                mergeProperty.setContent(customFileText.getText());
            } else {
                mergeProperty.setContent(((Text)contentMap.get(choice).getChildren().get(0)).getText());
            }
            mergeProperty.set(choice.getNumber());
            stage.close();
        }
    }

    public void setBooleanProperty(MyBooleanProperty booleanProperty) {
        this.booleanProperty = booleanProperty;
    }
}
