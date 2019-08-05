package magit.ui;

import exceptions.*;
import languages.LangEN;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import magit.Repository;
import org.apache.commons.io.FilenameUtils;
import settings.Settings;
import utils.FileManager;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public enum eMenuItem {
    OPEN_RESOURCE(LangEN.MENU_OPTION_OPEN_RESOURCE, Settings.MENU_ITEM_OPEN_RESOURCE, true) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String xmlPath = printAndAskFromString(LangEN.ENTER_XML_PATH);
            File xmlFile = new File(xmlPath);
            if (xmlFile.exists()) {
                String extension = FilenameUtils.getExtension(xmlFile.getName());
                if (extension.equals(Settings.XML_EXTENSION)) {
                    while (true) {
                        try {
                            File initialFile = new File(xmlPath);
                            InputStream inputStream = new FileInputStream(initialFile);
                            MagitRepository magitRepository = FileManager.deserializeFrom(inputStream);
                            magit.basicCheckXML(magitRepository);
                            magit.setCurrentRepository(Repository.XML_RepositoryFactory(magitRepository));
                            magit.afterXMLLayout();
                            return LangEN.LOAD_REPOSITORY_FROM_XML_SUCCESSED;
                        } catch (IOException e) {
                            return LangEN.UNKNOWN_FATAL_ERROR + e.getMessage();
                        } catch (RepositoryException e) {
                            return e.getCode().getMessage();
                        } catch (MyFileException e) {
                            return e.getCode().getMessage();
                        } catch (MyXMLException e) {
                            System.out.println(LangEN.ERROR_WHILE_LOAD_XML);
                            if (e.getCode() == eErrorCodesXML.ALREADY_EXIST_FOLDER) {
                                String choice = printAndAskFromString(LangEN.XML_DELETE_AND_START_NEW_REPOSITORY);
                                if (choice.equals(LangEN.LOWERCASE_SHORT_YES) || choice.equals(LangEN.LOWERCASE_LONG_YES)) {
                                    magit.deleteOldMagitFolder(e.getAdditionalData());
                                }
                            } else if (e.getCode() == eErrorCodesXML.TARGET_FOLDER_NOT_EMPTY) {
                                return e.getMessage();
                            } else {
                                return e.getMessage();
                            }
                        } catch (JAXBException e) {
                            return LangEN.XML_PARSE_FAILED;
                        }
                    }
                } else {
                    return LangEN.NONE_XML_FILE_EXTINCTION;
                }
            } else {
                return String.format(LangEN.XML_FILE_NOT_FOUND, xmlFile.getName());
            }
        }
    },

    NAME_UPDATE(LangEN.MENU_OPTION_NAME_UPDATE, Settings.MENU_ITEM_NAME_UPDATE, true) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String newName = printAndAskFromString(LangEN.PLEASE_ENTER_YOUR_NAME);
            magit.setCurrentUser(newName);
            return String.format(LangEN.CHANGE_NAME_SUCCESSFULLY_TO, newName);
        }
    },

    CHANGE_REPO(LangEN.MENU_OPTION_CHANGE_REPO, Settings.MENU_ITEM_CHANGE_REPO, true) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String path = printAndAskFromString(LangEN.ASK_FOR_PATH_FOR_REPO);

            try {
                if (magit.changeRepo(path)) {
                    return String.format(LangEN.LOAD_REPOSITORY_SUCCESS, magit.getCurrentRepository().getName());
                }
                return LangEN.LOAD_REPOSITORY_FAILED_NOT_EXIST_MAGIT;
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            } catch (IOException e) {
                return LangEN.UNKNOWN_FATAL_ERROR + e.getMessage();
            }
        }
    },

    SHOW_ALL_HISTORY(LangEN.MENU_OPTION_SHOW_ALL_HISTORY, Settings.MENU_ITEM_SHOW_ALL_HISTORY, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                String result = magit.showCurrentCommitHistory();
                if (result != null)
                    return result;
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            } catch (IOException e) {
                return LangEN.UNKNOWN_FATAL_ERROR + e.getMessage();
            } catch (MyFileException e) {
                return e.getCode().getMessage();
            }
            return LangEN.NO_COMMIT_HISTORY;
        }
    },

    SHOW_CURRENT_STATUS(LangEN.MENU_OPTION_SHOW_CURRENT_STATUS, Settings.MENU_ITEM_SHOW_CURRENT_STATUS, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws IOException {
            try {
                return magit.showCurrentStatus();
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            } catch (MyFileException e) {
                return e.getCode().getMessage();
            }
        }
    },

    COMMIT(LangEN.MENU_OPTION_COMMIT, Settings.MENU_ITEM_COMMIT, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                String comment = printAndAskFromString(LangEN.PLEASE_ENTER_YOUR_COMMENT);
                magit.commitMagit(currentUser, comment);
                return LangEN.COMMIT_CREATED_SUCCESSFULLY;
            } catch (IOException e) {
                return LangEN.ERROR_CREATE_COMMIT_FILE;
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            } catch (MyFileException e) {
                return e.getCode().getMessage() + e.getFilename();
            }
        }
    },

    SHOW_ALL_BRANCHES(LangEN.MENU_OPTION_SHOW_ALL_BRANCHES, Settings.MENU_ITEM_SHOW_ALL_BRANCHES, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            try {
                return magit.showAllBranches();
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            }
        }
    },

    CREATE_NEW_BRANCH(LangEN.MENU_OPTION_CREATE_NEW_BRANCH, Settings.MENU_ITEM_CREATE_NEW_BRANCH, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            Branch activeBranch = magit.getCurrentBranch();
            Commit lastCommit = activeBranch.getCommit();

            String newBranchName = printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_ADD + ": ");

            try {
                if (magit.tryCreateNewBranch(newBranchName)) {
                    String choice = printAndAskFromString(LangEN.DO_CHECKOUT_NOW);
                    choice = choice.toLowerCase();
                    if (choice.equals(LangEN.LOWERCASE_SHORT_YES) || choice.equals(LangEN.LOWERCASE_LONG_YES)) {
                        if (magit.tryCheckout(newBranchName)) {
                            return LangEN.CHECKOUT_COMPLETE_SUCCESSFULLY;
                        } else {
                            throw new RepositoryException(eErrorCodes.THERE_IS_OPENED_ISSUES);
                        }
                    }
                    return String.format(LangEN.BRANCH_CREATED_SUCCESSFULLY, newBranchName);
                }
                return LangEN.UNKNOWN_FATAL_ERROR + LangEN.CREATE_BRANCH_FILE_FAILED;
            } catch (RepositoryException e) {
                if (e.getCode() == eErrorCodes.THERE_IS_OPENED_ISSUES) {
                    return String.format(LangEN.BRANCH_CREATED_BUT_WITHOUT_CHECKOUT, newBranchName);
                }
                return String.format(LangEN.CREATE_BRANCH_FILE_FAILED, newBranchName) + e.getCode().getMessage();
            } catch (IOException e) {
                return String.format(LangEN.CREATE_BRANCH_FILE_FAILED, newBranchName);
            } catch (MyFileException e) {
                return e.getCode().getMessage();
            }
        }
    },

    DELETE_BRANCH(LangEN.MENU_OPTION_DELETE_BRANCH, Settings.MENU_ITEM_DELETE_BRANCH, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String branchName = printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME + LangEN.YOU_WANT_TO_DELETE + ": ");
            try {
                if (magit.deleteBranch(branchName)) {
                    return LangEN.DELETE_BRANCH_SUCCESS;
                } else {
                    return LangEN.DELETE_FILE_FAILED;
                }
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            }
        }
    },

    CHECK_OUT(LangEN.MENU_OPTION_CHECK_OUT, Settings.MENU_ITEM_CHECK_OUT, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            String branchName = printAndAskFromString(LangEN.PLEASE_ENTER_BRANCH_NAME);
            try {
                if (magit.tryCheckout(branchName)) {
                    return LangEN.CHECKOUT_COMPLETE_SUCCESSFULLY;
                } else {
                    String choice = printAndAskFromString(LangEN.IGNORE_OPENED_ISSUES);
                    if (choice.equals(LangEN.LOWERCASE_LONG_YES) || choice.equals(LangEN.LOWERCASE_SHORT_YES)) {
                        if (magit.checkout(branchName))
                            return LangEN.CHECKOUT_COMPLETE_SUCCESSFULLY;
                    } else {
                        return LangEN.CHECKOUT_FAILED_USER_CANCEL;
                    }
                }
                return LangEN.CHECKOUT_FAILED;
            } catch (IOException e) {
                return String.format(LangEN.READING_FROM_FILE_FAILED, e.getMessage());
            } catch (MyFileException e) {
                return e.getCode().getMessage();
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            }
        }
    },

    SHOW_ACTIVE_BRANCH_HISTORY(LangEN.MENU_OPTION_SHOW_ACTIVE_BRANCH_HISTORY, Settings.MENU_ITEM_SHOW_ACTIVE_BRANCH_HISTORY, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            return magit.getCurrentBranch().getCommitDataHistory();
        }
    },

    CREATE_NEW_REPO(LangEN.MENU_OPTION_CREATE_NEW_REPO, Settings.MENU_ITEM_CREATE_NEW_REPO, true) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws IOException, RepositoryException {
            String path, name;
            path = printAndAskFromString(LangEN.PLEASE_ENTER_REPOSITORY_PATH);
            name = printAndAskFromString(LangEN.PLEASE_ENTER_REPOSITORY_NAME);

            magit.setRootFolder(Paths.get(path));
            try {
                magit.checkCleanDir(path);
                magit.setCurrentRepository(new Repository(Paths.get(path), currentUser, name));
            } catch (RepositoryException e) {
                if (e.getCode() == eErrorCodes.MAGIT_FOLDER_ALREADY_EXIST) {
                    System.out.println(e.getCode().getMessage());
                    return LangEN.CREATE_NEW_REPOSITORY_FAILED_ALREADY_EXIST;
                } else if (e.getCode() == eErrorCodes.TARGET_DIR_NOT_EMPTY) {
                    return e.getCode().getMessage();
                } else {
                    throw new RepositoryException(e.getCode());
                }
            }
            return String.format(LangEN.NEW_REPOSITORY_CREATED_SUCCESSFULLY, magit.getCurrentRepository().getName());
        }
    },

    MENU_OPTION_RESET_BRANCH_TO_COMMIT(LangEN.MENU_OPTION_RESET_BRANCH_TO_COMMIT, Settings.MENU_ITEM_RESET_BRANCH_TO_COMMIT, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws RepositoryException, IOException, MyFileException {
            String user_SHA_ONE = printAndAskFromString(String.format(LangEN.CHOOSE_SHA_ONE_FOR_BRANCH, magit.getCurrentBranch().getName()));
            try {
                Commit oldCommit = magit.changeBranchPoint(user_SHA_ONE);
                if (oldCommit == null) {
                    return LangEN.CHANGING_SHA_ONE_SUCCESS;
                } else {
                    String choice = printAndAskFromString(LangEN.IGNORE_OPENED_ISSUES);
                    if (choice.equals(LangEN.LOWERCASE_LONG_YES) || choice.equals(LangEN.LOWERCASE_SHORT_YES)) {
                        if (magit.checkout(magit.getCurrentBranch().getName()))
                            return LangEN.CHANGING_SHA_ONE_SUCCESS;
                    } else {
                        magit.recoverOldCommit(oldCommit);
                        return LangEN.CHANGING_SHA_ONE_FAILED;
                    }
                }
            } catch (IOException e) {
                return e.getMessage();
            } catch (RepositoryException e) {
                return e.getCode().getMessage();
            } catch (MyFileException e) {
                return e.getCode().getMessage();
            }
            return LangEN.CHANGING_SHA_ONE_FAILED;
        }
    },

    EXPORT_REPOSITORY_TO_XML(LangEN.MENU_OPTION_EXPORT_TO_XML, Settings.MENU_ITEM_EXPORT_REPO, false) {
        @Override
        public String executeCommand(String currentUser, Magit magit) throws RepositoryException, IOException, MyFileException {
            String pathToXML = printAndAskFromString(LangEN.PLEASE_ENTER_TARGET_XML_PATH);
            try {
                MagitRepository magitRepository = magit.convertToXMLScheme();
                magit.exportFile(magitRepository, pathToXML);
                return String.format(LangEN.EXPORT_TO_XML_SUCCESS, pathToXML);
            } catch (JAXBException e) {
                return LangEN.UNKNOWN_FATAL_ERROR + e.getErrorCode() +
                        LangEN.EXPORT_TO_XML_FAILED + System.lineSeparator();
            }
        }
    },

    EXIT(LangEN.MENU_OPTION_EXIT, Settings.MENU_ITEM_EXIT, true) {
        @Override
        public String executeCommand(String currentUser, Magit magit) {
            return null;
        }
    };

    private String name;
    private int item;
    private boolean isAllow; //allow to use this command when no repository loaded

    eMenuItem(String name, int item, boolean isAllow) {
        this.name = name;
        this.item = item;
        this.isAllow = isAllow;
    }

    public abstract String executeCommand(String currentUser, Magit magit) throws RepositoryException, IOException, MyFileException;

    public boolean isAllow() {
        return this.isAllow;
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