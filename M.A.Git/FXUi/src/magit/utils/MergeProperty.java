package magit.utils;

import exceptions.eErrorCodes;
import javafx.beans.property.SimpleIntegerProperty;
import utils.eUserMergeChoice;

public class MergeProperty extends SimpleIntegerProperty {
    private OnSetValueListener value;
    private eUserMergeChoice choice;
    private String content;
    private eErrorCodes errorCode;
    private boolean isError = false;

    @Override
    public void set(int newValue) {
        super.set(newValue);
        if (value != null) {
            value.onValueSet(newValue);
            choice = eUserMergeChoice.getItem(newValue).get();
        }
    }

    public void setValueListener(OnSetValueListener valueListener) {
        this.value = valueListener;
    }

    public void setChoice(eUserMergeChoice choice) {
        this.choice = choice;
    }

    public void setError(eErrorCodes errorCode) {
        this.errorCode = errorCode;
        this.isError = true;
    }

    public boolean isInError() {
        return isError;
    }

    public eErrorCodes getErrorCode() {
        return errorCode;
    }

    public interface OnSetValueListener {
        void onValueSet(int value);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public eUserMergeChoice getChoice() {
        return choice;
    }
}
