package magit;

import utils.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BlobMap {
    private Map<BasicFile, Blob> fileBlobMap;

    public BlobMap(Map<BasicFile, Blob> fileBlobMap) {
        this.fileBlobMap = new HashMap<>();
    }

    public Map<BasicFile, Blob> getFileBlobMap() {
        return fileBlobMap;
    }

    public void setNewMap(Map<BasicFile, Blob> fileBlobMap)
    {
        this.fileBlobMap = fileBlobMap;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<BasicFile, Blob> entry : fileBlobMap.entrySet()) {
            Blob blob = entry.getValue();
            String type = blob.getType() == eFileTypes.FOLDER ? Settings.FOLDER_TYPE_IN_FOLDER_TABLE : Settings.FILE_TYPE_IN_FOLDER_TABLE;
            stringBuilder
                    .append(blob.getName())             .append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getSHA_ONE())          .append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(type)                       .append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getEditorName())       .append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getDate().toString())  .append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    public void addToMap(BasicFile key, BasicFile value, eFileTypes type) {
        switch (type) {
            case FILE:
                fileBlobMap.put(key, (Blob) value);
            case FOLDER:
                fileBlobMap.put(key, (Folder) value);
        }
    }

    public BasicFile getBlobByFile(File f) throws IOException {
        Blob temp;
        if (f.isDirectory()) {
            temp = new Folder(f.toPath(), Settings.USER_ADMINISTRATOR);
        } else {
            temp = new Blob(f.toPath(), Settings.USER_ADMINISTRATOR);
        }
        return fileBlobMap.get(temp);
    }
}
