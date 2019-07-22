package com.magit.ui;

import com.magit.engine.Magit;
import com.magit.engine.Repository;
import com.magit.settings.LangEN;
import com.magit.settings.Settings;

import java.nio.file.Paths;
import java.util.Scanner;

public enum eMenuItem {
    OPEN_RESOURCE{
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    NAME_UPDATE {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    CHANGE_REPO {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    SHOW_ALL_HISTORY {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    SHOW_CURRENT_STATUS {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    COMMIT {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    SHOW_ALL_BRANCHES {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    CREATE_NEW_BRANCH {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    DELETE_BRANCH {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    CHECK_OUT {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    SHOW_ACTIVE_BRANCH_HISTORY {
        @Override
        public boolean executeCommand() {
            return false;
        }
    },
    CREATE_NEW_REPO {
        @Override
        public boolean executeCommand() {
            Magit magit = Main.engine.getSystem();
            System.out.println(LangEN.ASK_FOR_PATH_FOR_REPO);
            String path = new Scanner(System.in).nextLine();
            magit.setCurrentRepository(new Repository(Paths.get(path)));
            return true;
        }
    },
    EXIT {
        @Override
        public boolean executeCommand() {
            return false;
        }
    };

    public abstract boolean executeCommand();

    public int getPlace() {
        return Settings.get_number_of_menu.get(this);
    }
}
