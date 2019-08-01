package utils;

import magit.BasicFile;
import magit.BlobMap;
import magit.Folder;

public enum MapKeys {
    LIST_DELETED {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.remove(file);
        }

        @Override
        public String toString() {
            return Settings.SHOW_STATUS_DELETED;
        }
    }, LIST_NEW {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.addNew(file,root);
        }

        @Override
        public String toString() {
            return Settings.SHOW_STATUS_NEW;
        }
    }, LIST_CHANGED {
        @Override
        public void execute(BlobMap map, BasicFile file, Folder root) {
            map.replace(file,root);
        }

        @Override
        public String toString() {
            return Settings.SHOW_STATUS_EDITED;
        }
    };

    public abstract void execute(BlobMap map, BasicFile file, Folder root);
}
