package magit.ui;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import magit.*;
import magit.settings.LangEN;
import magit.settings.Settings;
import utils.MapKeys;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.*;

public enum eMenuItem {
    OPEN_RESOURCE(LangEN.MENU_OPTION_OPEN_RESOURCE, Settings.MENU_ITEM_OPEN_RESOURCE) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            return null;
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    },
    NAME_UPDATE(LangEN.MENU_OPTION_NAME_UPDATE, Settings.MENU_ITEM_NAME_UPDATE) {
        @Override
        public boolean isAllow() {
            return true;
        }

        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String newName = printAndAskFromString(LangEN.PLEASE_ENTER_YOUR_NAME);
            magit.setCurrentUser(newName);
            return String.format(LangEN.CHANGE_NAME_SUCCESSFULLY_TO, newName);
        }
    },
    CHANGE_REPO(LangEN.MENU_OPTION_CHANGE_REPO, Settings.MENU_ITEM_CHANGE_REPO) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String path = printAndAskFromString(LangEN.PLEASE_ENTER_REPOSITORY_PATH);

            File basicPath = new File(path + File.separator + Settings.MAGIT_FOLDER);
            if (basicPath.exists()) {
                try {
                    magit.setCurrentRepository(new Repository(Paths.get(path), true, currentUser));
                } catch (RepositoryException e) {
                    return e.getCode().getPersonalMessage();
                } catch (IOException e) {
                    return LangEN.READING_FROM_FILE_FAILED;
                }
                return String.format(LangEN.LOAD_REPOSITORY_SUCCESS,magit.getCurrentRepository().getName());
            } else {
                return LangEN.LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT;
            }
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    },
    SHOW_ALL_HISTORY(LangEN.MENU_OPTION_SHOW_ALL_HISTORY, Settings.MENU_ITEM_SHOW_ALL_HISTORY) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            Commit lastCommit = magit.getCurrentRepository().getLastCommit();
            if (lastCommit == null)
                return LangEN.NO_COMMIT_HISTORY;
            else
                return lastCommit.showAllHistory();
        }
    },
    SHOW_CURRENT_STATUS(LangEN.MENU_OPTION_SHOW_CURRENT_STATUS, Settings.MENU_ITEM_SHOW_CURRENT_STATUS) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws IOException {
            Repository repository = magit.getCurrentRepository();
            Map<MapKeys, List<BasicFile>> fileLists = null;
            try {
                fileLists = repository.scanRepository(currentUser);
            } catch (MyFileException e) {
                System.out.println(e.getCode().getPersonalMessage());
            } catch (RepositoryException e) {
                System.out.println(e.getCode().getPersonalMessage());
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(LangEN.REPOSITORY_NAME).append(repository.getName())
                    .append(LangEN.REPOSITORY_PATH).append(repository.getMagitPath()).append(System.lineSeparator());

            assert fileLists != null;
            for (Map.Entry<MapKeys, List<BasicFile>> entry : fileLists.entrySet()) {
                List<BasicFile> temp = entry.getValue();
                if (temp.size() > 0) {
                    stringBuilder.append(entry.getKey()).append(System.lineSeparator());
                    for (BasicFile file : temp) {
                        stringBuilder.append(file).append(System.lineSeparator());
                    }
                    stringBuilder.append(Settings.SHOW_STATUS_SEPARATOR).append(System.lineSeparator());
                }
            }

            return stringBuilder.toString();
        }
    },
    COMMIT(LangEN.MENU_OPTION_COMMIT, Settings.MENU_ITEM_COMMIT) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                String comment = printAndAskFromString(LangEN.PLEASE_ENTER_YOUR_COMMENT);
                magit.commitMagit(currentUser, comment);
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
    SHOW_ALL_BRANCHES(LangEN.MENU_OPTION_SHOW_ALL_BRANCHES, Settings.MENU_ITEM_SHOW_ALL_BRANCHES) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                List<Branch> branches = magit.getCurrentRepository().getBranches();
                StringBuilder stringBuilder = new StringBuilder();
                for (Branch branch : branches) {
                    if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                        if (branch.equals(magit.getCurrentBranch()))
                            stringBuilder.append(LangEN.HEAD_ACTIVE_BRANCH_SIGN);
                        stringBuilder.append(branch).append(System.lineSeparator());
                    }
                }
                return stringBuilder.toString();
            } catch (NullPointerException e) {
                return LangEN.NOTHING_TO_SHOW;
            }
        }
    },
    CREATE_NEW_BRANCH(LangEN.MENU_OPTION_CREATE_NEW_BRANCH, Settings.MENU_ITEM_CREATE_NEW_BRANCH) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            Branch activeBranch = magit.getCurrentBranch();
            Commit lastCommit = activeBranch.getCommit();

            String newBranchName = printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_ADD + ": ");

            File newBranch = new File(magit.getCurrentRepository().getBranchesPath() + File.separator + newBranchName);
            if (newBranch.exists()) {
                return String.format(LangEN.BRANCH_ALREADY_EXIST, newBranchName);
            } else {
                try {
                    Branch branch = new Branch(newBranchName, lastCommit, magit.getCurrentRepository().getBranchesPath().toString());
                    PrintWriter writer = new PrintWriter(newBranch);
                    if (lastCommit != null)
                        writer.print(branch.getCommit().getSHAONE());
                    else
                        writer.print(Settings.EMPTY_COMMIT);
                    writer.close();
                    magit.getCurrentRepository().addBranch(branch);
                    return String.format(LangEN.BRANCH_CREATED_SUCCESSFULLY, newBranchName);
                } catch (IOException e) {
                    return String.format(LangEN.CREATE_BRANCH_FILE_FAILED, newBranchName);
                } catch (RepositoryException e) {
                    return String.format(LangEN.CREATE_BRANCH_FILE_FAILED, newBranchName) + e.getCode().getPersonalMessage();
                }
            }
        }
    },
    DELETE_BRANCH(LangEN.MENU_OPTION_DELETE_BRANCH, Settings.MENU_ITEM_DELETE_BRANCH) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String branchName = printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_DELETE + ": ");

            if (magit.getCurrentBranch().getName().equals(branchName)) {
                return LangEN.CANNOT_DELETE_ACTIVE_BRANCH;
            }
            String pathToBranches = magit.getCurrentRepository().getBranchesPath().toString();
            File branch = new File(pathToBranches + File.separator + branchName);
            if (branch.exists()) {
                if (branch.delete()) {
                    magit.removeBranch(branchName);
                    return LangEN.DELETE_BRANCH_SUCCESS;
                }
                return String.format(LangEN.BRANCH_DELETE_FAILED, branchName);
            }

            return String.format(LangEN.BRANCH_NOT_EXIST, branchName);
        }
    },
    CHECK_OUT(LangEN.MENU_OPTION_CHECK_OUT, Settings.MENU_ITEM_CHECK_OUT) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                if (magit.checkout(printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME))) {
                    return LangEN.CHECKOUT_COMPLETE_SUCCESSFULLY;
                }
                return LangEN.CHECKOUT_FAILED;
            } catch (IOException e) {
                return String.format(LangEN.READING_FROM_FILE_FAILED, e.getMessage());
            } catch (MyFileException e) {
                return e.getCode().getPersonalMessage();
            } catch (RepositoryException e) {
                return e.getCode().getPersonalMessage();
            }
        }
    },
    SHOW_ACTIVE_BRANCH_HISTORY(LangEN.MENU_OPTION_SHOW_ACTIVE_BRANCH_HISTORY, Settings.MENU_ITEM_SHOW_ACTIVE_BRANCH_HISTORY) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            return magit.getCurrentBranch().getCommitDataHistory();
        }
    },
    CREATE_NEW_REPO(LangEN.MENU_OPTION_CREATE_NEW_REPO, Settings.MENU_ITEM_CREATE_NEW_REPO) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws IOException, RepositoryException {
            String path, name;
            path = printAndAskFromString(LangEN.PLEASE_ENTER_REPOSITORY_PATH);
            name = printAndAskFromString(LangEN.PLEASE_ENTER_REPOSITORY_NAME);

            magit.setRootFolder(Paths.get(path));
            try {
                magit.setCurrentRepository(new Repository(Paths.get(path), currentUser, name));
            } catch (RepositoryException e) {
                if (e.getCode() == eErrorCodes.MAGIT_FOLDER_ALREADY_EXIST) {
                    System.out.println(e.getCode().getPersonalMessage());
                    return LangEN.CREATE_NEW_REPOSITORY_FAILED_ALREADY_EXIST;
                } else {
                    throw new RepositoryException(e.getCode());
                }
            }
            return String.format(LangEN.NEW_REPOSITORY_CREATED_SUCCESSFULLY, magit.getCurrentRepository().getName());
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    },
    EXIT(LangEN.MENU_OPTION_EXIT, Settings.MENU_ITEM_EXIT) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            return null;
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    };

    private String name;
    private int item;

    eMenuItem(String name, int item) {
        this.name = name;
        this.item = item;
    }

    public abstract String executeCommand(String currentUser, Magit magit) throws RepositoryException, IOException, MyFileException;

    public boolean isAllow() {
        return false;
    }

    public String getName() {
        return name;
    }

    public int getItem() {
        return item;
    }

    public static Optional<eMenuItem> getItem(int index) {
        return Arrays.stream(eMenuItem.values()).filter(e -> index == e.getItem()).findFirst();
    }

    public String printAndAskFromString(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextLine();
    }
}