package controller;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import settings.Settings;

public class DialogController {
    private String question;
    private StringProperty property;
    @FXML
    private Button buttonOK, ButtonClick;
    @FXML
    private TextFlow textLabel;
    @FXML
    private TextField textField;

    @FXML
    public void initialize() {
        textField.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                confirmDialog();
            }
        });
        textField.requestFocus();
    }

    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    @FXML
    private void onButtonCancelClick(ActionEvent event) {
        Stage stage = (Stage) ButtonClick.getScene().getWindow();
        stage.close();
    }

    public void setQuestion(String question) {
        this.question = question;
        textLabel.getChildren().add(new Text(question));
    }

    @FXML
    private void onButtonOKClick(ActionEvent event) {
        confirmDialog();
    }

    private void confirmDialog() {
        String result = textField.getText();
        if (!result.equals(Settings.EMPTY_STRING)) {
            Stage stage = (Stage) ButtonClick.getScene().getWindow();
            stage.close();
            property.setValue(result);
        }
    }

    public void setProperty(StringProperty string) {
        property = string;
    }
}
