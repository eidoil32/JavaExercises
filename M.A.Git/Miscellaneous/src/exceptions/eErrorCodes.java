package exceptions;

import settings.Settings;

public enum eErrorCodes {
    OPEN_CHANGES_NEEDS_PULL(Settings.language.getString("OPEN_CHANGES_NEEDS_PULL")),
    ONLY_ONE_RTB(Settings.language.getString("ONLY_ONE_RTB")),
    CANNOT_PULL_DATA_FOR_NONE_RTB(Settings.language.getString("CANNOT_PULL_DATA_FOR_NONE_RTB")),
    REMOTE_BRANCH_NOT_POINTED_ON_SAME_COMMIT(Settings.language.getString("REMOTE_BRANCH_NOT_POINTED_ON_SAME_COMMIT")),
    NOT_RTB_CANNOT_PUSH(Settings.language.getString("NOT_RTB_CANNOT_PUSH")),
    REMOTE_BRANCH_NOT_FOUND(Settings.language.getString("REMOTE_BRANCH_NOT_FOUND")),
    COPY_FILE_FROM_REMOTE_TO_LOCAL_FAILED(Settings.language.getString("COPY_FILE_FROM_REMOTE_TO_LOCAL_FAILED")),
    FAILED_RECOVER_FOLDER_CONTENT(Settings.language.getString("FAILED_RECOVER_FOLDER_CONTENT")),
    BRANCH_FOLDER_WRONG(Settings.language.getString("BRANCH_FOLDER_WRONG")),
    FAST_FORWARD_MERGE(Settings.language.getString("FAST_FORWARD_MERGE")),
    MARGE_CANCELED(Settings.language.getString("MARGE_CANCELED")),
    READING_REMOTE_BRANCH_FAILED(Settings.language.getString("READING_REMOTE_BRANCH_FAILED")),
    SELECTED_TO_DELETE_FILE("SELECTED_TO_DELETE_FILE"),
    NOT_SHA_ONE(Settings.language.getString("NOT_SHA_ONE")),
    REPOSITORY_FOLDER_SCAN_FAILED(Settings.language.getString("SCAN_FOLDER_FAILED")),
    CREATE_MAGIT_FOLDER_FAILED(Settings.language.getString("CREATE_MAGIT_FOLDER_FAILED")),
    CREATE_ZIP_FILE_FAILED(Settings.language.getString("CREATE_ZIP_FILE_FAILED")),
    OPEN_FILE_FAILED(Settings.language.getString("OPEN_FILE_FAILED")),
    INSERT_FILE_TO_ZIP_FAILED(Settings.language.getString("INSERT_FILE_TO_ZIP_FAILED")),
    WRITE_TO_FILE_FAILED(Settings.language.getString("WRITE_TO_FILE_FAILED")),
    CREATE_TEMP_FOLDER_FILE_FAILED(Settings.language.getString("CREATE_TEMP_FILE_FAILED")),
    OPEN_BRANCH_FILE_FAILED(Settings.language.getString("OPEN_BRANCH_FILE_FAILED")),
    READ_FROM_FILE_FAILED(Settings.language.getString("READ_FROM_FILE_FAILED")),
    FILE_NOT_EXIST(Settings.language.getString("FILE_NOT_EXIST")),
    WRONG_DATE_FORM(Settings.language.getString("WRONG_DATE_FORM")),
    TARGET_DIR_NOT_EMPTY(Settings.language.getString("TARGET_NEW_REPOSITORY_NOT_EMPTY")),
    ERROR_LOAD_REPOSITORY(Settings.language.getString("ERROR_LOAD_REPOSITORY")),
    UNKNOWN_ERROR(Settings.language.getString("UNKNOWN_ERROR")),
    CLOSE_FILE_FAILED(Settings.language.getString("CLOSE_FILE_FAILED")),
    DO_CHECKOUT(Settings.language.getString("DO_CHECKOUT")),
    NO_COMMIT_WITH_SHA_ONE_EXISTS(Settings.language.getString("NO_COMMIT_WITH_SHA_ONE_EXISTS")),
    MAGIT_FOLDER_CORRUPTED(Settings.language.getString("MAGIT_FOLDER_CORRUPTED")),
    BRANCH_ALREADY_EXIST(Settings.language.getString("BRANCH_ALREADY_EXIST")),
    CANNOT_RECOVER_BRANCH(Settings.language.getString("CANNOT_RECOVER_BRANCH")),
    FORBIDDEN_HEAD_NAME(Settings.language.getString("FORBIDDEN_HEAD_NAME")),
    MAGIT_FOLDER_ALREADY_EXIST(Settings.language.getString("MAGIT_FOLDER_ALREADY_EXIST")),
    BRANCH_NOT_EXIST(Settings.language.getString("BRANCH_NOT_EXIST")),
    NOTHING_TO_SEE(Settings.language.getString("NOTHING_TO_SHOW")),
    THERE_IS_OPENED_ISSUES(Settings.language.getString("THERE_IS_OPENED_ISSUES")),
    DELETE_FILE_FAILED(Settings.language.getString("DELETE_FILE_FAILED")),
    NOTHING_NEW(Settings.language.getString("NOTHING_NEW")),
    CANNOT_DELETE_ACTIVE_BRANCH(Settings.language.getString("CANNOT_DELETE_ACTIVE_BRANCH")),
    XML_PARSE_FAILED(Settings.language.getString("XML_PARSE_FAILED")),
    PARSE_BLOB_TO_FOLDER_FAILED(Settings.language.getString("PARSE_BLOB_TO_FOLDER_FAILED"));

    private String message;

    eErrorCodes(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
}
