package magit;

import settings.Settings;

public enum eFileTypes {
    FILE {
        @Override
        public String toString() {
            return Settings.language.getString("TYPE_STRING_BLOB");
        }
    }, FOLDER{
        @Override
        public String toString() {
            return Settings.language.getString("TYPE_STRING_FOLDER");
        }
    }
}
