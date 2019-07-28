package com.magit.ui;

import com.magit.engine.BasicFile;
import com.magit.engine.Magit;
import com.magit.engine.Repository;
import com.magit.exceptions.RepositoryException;
import com.magit.settings.LangEN;
import com.magit.settings.MapKeys;
import com.magit.settings.Settings;

import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public enum eMenuItem {
    OPEN_RESOURCE {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    NAME_UPDATE {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    CHANGE_REPO {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    SHOW_ALL_HISTORY {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    SHOW_CURRENT_STATUS {
        @Override
        public String executeCommand() throws IOException {
            Magit magit = Main.engine.getSystem();
            Repository repository = magit.getCurrentRepository();
            Map<String, List<BasicFile>> fileLists = repository.scanRepository();
            String temp;
            StringBuilder stringBuilder = new StringBuilder();

            temp = listOfFiles(fileLists.get(MapKeys.LIST_CHANGED), LangEN.SHOW_STATUS_EDITED);
            if(temp != null)
                stringBuilder.append(temp);
            temp = listOfFiles(fileLists.get(MapKeys.LIST_DELETED), LangEN.SHOW_STATUS_DELETED);
            if(temp != null)
                stringBuilder.append(temp);
            temp = listOfFiles(fileLists.get(MapKeys.LIST_NEW), LangEN.SHOW_STATUS_NEW);
            if(temp != null)
                stringBuilder.append(temp);

            return stringBuilder.toString();
        }
    },
    COMMIT {
        @Override
        public String executeCommand() {
            Magit magit = Main.engine.getSystem();
            try {
                magit.commitMagit();
            } catch (IOException e) {
                return LangEN.ERROR_CREATE_COMMIT_FILE;
            } catch (ParseException e) {
                return LangEN.ERROR_PARSE_CURRENT_DATE;
            } catch (RepositoryException e) {
                return LangEN.ERROR_COMMIT_FAILED;
            }
            return LangEN.COMMIT_CREATED_SUCCESSFULLY;
        }
    },
    SHOW_ALL_BRANCHES {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    CREATE_NEW_BRANCH {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    DELETE_BRANCH {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    CHECK_OUT {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    SHOW_ACTIVE_BRANCH_HISTORY {
        @Override
        public String executeCommand() {
            return null;
        }
    },
    CREATE_NEW_REPO {
        @Override
        public String executeCommand() throws RepositoryException, IOException {
            Magit magit = Main.engine.getSystem();
            System.out.println(LangEN.ASK_FOR_PATH_FOR_REPO);
            String path = new Scanner(System.in).nextLine();
            magit.setCurrentRepository(new Repository(Paths.get(path)));
            magit.setRootFolder(Paths.get(path));
            return String.format(LangEN.NEW_REPOSITORY_CREATED_SUCCESSFULLY, magit.getCurrentRepository().getName());
        }
    },
    EXIT {
        @Override
        public String executeCommand() {
            return null;
        }
    };

    public abstract String executeCommand() throws RepositoryException, IOException;

    public int getPlace() {
        return Settings.get_number_of_menu.get(this);
    }

    private static String listOfFiles(List<BasicFile> fileList, String title) {
        if (fileList.size() == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(title).append(System.lineSeparator());
        for (BasicFile file : fileList) {
            stringBuilder.append(file.getName()).append(System.lineSeparator());
        }
        stringBuilder.append(LangEN.SHOW_STATUS_SAPERATOR).append(System.lineSeparator());
        return stringBuilder.toString();
    }
}
