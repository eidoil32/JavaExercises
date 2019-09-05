package controller.screen.popups;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import magit.utils.SmartListener;
import settings.Settings;

public class SelectController {
    private Stage stage;
    private SmartListener listener;
    private BooleanProperty flag;

    @FXML
    private ChoiceBox<Object> selectChoiceBox;
    @FXML
    private Button btnOK, btnCancel;
    @FXML
    private TextFlow textLabel;

    @FXML
    private void initialize() {
        selectChoiceBox.setMaxWidth(Settings.MAGIT_UI_SELECT_POPUP_WIDTH - 10);
    }

    @FXML
    private void onButtonCancel_Click(ActionEvent event) {
        listener.setItem(null);
        stage.close();
    }

    @FXML
    private void onButtonOK_Click(ActionEvent event) {
        Object selectedItem = selectChoiceBox.getValue();
        if (selectedItem != null) {
            listener.setItem(selectedItem);
            stage.close();
            flag.setValue(true);
        }
        //else nothing selected - color red...
    }

    public void setListener(SmartListener listener) {
        this.listener = listener;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setQuestion(String question) {
        textLabel.getChildren().add(new Text(question));
    }

    public void setListForChoice(ObservableList<Object> observableList) {
        for (Object object : observableList) {
            selectChoiceBox.getItems().add(object);
        }
    }

    public void setConverter(StringConverter<Object> converter) {
        selectChoiceBox.setConverter(converter);
    }

    public void setFlag(BooleanProperty flag) {
        this.flag = flag;
    }
}