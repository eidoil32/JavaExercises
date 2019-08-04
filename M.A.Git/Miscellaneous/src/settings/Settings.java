package settings;

public class Settings {
    public static final int
            MENU_ITEM_EXIT = 13,
    //MENU_ITEM_RESET_BRANCH_TO_COMMIT = 13,
    MENU_ITEM_CREATE_NEW_REPO = 12,
            MENU_ITEM_SHOW_ACTIVE_BRANCH_HISTORY = 11,
            MENU_ITEM_CHECK_OUT = 10,
            MENU_ITEM_DELETE_BRANCH = 9,
            MENU_ITEM_CREATE_NEW_BRANCH = 8,
            MENU_ITEM_SHOW_ALL_BRANCHES = 7,
            MENU_ITEM_COMMIT = 6,
            MENU_ITEM_SHOW_CURRENT_STATUS = 5,
            MENU_ITEM_SHOW_ALL_HISTORY = 4,
            MENU_ITEM_CHANGE_REPO = 3,
            MENU_ITEM_NAME_UPDATE = 2,
            MENU_ITEM_OPEN_RESOURCE = 1;
    public static int MENU_SIZE = MENU_ITEM_EXIT;
    public static final String
            ROOT_SUB_FOLDERS = "ROOT_SUB_FOLDERS",
            ROOT_SUB_FILES = "ROOT_SUB_FILES",
            ALL_FOLDER = "ALL_FOLDER",
            ALL_FILES = "ALL_FILES",
            XML_LOAD_PACKAGE = "xml.basic",
            MAGIT_BRANCH_HEAD = "head",
            MAGIT_FOLDER = ".magit",
            SHOW_STATUS_SEPARATOR = "=================================",
            EMPTY_COMMIT = "null",
            DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS",
            MAGIT_BRANCH_MASTER = "master",
            FILE_ENCODING = "UTF-8",
            FOLDER_FILE_EXTENSION = "magit",
            FOLDER_DELIMITER = ",",
            FILE_TYPE_IN_FOLDER_TABLE = "file",
            FOLDER_TYPE_IN_FOLDER_TABLE = "folder",
            XML_ITEM_FILE_TYPE = "blob",
            XML_ITEM_FOLDER_TYPE = "folder",
            FOLDER_TABLE_DELIMITER = ",",
            REPOSITORY_NAME = "repository.magit",
            OBJECT_FOLDER = "object",
            BRANCHES_FOLDER = "branches",
            TEMP_FOLDER_NAME = "temp.magit",
            TEMP_UNZIP_FOLDER = "unZipTemp";
}