package com.magit.engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.TreeMap;

public class Folder extends Blob {
    private BlobMap blobMap;

    public Folder(Path filePath, String editor) throws IOException {
        super(filePath, editor);
        setType(eFileTypes.FOLDER);
        // this line is to create the Comparator of BasicFile names :
        blobMap = new BlobMap(new TreeMap<>(Comparator.comparing(BasicFile::getFullPathName)));
    }

    public void AddBlob(Blob blob) {
        blobMap.getFileBlobMap().put(blob, blob);
    }

    public BlobMap getBlobMap() {
        return blobMap;
    }

    public void setBlobMap(BlobMap blobMap) {
        this.blobMap = blobMap;
    }
}
