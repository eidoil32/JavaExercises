package com.magit.engine;

import java.util.Date;

public abstract class BasicFile {
    private String name, editorName, fullPathName;
    private Date date;
    private eFileTypes type;
    private int level;
    private Folder rootFolder;

    public BasicFile(String fullPathName, String name, String editorName, eFileTypes type) {
        this.fullPathName = fullPathName;
        this.name = name;
        this.date = new Date();
        this.editorName = editorName;
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getFullPathName() {
        return fullPathName;
    }

    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
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

    @Override
    public int hashCode() {
        return fullPathName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BasicFile && fullPathName.equals(((BasicFile) obj).getFullPathName());
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Folder rootFolder) {
        this.rootFolder = rootFolder;
    }
}
