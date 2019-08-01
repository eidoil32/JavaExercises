package magit;

import org.apache.commons.codec.digest.DigestUtils;
import utils.FileManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Blob extends BasicFile {
    private String content;
    private String SHA_ONE;
    private Path filePath;

    public Blob(Path filePath, String editor) throws IOException {
        super(filePath.toString(), filePath.getFileName().toString(), editor, eFileTypes.FILE);
        if (!new File(filePath.toString()).isDirectory()) {
            this.content = FileManager.readFile(filePath);
            this.SHA_ONE = DigestUtils.sha1Hex(content);
        }
        this.filePath = filePath;
    }

    public Blob() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(List<String> contentFromFile)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : contentFromFile)
        {
            stringBuilder.append(string);
        }
        this.content = stringBuilder.toString();
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public void setSHA_ONE(String SHA_ONE) {
        this.SHA_ONE = SHA_ONE;
    }

    public Path getFilePath() {
        return filePath;
    }

    public void setFilePath(Path filePath) {
        this.filePath = filePath;
        this.setFullPathName(filePath.toString());
    }
}
