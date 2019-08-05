package magit;

import settings.Settings;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class BlobMap {
    private Map<BasicFile, Blob> map;

    public BlobMap(Map<BasicFile, Blob> fileBlobMap) {
        if (fileBlobMap != null)
            this.map = fileBlobMap;
        else
            this.map = new HashMap<>();
    }

    public Map<BasicFile, Blob> getMap() {
        return map;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
            Blob blob = entry.getValue();
            String type = blob.getType() == eFileTypes.FOLDER ? Settings.FOLDER_TYPE_IN_FOLDER_TABLE : Settings.FILE_TYPE_IN_FOLDER_TABLE;
            stringBuilder
                    .append(blob.getName()).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getSHA_ONE()).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(type).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getEditorName()).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(new SimpleDateFormat(Settings.DATE_FORMAT).format(blob.getDate())).append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    public void addToMap(Blob blob) {
        map.put(blob,blob);
    }

    public void remove(BasicFile file) {
        if (map.remove(file) == null) //file not exist in map
        {
            for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
                if (entry.getValue().getType() == eFileTypes.FOLDER) {
                    ((Folder) entry.getValue()).getBlobMap().remove(file);
                }
            }
        }
    }

    public boolean replace(BasicFile file, Folder root)
    {
        if(file.getRootFolder().equals(root)) {
            map.remove(file);
            map.put(file, (Blob) file);
            return true;
        }
        else {
            for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
                if (entry.getValue().getType() == eFileTypes.FOLDER) {
                    Folder myRoot = (Folder) entry.getValue();
                    boolean result = myRoot.getBlobMap().replace(file,myRoot);
                    if(myRoot.getRootFolder().equals(root) && result)
                    {
                        Folder temp = (Folder)map.get(myRoot);
                        BlobMap tempBlob = temp.getBlobMap();
                        map.remove(myRoot);
                        map.put(myRoot,myRoot);
                        myRoot.setBlobMap(tempBlob);
                    }
                }
            }
        }
        return false;
    }

    public void addNew(BasicFile file, Folder root) {
        if(file.getRootFolder().equals(root))
            map.put(file,(Blob)file);
        else {
            for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
                if (entry.getValue().getType() == eFileTypes.FOLDER) {
                    ((Folder) entry.getValue()).getBlobMap().addNew(file,(Folder) entry.getValue());
                }
            }
        }
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public String getBasicData() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
            Blob blob = entry.getValue();
            stringBuilder
                    .append(blob.getName()).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(blob.getSHA_ONE()).append(Settings.FOLDER_TABLE_DELIMITER)
                    .append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }
}
