package com.magit.exceptions;

import com.magit.settings.LangEN;

public enum eErrorCodes {
    REPOSITORY_FOLDER_SCAN_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.SCAN_FOLDER_FAILED;
        }
    },
    CREATE_MAGIT_FOLDER_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.CREATE_MAGIT_FOLDER_FAILED;
        }
    },
    CREATE_ZIP_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.CREATE_ZIP_FILE_FAILED;
        }
    },
    OPEN_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.OPEN_FILE_FAILED;
        }
    },
    INSERT_FILE_TO_ZIP_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.INSERT_FILE_TO_ZIP_FAILED;
        }
    },
    WRITE_TO_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.WRITE_TO_FILE_FAILED;
        }
    },
    CREATE_TEMP_FOLDER_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.CREATE_TEMP_FILE_FAILED;
        }
    },
    OPEN_BRANCH_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.OPEN_BRANCH_FILE_FAILED;
        }
    },
    READ_FROM_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.READ_FROM_FILE_FAILED;
        }
    },
    FILE_NOT_EXIST {
        @Override
        public String getPersonalMessage() {
            return LangEN.FILE_NOT_EXIST;
        }
    },
    ERROR_LOAD_REPOSITORY {
        @Override
        public String getPersonalMessage() {
            return LangEN.ERROR_LOAD_REPOSITORY;
        }
    },
    CLOSE_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return LangEN.CLOSE_FILE_FAILED;
        }
    },
    BRANCH_ALREADY_EXIST {
        @Override
        public String getPersonalMessage() {
            return LangEN.BRANCH_ALREADY_EXIST;
        }
    },
    CANNOT_RECOVER_BRANCH {
        @Override
        public String getPersonalMessage() {
            return LangEN.CANNOT_RECOVER_BRANCH;
        }
    },
    MAGIT_FOLDER_ALREADY_EXIST {
        @Override
        public String getPersonalMessage() {
            return LangEN.MAGIT_FOLDER_ALREADY_EXIST;
        }
    };

    public String getPersonalMessage() {
        return "ERROR";
    }
}
