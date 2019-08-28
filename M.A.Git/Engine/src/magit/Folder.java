package magit;

import exceptions.MyXMLException;
import exceptions.eErrorCodesXML;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import xml.basic.Item;
import xml.basic.MagitBlob;
import xml.basic.MagitRepository;
import xml.basic.MagitSingleFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Folder extends Blob {
    private BlobMap blobMap;

    public Folder(Path filePath, String editor) throws IOException {
        super(filePath, editor);
        setType(eFileTypes.FOLDER);
        // this line is to create the Comparator of BasicFile names :
        blobMap = new BlobMap(new TreeMap<>(Comparator.comparing(BasicFile::getFullPathName)));
    }

    public Folder() {
    } // empty c'tor for recovering from SHA-1

    private Folder(MagitSingleFolder folder, Folder root, String path) throws MyXMLException {
        this.type = eFileTypes.FOLDER;
        this.editorName = folder.getLastUpdater();
        this.rootFolder = root;
        this.name = folder.getName();
        if(this.name == null) {
            this.name = new File(path).getName();
            this.fullPathName = path;
        } else {
            this.fullPathName = path + File.separator + name;
        }
        this.filePath = Paths.get(fullPathName);
        try {
            this.date = new SimpleDateFormat(Settings.DATE_FORMAT).parse(folder.getLastUpdateDate());
        } catch (ParseException e) {
            throw new MyXMLException(eErrorCodesXML.WRONG_DATE_FORMAT,folder.getLastUpdateDate());
        }

    }

    private static MagitSingleFolder findFolderByItem(List<MagitSingleFolder> allFolders, Item item) throws MyXMLException {
        for (MagitSingleFolder folder : allFolders) {
            if(folder.getId().equals(item.getId())) {
                return folder;
            }
        }
        throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_FOLDER, item.getId());
    }

    public static Folder XML_Parser(MagitSingleFolder root, MagitRepository xmlMagit, Folder root_Folder, String path)
            throws MyXMLException {
        BlobMap rootBlobMap = new BlobMap(new HashMap<>());
        Folder rootFolder = new Folder(root, root_Folder, path);

        List<MagitBlob> blobs = xmlMagit.getMagitBlobs().getMagitBlob();
        List<MagitSingleFolder> folders = xmlMagit.getMagitFolders().getMagitSingleFolder();

        rootFolder.setBlobMap(rootBlobMap);
        List<Item> subFiles = root.getItems().getItem();

        for (Item item : subFiles) {
            if(item.getType().equals(Settings.XML_ITEM_FILE_TYPE)) {
                rootBlobMap.addToMap(Blob.XML_Parser(blobs,item.getId(),rootFolder));
            } else if (item.getType().equals(Settings.XML_ITEM_FOLDER_TYPE)) {
                MagitSingleFolder temp = Folder.XML_BasicParser(folders,item.getId());
                if(temp.isIsRoot()) {
                    throw new MyXMLException(eErrorCodesXML.DUPLICATE_ROOT_FOLDER,root.getName());
                }
                rootBlobMap.addToMap(XML_Parser(temp,xmlMagit,rootFolder,rootFolder.fullPathName));
            } else {
                throw new MyXMLException(eErrorCodesXML.ITEM_WITH_UNKNOWN_TYPE,item.getType());
            }
        }

        rootFolder.calcFolderSHAONE();

        return rootFolder;
    }

    private static MagitSingleFolder XML_BasicParser(List<MagitSingleFolder> folders, String id) throws MyXMLException {
        for (MagitSingleFolder folder : folders) {
            if(folder.getId().equals(id)){
                return folder;
            }
        }
        throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_FOLDER,id);
    }

    private static MagitSingleFolder parseByID(List<MagitSingleFolder> folders, String id) throws MyXMLException {
        for (MagitSingleFolder folder : folders) {
            if (folder.getId().equals(id)) {
                return folder;
            }
        }
        throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_BLOB, id);
    }

    public static MagitSingleFolder findRootFolder(List<MagitSingleFolder> folders, String id) throws MyXMLException {
        for (MagitSingleFolder folder : folders) {
            if (folder.isIsRoot() && folder.getId().equals(id)) {
                return folder;
            }
        }
        throw new MyXMLException(eErrorCodesXML.COMMIT_POINT_TO_NONE_ROOT_FOLDER, null);
    }

    public static List<MagitBlob> getBlobs(MagitSingleFolder root, List<MagitBlob> blobs) throws MyXMLException {
        List<Item> subItems = root.getItems().getItem();
        List<MagitBlob> subBlobs = new LinkedList<>();
        for (Item item : subItems) {
            MagitBlob temp = findBlobById(blobs,item);
            subBlobs.add(temp);
        }

        return subBlobs;
    }

    private static MagitBlob findBlobById(List<MagitBlob> blobs, Item item) throws MyXMLException {
        for (MagitBlob blob : blobs) {
            if(blob.getId().equals(item.getId())) {
                return blob;
            }
        }
        throw new MyXMLException(eErrorCodesXML.FOLDER_POINT_TO_NONSEXIST_BLOB, item.getId());
    }

    public static List<MagitSingleFolder> getFolders(MagitSingleFolder root, List<MagitSingleFolder> allFolders) throws MyXMLException {
        List<Item> subItems = root.getItems().getItem();
        List<MagitSingleFolder> folders = new LinkedList<>();
        for (Item item : subItems) {
            MagitSingleFolder temp = findFolderByItem(allFolders,item);
            folders.add(temp);
        }

        return folders;
    }

    public void AddBlob(Blob blob) {
        blobMap.getMap().put(blob, blob);
    }

    public BlobMap getBlobMap() {
        return blobMap;
    }

    public void setBlobMap(BlobMap blobMap) {
        this.blobMap = blobMap;
    }

    public void calcFolderSHAONE() {
        for (Map.Entry<BasicFile, Blob> entry : blobMap.getMap().entrySet()) {
            if (entry.getValue().getType() == eFileTypes.FOLDER)
                ((Folder) entry.getValue()).calcFolderSHAONE();
        }
        this.setContent(blobMap.getBasicData());
        this.setSHA_ONE(DigestUtils.sha1Hex(getContent()));
    }

    public void completeMissingData(String[] row, String path, Folder rootFolder) {
        this.name = row[0];
        this.SHA_ONE = row[1];
        this.fullPathName = path + File.separator + name;
        this.filePath = Paths.get(fullPathName);
        this.editorName = row[3];
        String date = row[4];
        SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT);
        try {
            this.date = dateFormat.parse(date);
        } catch (ParseException e) {
            this.date = new Date();
        }

        this.type = eFileTypes.FOLDER;
        this.rootFolder = rootFolder;
    }
}
