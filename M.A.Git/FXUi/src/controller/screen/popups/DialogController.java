package controller.screen.popups;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import magit.utils.MyBooleanProperty;
import settings.Settings;

public class DialogController {
    private String question;
    private StringProperty property;
    private boolean hasCheckBoxOn = false;
    private MyBooleanProperty booleanProperty = new MyBooleanProperty();
    @FXML
    private Button buttonOK, ButtonClick;
    @FXML
    private TextFlow textLabel;
    @FXML
    private TextField textField;
    @FXML
    private CheckBox anotherOption;

    @FXML
    public void initialize() {
        textField.setOnKeyPressed((event) -> {
            if (event.getCode() == KeyCode.ENTER) {
                confirmDialog();
            }
        });
        textField.requestFocus();
    }

    public void setCheckBoxData(String text, MyBooleanProperty booleanProperty) {
        this.booleanProperty = booleanProperty;
        this.anotherOption.setText(text);
        this.anotherOption.setVisible(true);
        this.hasCheckBoxOn = true;
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


    @FXML
    void onOKButtonKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            confirmDialog();
        }
    }

    private void confirmDialog() {
        String result = textField.getText();
        if (!result.equals(Settings.EMPTY_STRING)) {
            if (hasCheckBoxOn && anotherOption.isSelected()) {
                booleanProperty.set(true);
            }
            Stage stage = (Stage) ButtonClick.getScene().getWindow();
            stage.close();
            property.setValue(result);
        }
    }

    public void setProperty(StringProperty string) {
        property = string;
    }
}
