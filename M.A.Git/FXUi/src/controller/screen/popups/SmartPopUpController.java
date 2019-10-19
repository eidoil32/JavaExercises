package controller.screen.popups;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import magit.utils.CustomAnimations;
import settings.Settings;

public class SmartPopUpController {

    @FXML
    private TextArea textArea;
    @FXML
    private Label labelTxt;
    @FXML
    private Button confirmBtn, cancelBtn;

    private StringProperty stringProperty;

    public void initialize() {
        textArea.setOnMouseClicked(event -> {
            textArea.getStyleClass().clear();
            textArea.getStyleClass().addAll(Settings.CSS_TEXT_AREA_BASIC, Settings.CSS_TEXT_INPUT_BASIC);
        });
    }

    public void setStringProperty(StringProperty stringProperty) {
        this.stringProperty = stringProperty;
    }

    @FXML
    private void onCancelButton_Click(ActionEvent event) {
        this.stringProperty.setValue(Settings.CANCEL_BTN_CLICKED_STRING);
        ((Node)event.getSource()).getScene().getWindow().hide();
    }

    public void setTitle(String title) {
        this.labelTxt.setText(title);
    }

    @FXML
    private void onConfirmButton_Click(ActionEvent event) {
        String comment = textArea.getText().replace(Settings.JAVAFX_NEWLINE_TEXTAREA,System.lineSeparator());
        if(!comment.equals(Settings.EMPTY_STRING)) {
            stringProperty.setValue(comment);
            ((Node)event.getSource()).getScene().getWindow().hide();
        } else {
            textArea.getStyleClass().clear();
            textArea.getStyleClass().add(Settings.CSS_RED_BORDER_ERROR);
            CustomAnimations.playAnimation(CustomAnimations.snoozeNode(textArea));
        }
    }
}