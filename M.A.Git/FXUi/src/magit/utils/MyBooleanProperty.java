package magit.utils;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleBooleanProperty;

public class MyBooleanProperty extends SimpleBooleanProperty {
    private OnSetValueListener value;
    private String additionalData;

    public MyBooleanProperty() {
        super();
    }

    public MyBooleanProperty(InvalidationListener function) {
        super();
        addListener(function);
    }

    @Override
    public void set(boolean newValue) {
        super.set(newValue);
        if(value != null) {
            value.onValueSet(newValue);
        }
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public void setValueListener(OnSetValueListener valueListener) {
        this.value = valueListener;
    }

    public interface OnSetValueListener {
        void onValueSet(boolean value);
    }
}
