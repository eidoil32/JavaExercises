package controller;

import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import magit.Branch;
import magit.utils.SmartListener;

public class SelectController {
    private Stage stage;
    private SmartListener listener;
    private BooleanProperty flag;

    @FXML private ChoiceBox<Object> selectChoiceBox;
    @FXML private Button btnOK, btnCancel;
    @FXML private TextFlow textLabel;

    @FXML
    private void onButtonCancel_Click(ActionEvent event) {
        listener.setItem(null);
    }

    @FXML
    private void onButtonOK_Click(ActionEvent event) {
        Object selectedItem = selectChoiceBox.getValue();
        if(selectedItem != null) {
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

    public void setListForChoice(ObservableList<Branch> observableList) {
        for (Branch branch : observableList) {
            if(!branch.isHead())
                selectChoiceBox.getItems().add(branch);
        }
    }

    public void setFlag(BooleanProperty flag) {
        this.flag = flag;
    }
}