package controller.screen.popups;

import controller.screen.intro.IntroController;
import controller.screen.main.MainController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import magit.Magit;
import settings.Settings;

public class SettingsController {
    private MainController mainController;
    private Magit model;
    private Stage stage;
    private BooleanProperty languageProperty, themeProperty;
    private StringProperty currentState;

    @FXML
    private Label styleHint, languagesHint;
    @FXML
    private ChoiceBox<String> languageChoiceBox;
    @FXML
    private Button saveButton, defaultButton;
    @FXML
    private ColorPicker styleChoiceBox;

    @FXML
    public void initialize() {
        int index;
        styleHint.setTooltip(new Tooltip(Settings.language.getString("STYLE_HINT")));
        languagesHint.setTooltip(new Tooltip(Settings.language.getString("LANGUAGE_HINT")));
        languageChoiceBox.getItems().add(0, Settings.LANG_HEB);
        languageChoiceBox.getItems().add(1, Settings.LANG_ENG);
        index = Settings.currentLanguage.equals(Settings.HEBREW_CODE) ? 0 : 1;
        languageChoiceBox.getSelectionModel().select(index);
        styleChoiceBox.setValue(Settings.CURRENT_THEME_COLOR);
        styleChoiceBox.setOnAction(event -> editCustomCSSFile());
    }

    private void editCustomCSSFile() {
        InputStream in = getClass().getClassLoader().getResourceAsStream(Settings.FXML_THEME_CUSTOM_CSS_FILE);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while ((line = reader.readLine()) != null) {
                if (i == 2) {
                    String standard = getColorCode(styleChoiceBox.getValue());
                    line = "    -standard                    : " + standard + ";";
                } else if (i == 3) {
                    String light = getColorCode(styleChoiceBox.getValue().brighter());
                    line = "    -bright                    : " + light + ";";
                }
                sb.append(line).append("\n");
                i++;
            }
            PrintWriter writer =
                    new PrintWriter(
                            new File(Settings.FXML_THEME_CUSTOM_EXTERNAL_CSS_FILE));
            writer.write(sb.toString());
            writer.close();
        } catch (IOException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
        }
    }

    private String getColorCode(Color color) {
        return "#" + color.toString().substring(2, 8);
    }

    @FXML
    void onSaveButton_Click(ActionEvent event) {
        String language = languageChoiceBox.getValue();
        Color style = styleChoiceBox.getValue();
        if (style.equals(Color.WHITE)) {
            Settings.currentTheme = Settings.THEME_WHITE;
        } else {
            Settings.currentTheme = Settings.THEME_CUSTOM;
        }
        Settings.CURRENT_THEME_COLOR = style;
        if (isLanguagesChanges(language))
        {
            Settings.currentLanguage = language.equals(Settings.LANG_ENG) ? Settings.ENGLISH_CODE : Settings.HEBREW_CODE;
            languageProperty.setValue(true);
        }
        themeProperty.setValue(true);
        currentState.set(Settings.language.getString("LANGUAGE_CHANGES_SUCCESSFULLY"));
        stage.close();
    }

    private boolean isLanguagesChanges(String selectedLanguages) {
        String currentLang;
        if (Settings.currentLanguage.equals(Settings.ENGLISH_CODE)) {
            currentLang = Settings.LANG_ENG;
        } else {
            currentLang = Settings.LANG_HEB;
        }

        return !selectedLanguages.equals(currentLang);
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    public void setLanguageProperty(BooleanProperty languageProperty) {
        this.languageProperty = languageProperty;
    }

    public void setThemeProperty(BooleanProperty themeProperty) {
        this.themeProperty = themeProperty;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentState(StringProperty currentState) {
        this.currentState = currentState;
    }

    @FXML
    void onDefaultButton_Clicked(ActionEvent event) {
        Settings.currentTheme = Settings.THEME_WHITE;
        Settings.currentLanguage = ""; // reset to english language
        Settings.CURRENT_THEME_COLOR = Color.WHITE;
        themeProperty.set(true);
        languageProperty.set(true);
        currentState.setValue(Settings.language.getString("SETTINGS_SAVED_SUCCESSFULLY"));
        stage.close();
    }
}