package com.magit.engine;

import com.magit.utils.FileManager;
import org.apache.commons.codec.digest.DigestUtils;
import java.io.IOException;
import java.nio.file.Path;

public class Blob extends BasicFile {
    private String content;
    private String SHA_ONE;
    private Path filePath;

    public Blob(Path filePath, String editor) throws IOException {
        super(filePath.getFileName().toString(),editor, eFileTypes.FILE);
        this.content = FileManager.ReadFile(filePath);
        this.filePath = filePath;
        this.SHA_ONE = DigestUtils.sha1Hex(content);
    }

    @Override
    public String toString() {
        return  super.toString() +
                "\nFile content: " + content +
                "\nFile SHA-1: " + SHA_ONE +
                "\nFile Path: " + filePath.toString();
    }
}
