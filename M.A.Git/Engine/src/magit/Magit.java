package magit;


import exceptions.*;
import languages.LangEN;
import settings.Settings;
import utils.MapKeys;
import utils.WarpBasicFile;
import utils.WarpInteger;
import xml.basic.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
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
        if (currentRepository.getRootFolder() != null)
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
                throw new MyFileException(eErrorCodes.BRANCH_NOT_EXIST, branchName);
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
                currentBranch.setCommit(commit, currentRepository.getBranchesPath().toString());
                if (checkout(currentBranch.getName())) {
                    currentRepository.setLastCommit(currentBranch.getCommit());
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

    public void recoverOldCommit(Commit oldCommit) throws RepositoryException {
        currentBranch.setCommit(oldCommit, currentRepository.getBranchesPath().toString());
    }

    public void afterXMLLayout() throws IOException {
        if (currentRepository.getRootFolder() != null) {
            layoutRepositoryByRootFolder(currentRepository.getRootFolder().getBlobMap().getMap());
        }
    }

    public void deleteOldMagitFolder(String path) {
        deleteOldFiles(path + File.separator + Settings.MAGIT_FOLDER);
        new File(path + File.separator + Settings.MAGIT_FOLDER).delete();
    }

    public void basicCheckXML(MagitRepository magitRepository) throws MyXMLException {
        List<MagitBlob> blobs = magitRepository.getMagitBlobs().getMagitBlob();
        List<MagitSingleFolder> folders = magitRepository.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleCommit> commits = magitRepository.getMagitCommits().getMagitSingleCommit();

        Map<Integer, Object> tempMap = new HashMap<>();

        for (MagitBlob blob : blobs) {
            if (tempMap.containsKey(Integer.parseInt(blob.getId()))) {
                throw new MyXMLException(eErrorCodesXML.DUPLICATE_ID_NUMBER, blob.getId());
            } else {
                tempMap.put(Integer.parseInt(blob.getId()), blob);
            }
        }

        tempMap = new HashMap<>();
        for (MagitSingleFolder folder : folders) {
            if (tempMap.containsKey(Integer.parseInt(folder.getId()))) {
                throw new MyXMLException(eErrorCodesXML.DUPLICATE_ID_NUMBER, folder.getId());
            } else {
                tempMap.put(Integer.parseInt(folder.getId()), folder);
            }
        }

        tempMap = new HashMap<>();
        for (MagitSingleCommit commit : commits) {
            if (tempMap.containsKey(Integer.parseInt(commit.getId()))) {
                throw new MyXMLException(eErrorCodesXML.DUPLICATE_ID_NUMBER, commit.getId());
            } else {
                tempMap.put(Integer.parseInt(commit.getId()), commit);
            }
        }

    }

    public void checkCleanDir(String path) throws RepositoryException {
        File target = new File(path);
        if (target.exists()) {
            if (target.listFiles() != null && target.listFiles().length > 0) {
                throw new RepositoryException(eErrorCodes.TARGET_DIR_NOT_EMPTY);
            }
        }
    }

    public MagitRepository convertToXMLScheme() throws RepositoryException, IOException, MyFileException {
        WarpInteger counterCommits = new WarpInteger(),
                counterBlobs = new WarpInteger(), counterFolders = new WarpInteger();
        MagitRepository exportRepository = new MagitRepository();
        exportRepository.setLocation(rootFolder.toString());
        exportRepository.setName(currentRepository.getName());

        Map<WarpBasicFile, Integer> myBlobs = new HashMap<>();
        Map<WarpBasicFile, Integer> myFolders = new HashMap<>();
        Map<Commit, Integer> myCommits = new HashMap<>();
        Map<String, Object> values = new HashMap<>();
        values.put(Settings.KEY_COUNTER_FILES, counterBlobs);
        values.put(Settings.KEY_COUNTER_FOLDERS, counterFolders);
        values.put(Settings.KEY_ROOT_FOLDER_PATH, rootFolder.toString());
        values.put(Settings.KEY_ALL_FILES, myBlobs);
        values.put(Settings.KEY_ALL_FOLDERS, myFolders);
        values.put(Settings.KEY_COUNTER_COMMIT, counterCommits);
        values.put(Settings.KEY_ALL_COMMITS, myCommits);

        MagitBranches branchManager = new MagitBranches();
        MagitCommits commitsManager = new MagitCommits();
        branchManager.setHead(currentBranch.getName());

        List<MagitSingleBranch> branches = branchManager.getMagitSingleBranch();
        List<MagitSingleCommit> commits = commitsManager.getMagitSingleCommit();

        for (Branch branch : currentRepository.getBranches()) {
            if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                MagitSingleBranch tempBranch = new MagitSingleBranch();
                Commit realCommit = branch.getCommit();
                tempBranch.setName(branch.getName());
                MagitSingleBranch.PointedCommit pointedCommit = new MagitSingleBranch.PointedCommit();
                myCommits.put(realCommit, counterCommits.number);
                commits.addAll(realCommit.convertToXMLCommit(currentRepository, currentRepository.loadDataFromCommit(realCommit), values));
                pointedCommit.setId(counterCommits.toString());
                tempBranch.setPointedCommit(pointedCommit);
                branches.add(tempBranch);
            }
        }

        exportRepository.setMagitCommits(commitsManager);
        exportRepository.setMagitBranches(branchManager);
        exportRepository.setMagitBlobs(parseMagitBlobsToXMLBlobs(myBlobs));
        exportRepository.setMagitFolders(parseMagitFoldersToXMLFolders(myFolders, myBlobs));

        return exportRepository;
    }

    private MagitBlobs parseMagitBlobsToXMLBlobs(Map<WarpBasicFile, Integer> myBlobs) {
        MagitBlobs blobs = new MagitBlobs();

        for (Map.Entry<WarpBasicFile, Integer> entry : myBlobs.entrySet()) {
            Blob tempBlob = entry.getKey().getFile();
            MagitBlob blob = new MagitBlob();
            blob.setId(entry.getValue().toString());
            blob.setLastUpdater(tempBlob.getEditorName());
            blob.setLastUpdateDate(new SimpleDateFormat(Settings.DATE_FORMAT).format(tempBlob.getDate()));
            blob.setContent(tempBlob.getContent());
            blob.setName(tempBlob.getName());
            blobs.getMagitBlob().add(blob);
        }

        return blobs;
    }

    private MagitFolders parseMagitFoldersToXMLFolders(Map<WarpBasicFile, Integer> myFolders, Map<WarpBasicFile, Integer> myBlobs) {
        MagitFolders folders = new MagitFolders();

        for (Map.Entry<WarpBasicFile, Integer> entry : myFolders.entrySet()) {
            Folder tempFolder = (Folder) entry.getKey().getFile();
            MagitSingleFolder folder = new MagitSingleFolder();
            folder.setId(entry.getValue().toString());
            folder.setName(tempFolder.getName());
            folder.setLastUpdater(tempFolder.getEditorName());
            folder.setItems(getItemsOfFolder(tempFolder,myFolders,myBlobs));
            folder.setLastUpdateDate(new SimpleDateFormat(Settings.DATE_FORMAT).format(tempFolder.getDate()));
            folder.setIsRoot(tempFolder.getRootFolder() == null);
            folders.getMagitSingleFolder().add(folder);
        }

        return folders;
    }

    private MagitSingleFolder.Items getItemsOfFolder(Folder tempFolder, Map<WarpBasicFile, Integer> myFolders, Map<WarpBasicFile, Integer> myBlobs) {
        MagitSingleFolder.Items listOfItems = new MagitSingleFolder.Items();

        for (Map.Entry<BasicFile,Blob> entry : tempFolder.getBlobMap().getMap().entrySet()) {
            Item temp = new Item();
            if(entry.getValue().getType() == eFileTypes.FILE) {
                temp.setId(myBlobs.get(new WarpBasicFile(entry.getValue())).toString());
                temp.setType(Settings.XML_ITEM_FILE_TYPE);
            } else {
                temp.setId(myFolders.get(new WarpBasicFile(entry.getValue())).toString());
                temp.setType(Settings.XML_ITEM_FOLDER_TYPE);
            }
            listOfItems.getItem().add(temp);
        }

        return listOfItems;
    }

    public boolean deleteBranch(String branchName) throws RepositoryException {
        if (currentBranch.getName().equals(branchName)) {
            throw new RepositoryException(eErrorCodes.CANNOT_DELETE_ACTIVE_BRANCH);
        } else {
            File branch = new File(currentRepository.getBranchesPath() + File.separator + branchName);
            if (branch.exists()) {
                if (branch.delete()) {
                    removeBranch(branchName);
                    return true;
                }
            } else {
                throw new RepositoryException(eErrorCodes.BRANCH_NOT_EXIST);
            }
        }
        return false;
    }

    public boolean tryCreateNewBranch(String branchName) throws RepositoryException, IOException {
        File newBranch = new File(currentRepository.getBranchesPath() + File.separator + branchName);
        if (newBranch.exists()) {
            throw new RepositoryException(eErrorCodes.BRANCH_ALREADY_EXIST);
        } else {
            Branch branch = new Branch(branchName, currentBranch.getCommit(), currentRepository.getBranchesPath().toString());
            PrintWriter writer = new PrintWriter(newBranch);
            if (currentBranch.getCommit() != null)
                writer.print(branch.getCommit().getSHA_ONE());
            else
                writer.print(Settings.EMPTY_COMMIT);
            writer.close();
            currentRepository.addBranch(branch);
            return true;
        }
    }

    public String showAllBranches() throws RepositoryException {
        List<Branch> branches = currentRepository.getBranches();
        if (branches.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Branch branch : branches) {
                if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                    if (branch.equals(currentBranch))
                        stringBuilder.append(LangEN.HEAD_ACTIVE_BRANCH_SIGN);
                    stringBuilder.append(branch).append(System.lineSeparator());
                }
            }
            return stringBuilder.toString();
        } else {
            throw new RepositoryException(eErrorCodes.NOTHING_TO_SEE);
        }
    }

    public String showCurrentStatus() throws RepositoryException, IOException, MyFileException {

        Map<MapKeys, List<BasicFile>> fileLists = currentRepository.scanRepository(currentUser);

        if (fileLists == null) {
            throw new RepositoryException(eErrorCodes.NOTHING_TO_SEE);
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(LangEN.REPOSITORY_NAME).append(currentRepository.getName())
                    .append(LangEN.REPOSITORY_PATH).append(currentRepository.getMagitPath()).append(System.lineSeparator());
            for (Map.Entry<MapKeys, List<BasicFile>> entry : fileLists.entrySet()) {
                List<BasicFile> temp = entry.getValue();
                if (temp.size() > 0) {
                    stringBuilder.append(entry.getKey()).append(System.lineSeparator());
                    for (BasicFile file : temp) {
                        stringBuilder.append(file.shortPath()).append(System.lineSeparator());
                    }
                    stringBuilder.append(Settings.SHOW_STATUS_SEPARATOR).append(System.lineSeparator());
                }
            }

            return stringBuilder.toString();
        }
    }

    public boolean changeRepo(String path) throws IOException, RepositoryException {
        File basicPath = new File(path + File.separator + Settings.MAGIT_FOLDER);
        if (basicPath.exists()) {
            currentRepository = new Repository(Paths.get(path), true, currentUser);
            rootFolder = Paths.get(path);
            currentBranch = currentRepository.getActiveBranch();
            currentUser = LangEN.USER_ADMINISTRATOR;
            return true;
        }
        return false;
    }

    public void exportFile(MagitRepository magitRepository, String pathToXML) throws JAXBException, IOException {
        OutputStream os = new FileOutputStream(pathToXML + File.separator + currentRepository.getName() + Settings.DOT_XML);
        JAXBContext jaxbContext = JAXBContext.newInstance(MagitRepository.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        jaxbMarshaller.marshal(magitRepository, os);
        os.close();
    }
}
