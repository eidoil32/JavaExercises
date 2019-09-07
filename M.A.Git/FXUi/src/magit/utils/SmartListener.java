package magit.utils;

import javafx.util.StringConverter;

public class SmartListener<T> {
    private T item;
    private StringConverter<Object> converter = null;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public StringConverter<Object> getConverter() {
        return converter;
    }

    public void setConverter(StringConverter<Object> converter) {
        this.converter = converter;
    }
}
