package magit;

import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.eErrorCodes;
import exceptions.eErrorCodesXML;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.FileManager;
import xml.basic.MagitBlob;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Blob extends BasicFile {
    protected String content;
    protected String SHA_ONE;
    protected Path filePath;

    public Blob(Path filePath, String editor) throws IOException {
        super(filePath.toString(), filePath.getFileName().toString(), editor, eFileTypes.FILE);
        if (!new File(filePath.toString()).isDirectory()) {
            this.content = FileManager.readFile(filePath);
            this.SHA_ONE = DigestUtils.sha1Hex(content);
        }
        this.filePath = filePath;
    }

    public Blob() {
    }

    public Blob(MagitBlob blob, String pathToFile, Folder rootFolder) {
        try {
            this.date = new SimpleDateFormat(Settings.DATE_FORMAT).parse(blob.getLastUpdateDate());
        } catch (ParseException e) {
            this.date = new Date();
        }
        this.editorName = blob.getLastUpdater();
        this.content = blob.getContent();
        this.SHA_ONE = DigestUtils.sha1Hex(this.content);
        this.name = blob.getName();
        this.fullPathName = pathToFile + File.separator + this.name;
        this.filePath = Paths.get(fullPathName);
        this.rootFolder = rootFolder;
        this.type = eFileTypes.FILE;
    }

    public static Blob BlobFactory(Path objectPath, String[] row, Folder rootFolder) throws IOException, MyFileException {
        Blob blob = new Blob();
        File file = FileManager.unZipFile(new File(objectPath + File.separator + row[1]), objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
        blob.setRootFolder(rootFolder);
        blob.setEditorName(row[3]);
        String date = row[4];
        SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT);
        try {
            blob.setDate(dateFormat.parse(date));
        } catch (ParseException e) {
            blob.setDate(new Date());
        }

        blob.setContent(FileManager.readFile(file.toPath()));
        blob.setSHA_ONE(row[1]);
        blob.setFilePath(Paths.get(rootFolder.getFilePath() + File.separator + row[0]));
        blob.setName(row[0]);
        blob.setType(eFileTypes.FILE);
        if (!file.delete()) {
            throw new MyFileException(eErrorCodes.DELETE_FILE_FAILED, file.getPath());
        }
        return blob;
    }

    protected static Blob findBlobById(List<MagitBlob> blobs, String id, Folder rootFolder) throws MyXMLException {
        for (MagitBlob blob : blobs) {
            if (blob.getId().equals(id)) {
                return new Blob(blob,rootFolder.getFullPathName(),rootFolder);
            }
        }
        throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_BLOB, id);
    }

    protected static Blob XML_Parser(List<MagitBlob> blobs, String id, Folder rootFolder) throws MyXMLException {
        Blob temp = new Blob();
        MagitBlob current = null;
        for (MagitBlob blob : blobs) {
            if(blob.getId().equals(id)) {
                current = blob;
                break;
            }
        }
        if(current == null) {
            throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_BLOB,id);
        }
        temp.rootFolder = rootFolder;
        temp.type = eFileTypes.FILE;
        temp.name = current.getName();
        temp.filePath = Paths.get(rootFolder.getFilePath() + File.separator + temp.name);
        temp.content = current.getContent();
        temp.SHA_ONE = DigestUtils.sha1Hex(temp.content);
        temp.fullPathName = temp.filePath.toString();
        temp.editorName = current.getLastUpdater();
        try {
            temp.date = new SimpleDateFormat(Settings.DATE_FORMAT).parse(current.getLastUpdateDate());
        } catch (ParseException e) {
            throw new MyXMLException(eErrorCodesXML.WRONG_DATE_FORMAT,current.getLastUpdateDate());
        }

        return temp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContent(List<String> contentFromFile) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String string : contentFromFile) {
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

    @Override
    public String toString() {
        return Settings.language.getString("FULL_PATH") + fullPathName + System.lineSeparator()
                + Settings.language.getString("BASIC_FILE_TYPE") + type + System.lineSeparator()
                + Settings.language.getString("BASIC_FILE_SHA_ONE") + SHA_ONE + System.lineSeparator()
                + Settings.language.getString("BASIC_FILE_EDITOR") + editorName + System.lineSeparator()
                + Settings.language.getString("BASIC_FILE_DATE") + date + System.lineSeparator();
    }
}
