package settings;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class Settings {
    // windows sizes for JavaFX UI application
    public static final int
            MAGIT_UI_DIALOG_BOX_HEIGHT = 175,
            MAGIT_UI_DIALOG_BOX_WIDTH = 250,
            MAGIT_UI_FILE_VIEWER_HEIGHT = 600,
            MAGIT_UI_FILE_VIEWER_WIDTH = 400,
            MAGIT_UI_INTRO_MIN_HEIGHT = 400,
            MAGIT_UI_INTRO_MIN_WIDTH = 600,
            MAGIT_UI_MERGE_WINDOW_HEIGHT = 700,
            MAGIT_UI_MERGE_WINDOW_WIDTH = 800,
            MAGIT_UI_MIN_HEIGHT = 600,
            MAGIT_UI_MIN_WIDTH = 900,
            MAGIT_UI_SELECT_POPUP_HEIGHT = 150,
            MAGIT_UI_SELECT_POPUP_WIDTH = 350,
            MAGIT_UI_SETTINGS_MIN_HEIGHT = 300,
            MAGIT_UI_SETTINGS_MIN_WIDTH = 650,
            MAGIT_UI_SMART_POPUP_MAX_HEIGHT = 250,
            MAGIT_UI_SMART_POPUP_MAX_WIDTH = 600,
            MAGIT_UI_TREE_WINDOW_HEIGHT = 700,
            MAGIT_UI_TREE_WINDOW_WIDTH = 500;

    public static final int
            ANIMATION_DURATION_TWO_SECONDS = 2000,
            COMMIT_TREE_START_X = 10,
            COMMIT_TREE_START_Y = 50,
            COMMIT_TREE_ADD_TO_Y = 40,
            SHA_ONE_CORRECT_LENGTH = 40,
            COMMIT_TREE_RECTANGLE_WIDTH_DETAILS = 300,
            COMMIT_TREE_RECTANGLE_HEIGHT_DETAILS = 40,
            COMMIT_TREE_STROKE_WIDTH = 6,
            MIN_COMMENT_SUBSTRING = 0,
            MAX_COMMENT_SUBSTRING = 30,
            MINIMUM_DAY_TO_SHOW = 10,
            FX_NEW_TAB_KEY = 0,
            FX_EDIT_TAB_KEY = 1,
            FX_DELETED_TAB_KEY = 2,
            FX_MAX_NAME_OF_REMOTE_REPOSITORY = 15,
            MAX_SHA_ONE_TABLE_LENGTH = 7;
    public static final String
            MAGIT_LINE_SEPARATOR = "\n",
            TEMP_BRANCH_FOR_EXPANDED_TREE = "not pointed commit",
            THEME_CUSTOM = "black",
            THEME_WHITE = "white",
            LANG_ENG = "english",
            LANG_HEB = "hebrew",
            HEBREW_CODE = "he_IL",
            ENGLISH_CODE = "",
            RESOURCE_FILE = "languages.lang",
            CANCEL_BTN_CLICKED_STRING = "cancel",
            JAVAFX_NEWLINE_TEXTAREA = "\n",
            REMOTE_REPOSITORY_FILE_DATA = "remote-repository",
            IS_TRACKING_REMOTE_BRANCH = "true",
            FX_DATE_FORMAT = "dd/MM/yyyy",
            XML_EXTENSION = "xml",
            XML_FILE_REQUEST_TYPE = "*." + XML_EXTENSION,
            DOT_XML = "." + XML_EXTENSION,
            KEY_ALL_FOLDERS = "ALL_FOLDER",
            KEY_ALL_FILES = "ALL_FILES",
            KEY_COMMIT_BRANCH_LIST = "KEY_COMMIT_BRANCH_LIST",
            KEY_COMMIT_LIST = "KEY_COMMIT_LIST",
            KEY_COUNTER_COMMIT = "KEY_COUNTER_COMMIT",
            KEY_ALL_COMMITS = "KEY_ALL_COMMITS",
            KEY_COUNTER_FILES = "KEY_COUNTER_FILES",
            KEY_COUNTER_FOLDERS = "KEY_COUNTER_FOLDERS",
            KEY_FOLDER_ONLY_MAP = "KEY_FOLDER_ONLY_MAP",
            KEY_ROOT_FOLDER_PATH = "KEY_ROOT_FOLDER_PATH",
            KEY_CHANGE_MAP = "KEY_CHANGE_MAP",
            KEY_ANCESTOR_MAP = "KEY_ANCESTOR_MAP",
            KEY_ACTIVE_MAP = "KEY_ACTIVE_MAP",
            KEY_TARGET_MAP = "KEY_TARGET_MAP",
            KEY_FINAL_MAP = "KEY_FINAL_MAP",
            KEY_EASY_TAKE_MAP = "KEY_EASY_TAKE_MAP",
            XML_LOAD_PACKAGE = "xml.basic",
            MAGIT_BRANCH_HEAD = "head",
            MAGIT_FOLDER = ".magit",
            TEMP_FILE = "temp.magit",
            EMPTY_STRING = "",
            BASIC_SLASH = "/",
            TYPE_SPLITTER = "|",
            SEPARATOR_PATTERN = Pattern.quote(File.separator),
            EMPTY_COMMIT = "null",
            DATE_FORMAT = "dd.MM.yyyy-HH:mm:ss:SSS",
            WEB_DATE_FORMAT = "dd.MM.yyyy",
            MAGIT_BRANCH_MASTER = "master",
            FOLDER_FILE_EXTENSION = "magit",
            FOLDER_DELIMITER = ",",
            FILE_TYPE_IN_FOLDER_TABLE = "file",
            FOLDER_TYPE_IN_FOLDER_TABLE = "folder",
            XML_ITEM_FILE_TYPE = "blob",
            XML_ITEM_FOLDER_TYPE = "folder",
            FOLDER_TABLE_DELIMITER = ",",
            REPOSITORY_NAME = "repository.magit",
            OBJECT_FOLDER = "object",
            FOLDER_IMAGE_KEY = "folder",
            FILE_IMAGE_KEY = "file",
            BRANCHES_FOLDER = "branches",
            TEMP_FOLDER_NAME = "temp.magit",
            FILE_FOLDER_DELETE = "delete",
            FILE_FOLDER_EDIT = "edit",
            FILE_FOLDER_NEW = "new",
            FAST_FORWARD_MERGE = "FAST_FORWARD_MERGE",
            TEMP_UNZIP_FOLDER = "unZipTemp";
    public static String currentLanguage = ENGLISH_CODE, currentTheme = THEME_WHITE;
    public static ResourceBundle language = ResourceBundle.getBundle(RESOURCE_FILE, new UTF8Control(new Locale(currentLanguage)));
    public static Map<String, String> themeManager = new HashMap<>();
    public static float DATE_CALCULATE_MILLISECONDS_TO_DAY = 1000 * 60 * 60 * 24;

    // resource files macros
    public static String
            RESOURCE_SEPARATOR = "/",                   // instead File.separator, not working in resource path.
            RESOURCE_MAGIT_PACKAGE = "magit",
            RESOURCE_RESOURCES_PACKAGE = "resources",
            RESOURCE_THEME_PACKAGE = "theme",
            IMAGE_PACKAGE = "img",
            IMAGE_PNG_TYPE = ".png",
            RESOURCE_ROOT_FOLDER = RESOURCE_MAGIT_PACKAGE + RESOURCE_SEPARATOR + RESOURCE_RESOURCES_PACKAGE + RESOURCE_SEPARATOR,
            RESOURCE_IMAGE_PACKAGE = RESOURCE_ROOT_FOLDER + IMAGE_PACKAGE + RESOURCE_SEPARATOR,
            THEME_ROOT_FOLDER = RESOURCE_ROOT_FOLDER + RESOURCE_THEME_PACKAGE + RESOURCE_SEPARATOR,
            FXML_SELECT_POPUP = RESOURCE_ROOT_FOLDER + "select_popup.fxml",
            FXML_SETTINGS_WINDOW = RESOURCE_ROOT_FOLDER + "settings.fxml",
            FXML_APPLICATION = RESOURCE_ROOT_FOLDER + "magit.fxml",
            FXML_SMART_POPUP_BOX = RESOURCE_ROOT_FOLDER + "smart_popup.fxml",
            FXML_DIALOG_BOX = RESOURCE_ROOT_FOLDER + "dialog_box.fxml",
            FXML_INTRO_WINDOW = RESOURCE_ROOT_FOLDER + "magit_intro.fxml",
            FXML_BRANCH_MANAGER = RESOURCE_ROOT_FOLDER + "branch_manager.fxml",
            FXML_MERGE_WINDOW = RESOURCE_ROOT_FOLDER + "merge_window.fxml",
            FXML_FILE_VIEWER = RESOURCE_ROOT_FOLDER + "file_view.fxml",
            FXML_TREE_VIEW_FILE = RESOURCE_IMAGE_PACKAGE + "file.png",
            FXML_TREE_WINDOW = RESOURCE_ROOT_FOLDER + "tree_window.fxml",
            FXML_TREE_VIEW_FOLDER = RESOURCE_IMAGE_PACKAGE + "folder.png",
            FXML_CLOSE_BUTTON_HOVER_IMG = RESOURCE_IMAGE_PACKAGE + "close_pressed.png",
            FXML_CLOSE_BUTTON_IMG = RESOURCE_IMAGE_PACKAGE + "close.png",
            FXML_EXPAND_BUTTON_IMG = RESOURCE_IMAGE_PACKAGE + "expand.png",
            FXML_EXPAND_BUTTON_HOVER_IMG = RESOURCE_IMAGE_PACKAGE + "expand_pressed.png",
            FXML_THEME_WHITE_CSS_FILE = THEME_ROOT_FOLDER + "white.css",
            FXML_THEME_CUSTOM_EXTERNAL_CSS_FILE =
                    new File(new File("").getAbsolutePath() + RESOURCE_SEPARATOR + RESOURCE_THEME_PACKAGE
                            + RESOURCE_SEPARATOR + "custom.css").toString(),
            FXML_THEME_CUSTOM_CSS_FILE = THEME_ROOT_FOLDER + "custom.css";

    // css macros
    public static String
            HEAD_BRANCH_CSS_CLASS = "head-branch",
            MOUSE_HAND_ON_HOVER = "hands-on",
            CSS_HEAD_BRANCH_ID = "commit-table-branch-name-column",
            CSS_COMMIT_TREE_LABEL = "commit-details-label",
            CSS_RED_BORDER_ERROR = "error-border",
            CSS_TEXT_AREA_BASIC = "text-area",
            CSS_TEXT_INPUT_BASIC = "text-input",
            LINE_BRIGHTER_COLOR = "#dedede";

    // commit tree circle settings
    public static final double
            COMMIT_CIRCLE_STROKE_WIDTH = 3,
            COMMIT_CIRCLE_RADIUS = 8,
            COMMIT_SPACE_BETWEEN_CIRCLES = 20;

    // animation macro
    public static final float SCALE_NODE = 1.2f;
    public static final int CYCLE_COUNT = 4;
    public static final boolean ALLOW_REVERSE = true;
    public static Duration ANIMATION_DURATION = Duration.millis(0);
    public static Color CURRENT_THEME_COLOR = Color.WHITE;
    private static List<String> file_locations = new LinkedList<>();

    // simple function to get brighter color of input color
    public static Color getBrighter(Color current) {
        return current.brighter();
    }

    public static final char[] special_chars = new char[]{'<', '>', ':', '\"', '|', '?', '*'};

    // WSA == Web Session Attributes - used for keys for session map
    public static final String
            COOKIE_USER_LOGGED_IN = "USER_ID",
            INVALID_XML_FILE = "invalid_xml_file",
            WSA_JSON_ACTIVE_BRANCH = "WSA_JSON_ACTIVE_BRANCH",
            WSA_REPOSITORY_NAME = "WSA_REPOSITORY_NAME",
            WSA_SINGLE_REPOSITORY_IS_RT = "WSA_SINGLE_REPOSITORY_IS_RT",
            WSA_SINGLE_REPOSITORY_OPENED_CHANGES = "WSA_SINGLE_REPOSITORY_OPENED_CHANGES",
            WSA_SINGLE_REPOSITORY_PR = "WSA_SINGLE_REPOSITORY_PR",
            WSA_SINGLE_REPOSITORY_FILE_TREE = "WSA_SINGLE_REPOSITORY_FILE_TREE",
            WSA_SINGLE_REPOSITORY_ALL_COMMITS = "WSA_SINGLE_REPOSITORY_ALL_COMMITS",
            WSA_SINGLE_REPOSITORY_BRANCHES = "WSA_SINGLE_REPOSITORY_BRANCHES",
            WSA_SINGLE_REPOSITORY_HEAD_BRANCH = "WSA_SINGLE_REPOSITORY_HEAD_BRANCH",
            WSA_JSON_CURRENT_PATH = "WSA_JSON_CURRENT_PATH",
            WSA_REMOTE_REPOSITORY_NAME = "WSA_REMOTE_REPOSITORY_NAME",
            WSA_REPOSITORIES = "WSA_REPOSITORIES",
            WSA_USER_NAME = "WSA_USER_NAME",
            WSA_SINGLE_REPOSITORY_OWNER_NAME = "WSA_SINGLE_REPOSITORY_OWNER_NAME",
            WSA_JSON_LAST_COMMIT_COMMENT = "WSA_JSON_LAST_COMMIT_COMMANT",
            WSA_JSON_LAST_COMMIT_DATA = "WSA_JSON_LAST_COMMIT_DATA",
            WSA_JSON_NUM_OF_BRANCHES = "WSA_JSON_NUM_OF_BRANCHES",
            WSA_REPOSITORIES_NUMBER = "WSA_REPOSITORIES_NUMBER",
            WSA_USERNAME_KEY = "username",
            WSA_REPOSITORY_LOCATION = "WSA_REPOSITORY_LOCATION",
            WSA_REPOSITORY_ID = "repo_id",
            NULL_STRING = "null",
            WSA_SINGLE_COMMIT_SHA1_KEY = "WSA_SINGLE_COMMIT_SHA1_KEY",
            WSA_SINGLE_COMMIT_COMMENT_KEY = "WSA_SINGLE_COMMIT_COMMENT_KEY",
            WSA_SINGLE_COMMIT_DATE_KEY = "WSA_SINGLE_COMMIT_DATE_KEY",
            WSA_SINGLE_COMMIT_CREATOR_KEY = "WSA_SINGLE_COMMIT_CREATOR_KEY",
            WSA_SINGLE_COMMIT_POINTED_BRANCHES = "WSA_SINGLE_COMMIT_POINTED_BRANCHES",
            WSA_USER = "WSA_USER";

    public static final String
            PR_FILE_CONTENT = "CONTENT",
            PR_ALL_COMMITS = "PR_ALL_COMMITS",
            PR_ID = "PR_ID",
            PR_LOCAL_BRANCH_NAME = "PR_LOCAL_BRANCH_NAME",
            PR_REMOTE_BRANCH_NAME = "PR_REMOTE_BRANCH_NAME",
            PR_COMMENT = "PR_COMMENT",
            PR_DATE_CREATION = "PR_DATE_CREATION",
            PR_REMOTE_REPOSITORY_ID = "PR_REMOTE_REPOSITORY_ID",
            PR_LOCAL_REPOSITORY_ID = "PR_LOCAL_REPOSITORY_ID",
            PR_REQUEST_CREATOR = "PR_REQUEST_CREATOR";

    public static final String[]
            FORK_REPOSITORY = {Settings.language.getString("USER_FORK_REPOSITORY_KEY"),
                    Settings.language.getString("USER_MESSAGE_FORK")},
            PUSH_BRANCH = {Settings.language.getString("BRANCH_PUSHED_TO_REPOSITORY_KEY"),
                    Settings.language.getString("BRANCH_PUSHED_TO_REPOSITORY")};

    public static final String
            SINGLE_REPOSITORY_PREFIX = "repository_",
            USER_MESSAGE_SEPARATOR = " | ",
            GET_URL_PARAMETERS_ADDON = "?",
            GET_URL_PARAMETERS_ADDON_PLUS = "&",
            URL_REFERER = "referer",
            APPLICATION_RESPONSE_TYPE = "application/json",
            SERVER_DATABASE = "c:" + File.separator + "magit-ex3",
            USERS_FOLDER = SERVER_DATABASE + File.separator + "users",
            USER_BASE_FOLDER = USERS_FOLDER + File.separator + "%s",
            USER_PULL_REQUEST_CENTER = USER_BASE_FOLDER + File.separator + "pull_requests_center",
            USER_MESSAGES_CENTER = USER_BASE_FOLDER + File.separator + "messages_center",
            USERS_REPOSITORY_ROOT_FOLDER = USERS_FOLDER + File.separator + "%s" + File.separator + "repositories", // argument user name
            USERS_REPOSITORY_FOLDER = USERS_REPOSITORY_ROOT_FOLDER + File.separator + "repository_%s"; // argument repository number

    public static final String USER_MESSAGE_PARTS[] = new String[]{
            "MESSAGE_KEY_TYPE", "MESSAGE_KEY_REPOSITORY", "MESSAGE_KEY_CONTENT", "MESSAGE_KEY_TIME", "MESSAGE_KEY_CREATOR"};

    public static final String
            PAGE_LOGIN = "/login.html",
            PAGE_SIGNUP = "/signup.html",
            PAGE_INDEX = "/index.html";


    public static boolean SERVER_STATUS;

    public static void setup() {
        themeManager.put(THEME_WHITE, FXML_THEME_WHITE_CSS_FILE);
        themeManager.put(THEME_CUSTOM, new File(FXML_THEME_CUSTOM_EXTERNAL_CSS_FILE).toURI().toString());

        addToList(FXML_SELECT_POPUP,
                FXML_SETTINGS_WINDOW,
                FXML_APPLICATION,
                FXML_SMART_POPUP_BOX,
                FXML_DIALOG_BOX,
                FXML_INTRO_WINDOW,
                FXML_BRANCH_MANAGER,
                FXML_MERGE_WINDOW,
                FXML_FILE_VIEWER,
                FXML_TREE_VIEW_FILE,
                FXML_TREE_WINDOW,
                FXML_TREE_VIEW_FOLDER,
                FXML_CLOSE_BUTTON_HOVER_IMG,
                FXML_CLOSE_BUTTON_IMG,
                FXML_EXPAND_BUTTON_IMG,
                FXML_EXPAND_BUTTON_HOVER_IMG,
                FXML_THEME_WHITE_CSS_FILE,
                FXML_THEME_CUSTOM_CSS_FILE);
    }

    private static void addToList(String... values) {
        file_locations.addAll(Arrays.asList(values));
    }

    public static boolean testPaths() {
        boolean check = true;
        Date now = new Date();

        System.out.println("Welcome to M.A.Git - JavaFX application");
        System.out.println("System check: Checking resource files...");

        for (String file_location : file_locations) {
            URL url = Settings.class.getResource(file_location);
            if (url != null) {
                System.out.println("Error, the resource file '" + url + "' not found!");
                check = false;
            }
        }


        System.out.println("System check: Checking external custom css file..");
        if (!new File(FXML_THEME_CUSTOM_EXTERNAL_CSS_FILE).exists()) {
            System.out.println("Error, external css file not found!");
            check = false;
        }

        double seconds = (double) (new Date().getTime() - now.getTime()) / 1000;
        System.out.println(String.format("System check: System scanning finish in %.3f seconds..", seconds));

        if (!check) {
            System.out.println("System check: Scanning system files finish unsuccessfully.");
            System.out.println("Open M.A.Git canceled.");
        } else {
            System.out.println("System check: Scanning system files finish successfully,  enjoy your M.A.Git!");
            System.out.println("Starting M.A.Git...");
        }

        return check;
    }
}