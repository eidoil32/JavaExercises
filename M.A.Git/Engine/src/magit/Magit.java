package magit;


import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import languages.LangEN;
import settings.Settings;
import utils.FileManager;
import utils.MapKeys;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class Magit {
    private Path rootFolder;
    private Branch currentBranch;
    private String currentUser;
    private Repository currentRepository;

    public Magit() {
        this.currentUser = LangEN.USER_ADMINISTRATOR;
        this.currentBranch = null;
        this.rootFolder = null;
        this.currentRepository = null;
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
        this.rootFolder = currentRepository.getRootFolder().getFilePath();
    }

    public void commitMagit(String currentUser, String comment) throws IOException, MyFileException, RepositoryException {
        Commit newCommit = new Commit(currentBranch.getCommit());
        newCommit.createCommitFile(currentRepository, currentRepository.scanRepository(currentUser), currentUser, comment);
        currentBranch.setCommit(newCommit, currentRepository.getBranchesPath().toString());
        currentRepository.setLastCommit(newCommit);
    }

    public void deleteOldFiles(String rootPath) {
        File root = new File(rootPath);
        if (root.exists()) {
            if (root.isDirectory()) {
                File[] files = root.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.getName().equals(Settings.MAGIT_FOLDER)) {
                            if (file.isDirectory())
                                deleteOldFiles(file.getAbsolutePath());
                            file.delete();
                        }
                    }
                }
            }
        }
    }

    public boolean tryCheckout(String branchName) throws RepositoryException, MyFileException, IOException {
        Map<MapKeys, List<BasicFile>> delta = currentRepository.scanRepository(currentUser);
        if (delta == null) {
            return checkout(branchName);
        }
        return false;
    }

    private void setNewActiveBranch(Branch branch) throws RepositoryException, FileNotFoundException {
        currentBranch = branch;
        File head = new File(currentRepository.getBranchesPath().toString() + File.separator + Settings.MAGIT_BRANCH_HEAD);
        if (head.exists()) {
            PrintWriter writer = new PrintWriter(head);
            writer.print(branch.getName());
            writer.close();
        } else {
            throw new RepositoryException(eErrorCodes.MAGIT_FOLDER_CORRUPTED);
        }
    }

    private void layoutRepositoryByRootFolder(Map<BasicFile, Blob> map) throws IOException {
        for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
            Blob temp = entry.getValue();
            File file = new File(temp.getFullPathName());
            if (temp.getType() == eFileTypes.FOLDER) {
                file.mkdirs();
                layoutRepositoryByRootFolder(((Folder) temp).getBlobMap().getMap());
            } else {
                file.createNewFile();
                PrintWriter writer = new PrintWriter(file);
                writer.print(temp.getContent());
                writer.close();
            }
        }
    }

    public void removeBranch(String branchName) {
        List<Branch> branches = currentRepository.getBranches();
        Branch temp = new Branch(branchName);
        int index = branches.indexOf(temp);
        if (index != -1)
            branches.remove(index);
    }

    public String showCurrentCommitHistory() throws RepositoryException, IOException, MyFileException {
        Folder rootFolder = currentRepository.getRootFolder();
        if (rootFolder.getBlobMap().isEmpty()) {
            rootFolder.setBlobMap(currentRepository.loadDataFromCommit(currentRepository.getLastCommit()));
        }

        return getFullDataFromBlobMap(rootFolder.getBlobMap().getMap());
    }

    private String getFullDataFromBlobMap(Map<BasicFile, Blob> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
            Blob blob = entry.getValue();
            if (blob.getType() == eFileTypes.FOLDER) {
                stringBuilder.append(getFullDataFromBlobMap(((Folder) blob).getBlobMap().getMap())).append(System.lineSeparator());
            }
            stringBuilder.append(blob).append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }

    public boolean checkout(String branchName) throws RepositoryException, IOException, MyFileException {
        if (new File(currentRepository.getBranchesPath() + File.separator + branchName).exists()) {
            Branch branch = currentRepository.searchBranch(branchName);
            if (branch == null)
                throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
            else {
                setNewActiveBranch(branch);
                deleteOldFiles(this.rootFolder.toString());
                currentRepository.getRootFolder().setBlobMap(currentRepository.loadDataFromCommit(branch.getCommit()));
                layoutRepositoryByRootFolder(currentRepository.getRootFolder().getBlobMap().getMap());
                return true;
            }
        } else {
            throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
        }
    }

    public Commit changeBranchPoint(String user_sha_one) throws RepositoryException, IOException, MyFileException {
        Commit commit = loadCommitBySHA(user_sha_one);
        if (commit == null) {
            throw new RepositoryException(eErrorCodes.NO_COMMIT_WITH_SHA_ONE_EXISTS);
        } else {
            Commit oldCommit = currentBranch.getCommit();
            if (currentRepository.scanRepository(currentUser) != null) { //there is opened changes
                return oldCommit;
            } else { // there's no opened changes
                if (checkout(currentBranch.getName())) {
                    currentBranch.setCommit(commit, currentRepository.getBranchesPath().toString());
                    return null;
                } else {
                    throw new RepositoryException(eErrorCodes.UNKNOWN_ERROR);
                }
            }
        }
    }

    private Commit loadCommitBySHA(String user_sha_one) throws IOException, RepositoryException {
        Path pathToObject = currentRepository.getObjectPath();
        File commit = new File(pathToObject + File.separator + user_sha_one);
        if (commit.exists()) {
            List<String> commitContent = Files.readAllLines(commit.toPath());
            if (checkIsRealCommitFile(commitContent)) {
                Commit recoveredCommit = new Commit();
                recoveredCommit.loadDataFromFile(pathToObject, user_sha_one);
                return recoveredCommit;
            } else {
                return null;
            }
        }
        return null;
    }

    private boolean checkIsRealCommitFile(List<String> lines) throws IOException, RepositoryException {
        if (lines.size() == 5) {
            if (lines.get(0).length() != 40) {
                return false;
            } else {
                String prevCommit = lines.get(1);
                if (!prevCommit.equals(Settings.EMPTY_COMMIT)) {
                    if (prevCommit.length() != 40) {
                        return false;
                    } else {
                        if (loadCommitBySHA(prevCommit) == null) {
                            return false;
                        }
                    }
                }
            }
            try {
                new SimpleDateFormat(Settings.DATE_FORMAT).parse(lines.get(3));
                return true;
            } catch (ParseException e) {
                return false;
            }
        }
        return false;
    }

    public Magit XML_MagitFactory(String xml_path) throws RepositoryException, IOException, MyXMLException {
        InputStream inputStream = Magit.class.getResourceAsStream(xml_path);
        try {
            MagitRepository magit = FileManager.deserializeFrom(inputStream);
            return parseMagitRepository(magit);
        } catch (JAXBException e) {
            throw new RepositoryException(eErrorCodes.XML_PARSE_FAILED);
        }
    }

    private Magit parseMagitRepository(MagitRepository xmlMagit) throws RepositoryException, IOException, MyXMLException {
        Magit magit = new Magit();
        magit.setRootFolder(Paths.get(xmlMagit.getLocation()));
        Repository repository = Repository.XML_RepositoryFactory(xmlMagit);
        magit.setCurrentRepository(repository);
        magit.setCurrentBranch(repository.getActiveBranch());
        return magit;
    }

    public void recoverOldCommit(Commit oldCommit) throws RepositoryException {
        currentBranch.setCommit(oldCommit, currentRepository.getBranchesPath().toString());
    }
}
