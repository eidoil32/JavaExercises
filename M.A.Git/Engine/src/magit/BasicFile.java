package magit;

import java.io.File;
import java.util.Date;

public abstract class BasicFile {
    private String name, editorName, fullPathName;
    private Date date;
    private eFileTypes type;
    private Folder rootFolder;

    public BasicFile(String fullPathName, String name, String editorName, eFileTypes type) {
        this.fullPathName = fullPathName;
        this.name = name;
        this.date = new Date();
        this.editorName = editorName;
        this.type = type;
    }

    public BasicFile() {}

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

    @Override
    public String toString() {
        return rootFolder == null ? name : rootFolder + File.separator + name;
    }

    public void updateAllChain(String currentUser) {
        this.editorName = currentUser;
        if (rootFolder != null)
            rootFolder.updateAllChain(currentUser);
    }

    public Folder tryParseFolder()
    {
        if(this instanceof Folder)
            return (Folder) this;
        else
            return null;
    }

    public Blob tryParseBlob()
    {
        if(this instanceof Blob)
            return (Blob) this;
        else
            return null;
    }
}
