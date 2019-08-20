package magit.utils;

import javafx.beans.property.SimpleBooleanProperty;

public class MyBooleanProperty extends SimpleBooleanProperty {
    private OnSetValueListener value;

    @Override
    public void set(boolean newValue) {
        super.set(newValue);
        if(value != null) {
            value.onValueSet(newValue);
        }
    }

    public void setValueListener(OnSetValueListener valueListener) {
        this.value = valueListener;
    }

    public interface OnSetValueListener {
        void onValueSet(boolean value);
    }
}
