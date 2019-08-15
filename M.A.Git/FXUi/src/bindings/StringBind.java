package bindings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StringBind {
    final private StringProperty value = new SimpleStringProperty();

    public StringProperty valueProperty() {
        return value;
    }

    public String getValue() {
        return value.getValue();
    }

    public void setValue(String string) {
        value.setValue(string);
    }
}
