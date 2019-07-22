package com.magit.engine;

import java.util.Date;

public abstract class BasicFile {
    private String name, editorName;
    private Date date;
    private eFileTypes type;

    public BasicFile(String name, String editorName, eFileTypes type) {
        this.name = name;
        this.date = new Date();
        this.editorName = editorName;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEditorName() {
        return editorName;
    }

    public void setEditorName(String editorName) {
        this.editorName = editorName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public eFileTypes getType() {
        return type;
    }

    public void setType(eFileTypes type) {
        this.type = type;
    }
}
