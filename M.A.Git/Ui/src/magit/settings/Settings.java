package magit.settings;

import magit.ui.eMenuItem;

public class Settings {
    public static int MENU_SIZE = 13;
    public static final String
            MAGIT_BRANCH_HEAD = "head",
            MAGIT_FOLDER = ".magit",
            SHOW_STATUS_SEPARATOR = "=================================",
            EMPTY_COMMIT = "null";
    public static final int
            MENU_ITEM_EXIT = 13,
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
    public static final StringBuilder MAIN_MENU = new StringBuilder();

    public Settings() {
        for (int i = 1; i <= MENU_SIZE; i++) {
            String itemName = eMenuItem.getItem(i).get().getName();
            MAIN_MENU.append(String.format("%d - %s", i, itemName)).append(System.lineSeparator());
        }
    }
}