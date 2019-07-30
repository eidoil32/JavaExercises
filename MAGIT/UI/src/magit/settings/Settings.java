package com.magit.settings;

import com.magit.ui.eMenuItem;

import java.util.HashMap;
import java.util.Map;

public class Settings {
    public static int MENU_SIZE = 13, EXIT_CHOICE = MENU_SIZE;
    public static final String
            FILE_ENCODING = "UTF-8",
            FOLDER_FILE_EXTENSION = "magit",
            MAGIT_BRANCH_MASTER = "master",
            FOLDER_DELIMITER = ",",
            FILE_TYPE_IN_FOLDER_TABLE = "file",
            FOLDER_TYPE_IN_FOLDER_TABLE = "folder",
            FOLDER_TABLE_DELIMITER = ",",
            MAGIT_FOLDER = ".magit",
            EMPTY_COMMIT = "null",
            OBJECT_FOLDER = "object",
            BRANCHES_FOLDER  = "branches",
            TEMP_UNZIP_FOLDER = "unZipTemp",
            MAGIT_BRANCH_HEAD = "HEAD";
    public static final Map<Integer,String> menu_options = new HashMap<>();
    public static final Map<Integer,eMenuItem> e_menu_options = new HashMap<>();
    public static final Map<eMenuItem,Integer> get_number_of_menu = new HashMap<>();
    public static final StringBuilder MAIN_MENU = new StringBuilder();

    public Settings() {
        CreateMapOfeMenuItems();
        menu_options.put(eMenuItem.NAME_UPDATE.getPlace(),LangEN.MENU_OPTION_NAME_UPDATE);
        menu_options.put(eMenuItem.OPEN_RESOURCE.getPlace(),LangEN.MENU_OPTION_OPEN_RESOURCE);
        menu_options.put(eMenuItem.CHANGE_REPO.getPlace(),LangEN.MENU_OPTION_CHANGE_REPO);
        menu_options.put(eMenuItem.CREATE_NEW_REPO.getPlace(),LangEN.MENU_OPTION_CREATE_NEW_REPO);
        menu_options.put(eMenuItem.SHOW_ALL_HISTORY.getPlace(),LangEN.MENU_OPTION_SHOW_ALL_HISTORY);
        menu_options.put(eMenuItem.SHOW_CURRENT_STATUS.getPlace(),LangEN.MENU_OPTION_SHOW_CURRENT_STATUS);
        menu_options.put(eMenuItem.COMMIT.getPlace(),LangEN.MENU_OPTION_COMMIT);
        menu_options.put(eMenuItem.SHOW_ALL_BRANCHES.getPlace(),LangEN.MENU_OPTION_SHOW_ALL_BRANCHES);
        menu_options.put(eMenuItem.CREATE_NEW_BRANCH.getPlace(),LangEN.MENU_OPTION_CREATE_NEW_BRANCH);
        menu_options.put(eMenuItem.DELETE_BRANCH.getPlace(),LangEN.MENU_OPTION_DELETE_BRANCH);
        menu_options.put(eMenuItem.CHECK_OUT.getPlace(),LangEN.MENU_OPTION_CHECK_OUT);
        menu_options.put(eMenuItem.SHOW_ACTIVE_BRANCH_HISTORY.getPlace(),LangEN.MENU_OPTION_SHOW_ACTIVE_BRANCH_HISTORY);
        menu_options.put(eMenuItem.EXIT.getPlace(),LangEN.MENU_OPTION_EXIT);
        createMenuOneTime();
    }

    public void createMenuOneTime() {
        for (int i = 0; i < MENU_SIZE; i++) {
            MAIN_MENU.append(String.format("%d - %s", i + 1, Settings.menu_options.get(i))).append(System.lineSeparator());
        }
    }

    private void CreateMapOfeMenuItems()
    {
        eMenuItem[] enumArray = eMenuItem.values();
        for (int i = 0; i < MENU_SIZE; i++) {
            get_number_of_menu.put(enumArray[i],i);
            e_menu_options.put(i,enumArray[i]);
        }
    }
}