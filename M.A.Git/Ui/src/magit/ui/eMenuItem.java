package magit.ui;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.*;
import magit.settings.LangEN;
import magit.settings.Settings;
import utils.MapKeys;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public enum eMenuItem {
    OPEN_RESOURCE {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    NAME_UPDATE {
        @Override
        public String executeCommand(String currentUser) {
            System.out.print(LangEN.PLEASE_ENTER_YOUR_NAME);
            String newName = new Scanner(System.in).nextLine();
            Main.engine.getSystem().setCurrentUser(newName);
            return String.format(LangEN.CHANGE_NAME_SUCCESSFULLY_TO,newName);
        }
    },
    CHANGE_REPO {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    SHOW_ALL_HISTORY {
        @Override
        public String executeCommand(String currentUser) {
            Commit lastCommit = Main.engine.getSystem().getCurrentRepository().getLastCommit();
            if(lastCommit == null)
                return "";
            else
                return lastCommit.showAllHistory();
        }
    },
    SHOW_CURRENT_STATUS {
        @Override
        public String executeCommand(String currentUser) throws IOException {
            Magit magit = Main.engine.getSystem();
            Repository repository = magit.getCurrentRepository();
            Map<String, List<BasicFile>> fileLists = null;
            try {
                fileLists = repository.scanRepository();
            } catch (MyFileException e) {
                System.out.println(e.getCode().getPersonalMessage());
            } catch (RepositoryException e) {
                System.out.println(e.getCode().getPersonalMessage());
            }
            String temp;
            StringBuilder stringBuilder = new StringBuilder();

            temp = listOfFiles(fileLists.get(MapKeys.LIST_CHANGED), LangEN.SHOW_STATUS_EDITED);
            if (temp != null)
                stringBuilder.append(temp);
            temp = listOfFiles(fileLists.get(MapKeys.LIST_DELETED), LangEN.SHOW_STATUS_DELETED);
            if (temp != null)
                stringBuilder.append(temp);
            temp = listOfFiles(fileLists.get(MapKeys.LIST_NEW), LangEN.SHOW_STATUS_NEW);
            if (temp != null)
                stringBuilder.append(temp);

            return stringBuilder.toString();
        }
    },
    COMMIT {
        @Override
        public String executeCommand(String currentUser) {
            Magit magit = Main.engine.getSystem();
            try {
                System.out.println(LangEN.PLEASE_ENTER_YOUR_COMMANT);
                String comment = new Scanner(System.in).nextLine();
                magit.commitMagit(currentUser,comment);
                return LangEN.COMMIT_CREATED_SUCCESSFULLY;
            } catch (IOException e) {
                return LangEN.ERROR_CREATE_COMMIT_FILE;
            } catch (RepositoryException e) {
                return e.getCode().getPersonalMessage();
            } catch (MyFileException e) {
                return e.getCode().getPersonalMessage() + e.getFilename();
            }
        }
    },
    SHOW_ALL_BRANCHES {
        @Override
        public String executeCommand(String currentUser) {
            try {
                List<Branch> branches = Main.engine.getSystem().getCurrentRepository().getBranches();
                StringBuilder stringBuilder = new StringBuilder();
                for (Branch branch : branches) {
                    if(!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                        if (branch == Main.engine.getSystem().getCurrentBranch())
                            stringBuilder.append("*");
                        stringBuilder.append(branch.getName()).append(System.lineSeparator());
                        stringBuilder.append(branch.getCommit().getSHAONE()).append(System.lineSeparator());
                        stringBuilder.append(branch.getCommit().getComment()).append(System.lineSeparator());
                    }
                }
                return stringBuilder.toString();
            }
            catch (NullPointerException e)
            {
                return LangEN.NOTHING_TO_SHOW;
            }
        }
    },
    CREATE_NEW_BRANCH {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    DELETE_BRANCH {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    CHECK_OUT {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    SHOW_ACTIVE_BRANCH_HISTORY {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    },
    CREATE_NEW_REPO {
        @Override
        public String executeCommand(String currentUser) throws IOException, RepositoryException, MyFileException {
            Magit magit = Main.engine.getSystem();
            System.out.println(LangEN.ASK_FOR_PATH_FOR_REPO);
            String path = new Scanner(System.in).nextLine();
            magit.setRootFolder(Paths.get(path));
            try {
                magit.setCurrentRepository(new Repository(Paths.get(path),currentUser));
            } catch (RepositoryException e) {
                if (e.getCode() == eErrorCodes.MAGIT_FOLDER_ALREADY_EXIST) {
                    System.out.println(e.getCode().getPersonalMessage());
                    return LangEN.CREATE_NEW_REPOSITORY_FAILED_ALREADY_EXIST;
//                    String userChoice = new Scanner(System.in).nextLine();
//                    userChoice = userChoice.toLowerCase();
//                    if(userChoice.equals("yes") || userChoice.equals("y"))
//                    {
//                        magit.setCurrentRepository(new Repository(Paths.get(path),true));
//                        magit.loadOldRepository();
//                    }
//                    else
//                    {
//                        throw new RepositoryException(eErrorCodes.ERROR_LOAD_REPOSITORY);
//                    }
                } else {
                    throw new RepositoryException(e.getCode());
                }
            }
            return String.format(LangEN.NEW_REPOSITORY_CREATED_SUCCESSFULLY, magit.getCurrentRepository().getName());
        }
    },
    EXIT {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }
    };

    public abstract String executeCommand(String currentUser) throws RepositoryException, IOException, MyFileException;

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
