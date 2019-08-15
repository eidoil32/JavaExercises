package controller;

import bindings.StringBind;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import settings.Settings;

public class DialogController {
    private String question;
    private StringBind property;
    @FXML private Button buttonOK, ButtonClick;
    @FXML private Label textLabel;
    @FXML private TextField textField;

    @FXML
    void onButtonCancelClick(ActionEvent event) {
        Stage stage = (Stage) ButtonClick.getScene().getWindow();
        stage.close();
    }

    public void setQuestion(String question) {
        this.question = question;
        textLabel.setText(question);
    }

    @FXML
    void onButtonOKClick(ActionEvent event) {
        String result = textField.getText();
        if(!result.equals(Settings.EMPTY_STRING)) {
            Stage stage = (Stage) ButtonClick.getScene().getWindow();
            stage.close();
            property.setValue(result);
        }
    }

    public void setProperty(StringBind repositoryName) {
        property = repositoryName;
    }
}
