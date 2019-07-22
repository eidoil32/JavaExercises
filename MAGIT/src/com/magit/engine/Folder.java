package com.magit.engine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Folder extends Blob {
    private Map<BasicFile,Blob> blobs;

    public Folder(Path filePath, String editor) throws IOException {
        super(filePath,editor);
        setType(eFileTypes.FOLDER);
        blobs = new TreeMap<>(Comparator.comparing(BasicFile::getName)); // this line is to create the Comparator of BasicFile names
    }

    public void AddBlob(Blob blob)
    {
        blobs.put(blob,blob);
    }
}
