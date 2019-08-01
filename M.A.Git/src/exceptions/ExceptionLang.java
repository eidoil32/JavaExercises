package exceptions;

public class ExceptionLang {
    public static final String
            FOLDER_PERMISSIONS = "please check folder permissions and try again.",
            FILE_EXIST_OR_PERMISSIONS = "please check if file exist or have the right permissions.",
            FILE_NOT_EXIST = "Sorry, but file is not exist",
            CANNOT_RECOVER_BRANCH = "Cannot recover active branch from HEAD, magit folder is corrupted!",
            BRANCH_ALREADY_EXIST = "This branch already exist!",
            MAGIT_FOLDER_ALREADY_EXIST = "It's seems that the path you give us already an magit repository, we cannot create new repository there!",
            READ_FROM_FILE_FAILED = String.format("Reading from file failed, %s", FILE_EXIST_OR_PERMISSIONS),
            OPEN_BRANCH_FILE_FAILED = "Sorry but we can't open current branch file, please reload magit.",
            CREATE_TEMP_FILE_FAILED = String.format("Creating temp file for folder failed %s", FOLDER_PERMISSIONS),
            ERROR_LOAD_REPOSITORY = "Error while loading repository, maybe one of magit file was corrupted, we can't do nothing!",
            CREATE_MAGIT_FOLDER_FAILED = String.format("Creating magit folder failed! %s.", FOLDER_PERMISSIONS),
            CREATE_ZIP_FILE_FAILED = String.format("Creating zip file failed! %s.", FILE_EXIST_OR_PERMISSIONS),
            OPEN_FILE_FAILED = String.format("Opening file failed! %s.", FILE_EXIST_OR_PERMISSIONS),
            INSERT_FILE_TO_ZIP_FAILED = "Insert data to zip file failed! please try again.",
            WRITE_TO_FILE_FAILED = String.format("Writing to file failed! %s.", FILE_EXIST_OR_PERMISSIONS),
            NOTHING_NEW = "Nothing new in the working copy, no need for commit.",
            CLOSE_FILE_FAILED = "Closing file failed, I really don't know what happens, please try command again.",
            DELETE_FILE_FAILED = "Deleting '%s' file failed! magit will not work properly!" + System.lineSeparator() +
                    "Please check delete this file and try again.",
            BRANCH_NOT_EXIST = "The branch %s is not exist!",
            THERE_IS_OPENED_ISSUES = "Cannot checkout because there files that not saved (do commit first and try again)",
            PARSE_BLOB_TO_FOLDER_FAILED = "FATAL ERROR: while parsing blob to folder failed (maybe corrupted file)",
            SCAN_FOLDER_FAILED = "We sorry, but we can't scan the folder you give us.\nPlease try again.";
}
