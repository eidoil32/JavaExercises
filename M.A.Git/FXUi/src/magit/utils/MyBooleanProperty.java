package magit.utils;

import javafx.beans.property.SimpleBooleanProperty;

public class MyBooleanProperty extends SimpleBooleanProperty {
    private OnSetValueListener value;
    private String additionalData;

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
