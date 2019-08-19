package controller;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import settings.Settings;

public class SmartPopUpController {

    @FXML
    private TextArea textArea;
    @FXML
    private Label labelTxt;
    @FXML
    private Button confirmBtn, cancelBtn;

    private StringProperty stringProperty;

    public void setStringProperty(StringProperty stringProperty) {
        this.stringProperty = stringProperty;
    }

    @FXML
    private void onCancelButton_Click(ActionEvent event) {
        this.stringProperty.setValue(Settings.CANCEL_BTN_CLICKED_STRING);
        ((Node)event.getSource()).getScene().getWindow().hide();
    }

    @FXML
    private void onConfirmButton_Click(ActionEvent event) {
        String comment = textArea.getText().replace(Settings.JAVAFX_NEWLINE_TEXTAREA,System.lineSeparator());
        if(!comment.equals(Settings.EMPTY_STRING)) {
            stringProperty.setValue(comment);
            ((Node)event.getSource()).getScene().getWindow().hide();
        } else {
            // set textarea border color to red! - animation
        }
    }
}