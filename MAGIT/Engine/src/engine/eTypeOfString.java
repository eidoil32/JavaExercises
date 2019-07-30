package com.magit.engine;

public enum eTypeOfString {
    T_COMMIT {
        public boolean valid(String string) {
            return string.length() >= 5 && string.length() <= 1000;
        }
    }, T_PATH {
        public boolean valid(String string) {
            return true;
        }
    }, T_REPOSITORY {
        public boolean valid(String string) {
            return true;
        }
    }, T_BRANCH {
        public boolean valid(String string) {
            return true;
        }
    };

    abstract public boolean valid(String string);
}