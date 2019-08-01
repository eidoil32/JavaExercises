package magit;


import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import utils.MapKeys;
import utils.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class Magit {
    private Path rootFolder;
    private Branch currentBranch;
    private String currentUser;
    private Repository currentRepository;

    public Magit() {
        currentUser = Settings.USER_ADMINISTRATOR;
        currentBranch = null;
        rootFolder = null;
        currentRepository = null;
    }

    public Path getRootFolder() {
        return rootFolder;
    }

    public void setRootFolder(Path rootFolder) {
        this.rootFolder = rootFolder;
    }

    public Branch getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(Branch currentBranch) throws RepositoryException {
        this.currentBranch = currentBranch;
        File head = new File(currentRepository.getBranchesPath() + File.separator + Settings.MAGIT_BRANCH_HEAD);
        if (head.exists()) {
            try {
                PrintWriter writer = new PrintWriter(head);
                writer.print(currentBranch.getName());
                writer.close();
            } catch (FileNotFoundException e) {
                throw new RepositoryException(eErrorCodes.OPEN_FILE_FAILED);
            }

        } else
            throw new RepositoryException(eErrorCodes.OPEN_FILE_FAILED);
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String currentUser) {
        this.currentUser = currentUser;
    }

    public Repository getCurrentRepository() {
        return currentRepository;
    }

    public void setCurrentRepository(Repository currentRepository) {
        this.currentRepository = currentRepository;
        this.currentBranch = currentRepository.getActiveBranch();
    }

    public void commitMagit(String currentUser, String comment) throws IOException, MyFileException, RepositoryException {
        Commit newCommit = new Commit(currentBranch.getCommit());
        newCommit.createCommitFile(currentRepository, currentRepository.scanRepository(currentUser), currentUser, comment);
        currentBranch.setCommit(newCommit, currentRepository.getBranchesPath().toString());
        currentRepository.setLastCommit(newCommit);
    }

    public void loadOldRepository(String path) throws RepositoryException, IOException, ParseException {
        currentRepository.loadBranches();
        currentBranch = currentRepository.getActiveBranch();
        Commit commit = new Commit();
        commit.loadDataFromFile(currentRepository.getObjectPath(), currentBranch.getSHA_ONE());
    }

    public void deleteOldFiles(String rootPath) {
        File root = new File(rootPath);
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory())
                            deleteOldFiles(file.getAbsolutePath());
                        else
                            file.delete();
                    }
                }
            }
            root.delete();
        }
    }

    public boolean checkout(String branchName) throws RepositoryException, MyFileException, IOException {
        Map<MapKeys, List<BasicFile>> delta = currentRepository.scanRepository(currentUser);
        for (Map.Entry<MapKeys, List<BasicFile>> entry : delta.entrySet()) {
            if (entry.getValue().size() > 0)
                throw new RepositoryException(eErrorCodes.THERE_IS_OPENED_ISSUES);
        }

        if (new File(currentRepository.getBranchesPath() + File.separator + branchName).exists()) {
            Branch branch = currentRepository.searchBranch(branchName);
            if (branch == null)
                throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
            else {
                currentBranch = branch;
                deleteOldFiles(this.rootFolder.toString());
                currentRepository.getRootFolder().setBlobMap(currentRepository.loadDataFromCommit(branch.getCommit()));
                layoutRepositoryByRootFolder();
                return true;
            }
        }
        else {
            throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
        }
    }

    private void layoutRepositoryByRootFolder() {
    }

    public void removeBranch(String branchName) {
        List<Branch> branches = currentRepository.getBranches();
        Branch temp = new Branch(branchName);
        int index = branches.indexOf(temp);
        if(index != -1)
            branches.remove(index);
    }
}
