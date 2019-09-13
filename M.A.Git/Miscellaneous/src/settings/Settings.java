package settings;

import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
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
            MAGIT_UI_SETTINGS_MIN_HEIGHT = 250,
            MAGIT_UI_SETTINGS_MIN_WIDTH = 600,
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
            RESOURCE_ROOT_FOLDER = RESOURCE_SEPARATOR + RESOURCE_MAGIT_PACKAGE + RESOURCE_SEPARATOR + RESOURCE_RESOURCES_PACKAGE + RESOURCE_SEPARATOR,
            RESOURCE_IMAGE_PACKAGE = RESOURCE_ROOT_FOLDER + RESOURCE_SEPARATOR + IMAGE_PACKAGE + RESOURCE_SEPARATOR,
            THEME_ROOT_FOLDER = RESOURCE_ROOT_FOLDER + RESOURCE_THEME_PACKAGE + RESOURCE_SEPARATOR,
            FXML_SELECT_POPUP = RESOURCE_ROOT_FOLDER + "select_popup.fxml",
            FXML_SETTINGS_WINDOW = RESOURCE_ROOT_FOLDER + "settings.fxml",
            FXML_APPLICATION = RESOURCE_ROOT_FOLDER + "magit.fxml",
            FXML_SMART_POPUP_BOX = RESOURCE_ROOT_FOLDER + "smart_popup.fxml",
            FXML_DIALOG_BOX = RESOURCE_ROOT_FOLDER + "dialogBox.fxml",
            FXML_INTRO_WINDOW = RESOURCE_ROOT_FOLDER + "magit_intro.fxml",
            FXML_BRANCH_MANAGER = RESOURCE_ROOT_FOLDER + "branchManager.fxml",
            FXML_MERGE_WINDOW = RESOURCE_ROOT_FOLDER + "mergeWindow.fxml",
            FXML_FILE_VIEWER = RESOURCE_ROOT_FOLDER + "fileView.fxml",
            FXML_TREE_VIEW_FILE = RESOURCE_IMAGE_PACKAGE + "file.png",
            FXML_TREE_WINDOW = RESOURCE_ROOT_FOLDER + "treeWindow.fxml",
            FXML_TREE_VIEW_FOLDER = RESOURCE_IMAGE_PACKAGE + "folder.png",
            FXML_CLOSE_BUTTON_HOVER_IMG = RESOURCE_IMAGE_PACKAGE + "close_pressed.png",
            FXML_CLOSE_BUTTON_IMG = RESOURCE_IMAGE_PACKAGE + "close.png",
            FXML_EXPAND_BUTTON_IMG = RESOURCE_IMAGE_PACKAGE + "expand.png",
            FXML_EXPAND_BUTTON_HOVER_IMG = RESOURCE_IMAGE_PACKAGE + "expand_pressed.png",
            FXML_THEME_WHITE_CSS_FILE = THEME_ROOT_FOLDER + "white.css",
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

    // simple function to get brighter color of input color
    public static Color getBrighter(Color current) {
        return current.brighter();
    }

    public static void setup() {
        themeManager.put(THEME_WHITE, FXML_THEME_WHITE_CSS_FILE);
        themeManager.put(THEME_CUSTOM, FXML_THEME_CUSTOM_CSS_FILE);
    }
}