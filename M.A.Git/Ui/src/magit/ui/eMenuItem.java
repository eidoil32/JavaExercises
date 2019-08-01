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
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public enum eMenuItem {
    OPEN_RESOURCE {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    },
    NAME_UPDATE {
        @Override
        public boolean isAllow() {
            return true;
        }

        @Override
        public String executeCommand(String currentUser) {
            System.out.print(LangEN.PLEASE_ENTER_YOUR_NAME);
            String newName = new Scanner(System.in).nextLine();
            Main.engine.getSystem().setCurrentUser(newName);
            return String.format(LangEN.CHANGE_NAME_SUCCESSFULLY_TO, newName);
        }
    },
    CHANGE_REPO {
        @Override
        public String executeCommand(String currentUser) {
            Magit magit = Main.engine.getSystem();
            Scanner scanner = new Scanner(System.in);
            String path;
            System.out.print(LangEN.PLEASE_ENTER_REPOSITORY_PATH);
            path = scanner.nextLine();

            File basicPath = new File(path + File.separator + Settings.MAGIT_FOLDER);
            if (basicPath.exists()) {
                try {
                    magit.setCurrentRepository(new Repository(Paths.get(path), true, currentUser));
                } catch (RepositoryException e) {
                    return e.getCode().getPersonalMessage();
                } catch (IOException e) {
                    return LangEN.READING_FROM_FILE_FAILED;
                }
                return LangEN.LOAD_REPOSITORY_SUCCESS;
            } else {
                return LangEN.LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT;
            }
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    },
    SHOW_ALL_HISTORY {
        @Override
        public String executeCommand(String currentUser) {
            Commit lastCommit = Main.engine.getSystem().getCurrentRepository().getLastCommit();
            if (lastCommit == null)
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
    COMMIT {
        @Override
        public String executeCommand(String currentUser) {
            Magit magit = Main.engine.getSystem();
            try {
                System.out.println(LangEN.PLEASE_ENTER_YOUR_COMMENT);
                String comment = new Scanner(System.in).nextLine();
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
    SHOW_ALL_BRANCHES {
        @Override
        public String executeCommand(String currentUser) {
            try {
                List<Branch> branches = Main.engine.getSystem().getCurrentRepository().getBranches();
                StringBuilder stringBuilder = new StringBuilder();
                for (Branch branch : branches) {
                    if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                        if (branch == Main.engine.getSystem().getCurrentBranch())
                            stringBuilder.append("*");
                        stringBuilder.append(branch.getName()).append(System.lineSeparator());
                        stringBuilder.append(branch.getCommit().getSHAONE()).append(System.lineSeparator());
                        stringBuilder.append(branch.getCommit().getComment()).append(System.lineSeparator());
                    }
                }
                return stringBuilder.toString();
            } catch (NullPointerException e) {
                return LangEN.NOTHING_TO_SHOW;
            }
        }
    },
    CREATE_NEW_BRANCH {
        @Override
        public String executeCommand(String currentUser) {
            Magit magit = Main.engine.getSystem();
            Branch activeBranch = magit.getCurrentBranch();
            Commit lastCommit = activeBranch.getCommit();
            System.out.print(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_ADD + ": ");
            String newBranchName = new Scanner(System.in).nextLine();
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
    DELETE_BRANCH {
        @Override
        public String executeCommand(String currentUser) {
            System.out.print(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_DELETE + ": ");
            String branchName = new Scanner(System.in).nextLine();

            Magit magit = Main.engine.getSystem();
            if (magit.getCurrentBranch().getName().equals(branchName)) {
                return LangEN.CANNOT_DELETE_ACTIVE_BRANCH;
            }
            String pathToBranches = magit.getCurrentRepository().getBranchesPath().toString();
            File branch = new File(pathToBranches + File.separator + branchName);
            if (branch.exists()) {
                if (branch.delete()) {
                    return LangEN.DELETE_BRANCH_SUCCESS;
                }
                return String.format(LangEN.BRANCH_DELETE_FAILED, branchName);
            }

            return String.format(LangEN.BRANCH_NOT_EXIST, branchName);
        }
    },
    CHECK_OUT {
        @Override
        public String executeCommand(String currentUser) {
            Magit magit = Main.engine.getSystem();
            try {
                Map<MapKeys, List<BasicFile>> delta = magit.getCurrentRepository().scanRepository(currentUser);
                for (Map.Entry<MapKeys, List<BasicFile>> entry : delta.entrySet()) {
                    if (entry.getValue().size() > 0)
                        return LangEN.CANNOT_CHECKOUT;
                }

                String branchName = new Scanner(System.in).nextLine();
                if (new File(magit.getCurrentRepository().getBranchesPath() + File.separator + branchName).exists()) {
                    Repository repository = magit.getCurrentRepository();
                    Branch branch = repository.searchBranch(branchName);
                    if (branch == null)
                        return LangEN.MOVING_TO_BRANCH_FAILED_MAGIT_FATAL;
                    else {
                        magit.setCurrentBranch(branch);
                        magit.deleteOldFiles(repository.getMagitPath().toAbsolutePath().toString());
                        repository.loadLastCommit();
                        return LangEN.CHECKOUT_COMPLETE_SUCCESSFULLY;
                    }
                }
                return LangEN.MOVING_TO_BRANCH_FAILED_MAGIT_FATAL;
            } catch (IOException e) {
                return String.format(LangEN.READING_FROM_FILE_FAILED, e.getMessage());
            } catch (MyFileException e) {
                return e.getCode().getPersonalMessage();
            } catch (RepositoryException e) {
                return e.getCode().getPersonalMessage();
            }
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
            Scanner scanner = new Scanner(System.in);
            String path, name;
            System.out.print(LangEN.PLEASE_ENTER_REPOSITORY_PATH);
            path = scanner.nextLine();
            System.out.print(LangEN.PLEASE_ENTER_REPOSITORY_NAME);
            name = scanner.nextLine();

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
    EXIT {
        @Override
        public String executeCommand(String currentUser) {
            return null;
        }

        @Override
        public boolean isAllow() {
            return true;
        }
    };

    public abstract String executeCommand(String currentUser) throws RepositoryException, IOException, MyFileException;

    public int getPlace() {
        return Settings.get_number_of_menu.get(this);
    }

    public boolean isAllow() {
        return false;
    }
}