package utils;

import magit.BasicFile;
import magit.BlobMap;
import magit.Folder;
import settings.Settings;

public enum MapKeys {
    LIST_DELETED(0) {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.remove(file);
        }

        @Override
        public String toString() {
            return Settings.language.getString("SHOW_STATUS_DELETED");
        }
    }, LIST_NEW(1) {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.addNew(file,root);
        }

        @Override
        public String toString() {
            return Settings.language.getString("SHOW_STATUS_NEW");
        }
    }, LIST_CHANGED(1) {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.replace(file,root);
        }

        @Override
        public String toString() {
            return Settings.language.getString("SHOW_STATUS_EDITED");
        }
    };

    private int place;

    MapKeys(int place) {
        this.place = place;
    }

    public int getPlace() {
        return place;
    }

    public abstract void execute(BlobMap map, BasicFile file, Folder root);
}
