package com.magit.engine;

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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<BasicFile, Blob> entry : fileBlobMap.entrySet()) {
            Blob blob = entry.getValue();
            String type = blob.getType() == eFileTypes.FOLDER ? "folder" : "file";
            stringBuilder.append(String.format("%s,%s,%s,%s,%s",
                    blob.getName(),
                    blob.getSHA_ONE(),
                    type,
                    blob.getEditorName(),
                    blob.getDate().toString()));
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
            temp = new Folder(f.toPath(), "NONE");
        } else {
            temp = new Blob(f.toPath(), "NONE");
        }
        return fileBlobMap.get(temp);
    }
}
