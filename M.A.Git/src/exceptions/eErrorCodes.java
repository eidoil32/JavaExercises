package exceptions;

public enum eErrorCodes {
    REPOSITORY_FOLDER_SCAN_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.SCAN_FOLDER_FAILED;
        }
    },
    CREATE_MAGIT_FOLDER_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.CREATE_MAGIT_FOLDER_FAILED;
        }
    },
    CREATE_ZIP_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.CREATE_ZIP_FILE_FAILED;
        }
    },
    OPEN_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.OPEN_FILE_FAILED;
        }
    },
    INSERT_FILE_TO_ZIP_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.INSERT_FILE_TO_ZIP_FAILED;
        }
    },
    WRITE_TO_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.WRITE_TO_FILE_FAILED;
        }
    },
    CREATE_TEMP_FOLDER_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.CREATE_TEMP_FILE_FAILED;
        }
    },
    OPEN_BRANCH_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.OPEN_BRANCH_FILE_FAILED;
        }
    },
    READ_FROM_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.READ_FROM_FILE_FAILED;
        }
    },
    FILE_NOT_EXIST {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.FILE_NOT_EXIST;
        }
    },
    ERROR_LOAD_REPOSITORY {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.ERROR_LOAD_REPOSITORY;
        }
    },
    CLOSE_FILE_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.CLOSE_FILE_FAILED;
        }
    },
    BRANCH_ALREADY_EXIST {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.BRANCH_ALREADY_EXIST;
        }
    },
    CANNOT_RECOVER_BRANCH {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.CANNOT_RECOVER_BRANCH;
        }
    },
    MAGIT_FOLDER_ALREADY_EXIST {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.MAGIT_FOLDER_ALREADY_EXIST;
        }
    },

    PARSE_BLOB_TO_FOLDER_FAILED {
        @Override
        public String getPersonalMessage() {
            return ExceptionLang.PARSE_BLOB_TO_FOLDER_FAILED;
        }
    };

    public String getPersonalMessage() {
        return "ERROR";
    }
}
