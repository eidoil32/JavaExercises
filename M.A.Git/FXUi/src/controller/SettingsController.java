package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import magit.Magit;
import settings.Settings;

public class SettingsController {
    private Controller mainController;
    private Magit model;
    private Stage stage;
    private BooleanProperty languageProperty;
    private StringProperty currentState;

    @FXML
    private Label styleHint, languagesHint;
    @FXML
    private ChoiceBox<String> styleChoiceBox, languageChoiceBox;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        int index;
        styleHint.setTooltip(new Tooltip(Settings.language.getString("STYLE_HINT")));
        languagesHint.setTooltip(new Tooltip(Settings.language.getString("LANGUAGE_HINT")));
        languageChoiceBox.getItems().add(0, Settings.LANG_HEB);
        languageChoiceBox.getItems().add(1, Settings.LANG_ENG);
        index = Settings.currentLanguage.equals(Settings.HEBREW_CODE) ? 0 : 1;
        languageChoiceBox.getSelectionModel().select(index);
        index = Settings.currentTheme.equals(Settings.THEME_WHITE) ? 0 : 1;
        styleChoiceBox.getItems().add(0, Settings.THEME_WHITE);
        styleChoiceBox.getItems().add(1, Settings.THEME_BLACK);
        styleChoiceBox.getSelectionModel().select(index);
    }

    @FXML
    void onSaveButton_Click(ActionEvent event) {
        String language = languageChoiceBox.getValue(), style = styleChoiceBox.getValue();
        Settings.currentLanguage = language.equals(Settings.LANG_ENG) ? "" : Settings.HEBREW_CODE;
        Settings.currentTheme = style;
        languageProperty.setValue(true);
        currentState.setValue(Settings.language.getString("LANGUAGE_CHANGES_SUCCESSFULLY"));
        stage.close();
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    public void setLanguageProperty(BooleanProperty languageProperty) {
        this.languageProperty = languageProperty;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentState(StringProperty currentState) {
        this.currentState = currentState;
    }
}