package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import magit.Magit;
import settings.Settings;

public class SettingsController {
    private Controller mainController;
    private Magit model;

    @FXML
    private Label styleHint, languagesHint;
    @FXML
    private ChoiceBox<String> styleChoiceBox, languageChoiceBox;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        styleHint.setTooltip(new Tooltip(Settings.language.getString("STYLE_HINT")));
        languagesHint.setTooltip(new Tooltip(Settings.language.getString("LANGUAGE_HINT")));
        styleChoiceBox.getItems().addAll(Settings.THEME_WHITE,Settings.THEME_BLACK);
        languageChoiceBox.getItems().addAll(Settings.LANG_ENG,Settings.LANG_HEB);
    }

    public void setMainController(Controller mainController) {
        this.mainController = mainController;
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    @FXML
    void onSaveButton_Click(ActionEvent event) {
        String language = languageChoiceBox.getValue(), style = styleChoiceBox.getValue();
        System.out.println("Language: " + language + " Style: " + style);
    }
}