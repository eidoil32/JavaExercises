package exceptions;

import settings.Settings;

public enum eErrorCodesXML {
    DUPLICATE_ROOT_FOLDER(Settings.language.getString("DUPLICATE_ROOT_FOLDER")),
    TOO_MANY_PREV_COMMITS(Settings.language.getString("TOO_MANY_PREV_COMMITS")),
    COMMIT_POINT_NONE_EXIST(Settings.language.getString("COMMIT_POINT_NONE_EXIST")),
    DUPLICATE_ID_NUMBER(Settings.language.getString("DUPLICATE_ID_NUMBER")),
    FOLDER_POINT_TO_NONSEXIST_BLOB(Settings.language.getString("FOLDER_POINT_TO_NONSEXIST_BLOB")),
    FOLDER_POINT_TO_SELF(Settings.language.getString("FOLDER_POINT_TO_SELF")),
    TARGET_FOLDER_NOT_EMPTY(Settings.language.getString("TARGET_FOLDER_NOT_EMPTY")),
    WRONG_DATE_FORMAT(Settings.language.getString("WRONG_DATE_FORMAT")),
    ALREADY_EXIST_FOLDER(Settings.language.getString("ALREADY_EXIST_FOLDER")),
    ITEM_WITH_UNKNOWN_TYPE(Settings.language.getString("ITEM_WITH_UNKNOWN_TYPE")),
    FOLDER_POINT_TO_NONSEXIST_FOLDER(Settings.language.getString("FOLDER_POINT_TO_NONSEXIST_FOLDER")),
    COMMIT_POINT_TO_BLOB(Settings.language.getString("COMMIT_POINT_TO_BLOB")),
    COMMIT_POINT_TO_NONE_ROOT_FOLDER(Settings.language.getString("COMMIT_POINT_TO_NONE_ROOT_FOLDER")),
    BRANCH_POINT_TO_NONSEXIST_COMMIT(Settings.language.getString("BRANCH_POINT_TO_NONSEXIST_COMMIT")),
    HEAD_POINT_TO_NONSEXIST_BRANCH(Settings.language.getString("HEAD_POINT_TO_NONSEXIST_BRANCH"));

    private String errorMessage;

    eErrorCodesXML(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}
