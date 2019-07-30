package magit;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import utils.FileManager;
import utils.MapKeys;
import utils.ParseSHA;
import utils.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {
    private Path currentPath, magitPath, objectPath, branchesPath;
    private Folder rootFolder;
    private Commit lastCommit;
    private List<Branch> branches;
    private String name, SHAONE;

    public Repository(Path repositoryPath, String currentUser) throws RepositoryException, IOException { // set clean repository
        this.currentPath = repositoryPath;
        initialisePaths();
        createNewMagitFolder();
        this.rootFolder = new Folder(repositoryPath, currentUser);
        this.lastCommit = null;
        this.branches = new LinkedList<>();
        Branch master = new Branch(Settings.MAGIT_BRANCH_MASTER);
        Branch head = new Branch(true, master);
        this.branches.add(master);
        this.branches.add(head);
        this.name = repositoryPath.getName(repositoryPath.getNameCount() - 1).toString();
        this.SHAONE = DigestUtils.sha1Hex(ParseSHA.parseSHA(this));
    }

    public Repository(Path repositoryPath, boolean isOld, String currentUser) throws IOException, RepositoryException {
        this.currentPath = repositoryPath;
        initialisePaths();
        List<String> activeBranchName = Files.readAllLines(Paths.get(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD));
        List<String> activeBranchContent = Files.readAllLines(Paths.get(branchesPath + File.separator + activeBranchName.get(0)));
        String commit_sha = activeBranchContent.get(0);
        if (commit_sha.equals(Settings.EMPTY_COMMIT)) {
            this.lastCommit = null;
        } else {
            this.lastCommit = new Commit(activeBranchContent.get(0), objectPath.toString());
        }
        this.branches = new LinkedList<>();
        Branch active, head;
        try {
            active = new Branch(activeBranchName.get(0), this.lastCommit, branchesPath.toString());
        } catch (RepositoryException e) {
            if (e.getCode() != eErrorCodes.BRANCH_ALREADY_EXIST) throw e;
            active = new Branch(activeBranchName.get(0));
            active.setCommit(this.lastCommit, branchesPath.toString(), active.getName());
        }
        head = new Branch(true, active);
        this.branches.add(active);
        this.branches.add(head);
        this.rootFolder = new Folder(repositoryPath, currentUser);
        this.name = repositoryPath.getName(repositoryPath.getNameCount() - 1).toString();
        this.SHAONE = DigestUtils.sha1Hex(ParseSHA.parseSHA(this));
    }

    private void initialisePaths() {
        this.magitPath = Paths.get(this.currentPath + "//" + Settings.MAGIT_FOLDER);
        this.objectPath = Paths.get(magitPath + "//" + Settings.OBJECT_FOLDER);
        this.branchesPath = Paths.get(magitPath + "//" + Settings.BRANCHES_FOLDER);
    }

    private void createNewMagitFolder() throws RepositoryException {
        File magitFolder = new File(magitPath.toUri()),
                objectFolder = new File(objectPath.toUri()),
                branchesFolder = new File(branchesPath.toUri());
        if (magitFolder.exists() && objectFolder.exists() && branchesFolder.exists()) {
            throw new RepositoryException(eErrorCodes.MAGIT_FOLDER_ALREADY_EXIST);
        } else {
            try {
                FileUtils.forceMkdir(magitFolder);
                FileUtils.forceMkdir(objectFolder);
                FileUtils.forceMkdir(branchesFolder);
                File branchHEAD = new File(branchesPath.toString() + File.separator + Settings.MAGIT_BRANCH_HEAD),
                        branchMaster = new File(branchesPath + File.separator + Settings.MAGIT_BRANCH_MASTER);
                branchHEAD.createNewFile();
                branchMaster.createNewFile();
                FileWriter headCommit = new FileWriter(branchHEAD);
                headCommit.write(Settings.MAGIT_BRANCH_MASTER);
                headCommit.close();
                FileWriter masterCommit = new FileWriter(branchMaster);
                masterCommit.write(Settings.EMPTY_COMMIT);
                masterCommit.close();
            } catch (IOException e) {
                throw new RepositoryException(eErrorCodes.CREATE_MAGIT_FOLDER_FAILED);
            }
        }
    }

    public Path getMagitPath() {
        return magitPath;
    }

    public void setMagitPath(Path magitPath) {
        this.magitPath = magitPath;
    }

    public Path getObjectPath() {
        return objectPath;
    }

    public void setObjectPath(Path objectPath) {
        this.objectPath = objectPath;
    }

    public Path getBranchesPath() {
        return branchesPath;
    }

    public void setBranchesPath(Path branchesPath) {
        this.branchesPath = branchesPath;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public Commit getLastCommit() {
        return lastCommit;
    }

    public void setLastCommit(Commit lastCommit) {
        this.lastCommit = lastCommit;
    }

    public List<Branch> getBranches() {
        return branches;
    }

    public void setBranches(List<Branch> branches) {
        this.branches = branches;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSHAONE() {
        return SHAONE;
    }

    public void setSHAONE(String SHAONE) {
        this.SHAONE = SHAONE;
    }

    public Map<String, List<BasicFile>> scanRepository() throws IOException, MyFileException, RepositoryException {
        Map<String, List<BasicFile>> repository = new HashMap<>();
        repository.put(MapKeys.LIST_NEW, new ArrayList<>());
        repository.put(MapKeys.LIST_CHANGED, new ArrayList<>());
        repository.put(MapKeys.LIST_DELETED, new ArrayList<>());

        Folder rootFolder = new Folder(currentPath, Settings.USER_ADMINISTRATOR);
        rootFolder.setRootFolder(null);
        scanRecursiveFolder(rootFolder, rootFolder);

        scanBetweenMaps(rootFolder.getBlobMap().getFileBlobMap(), this.rootFolder.getBlobMap().getFileBlobMap(), repository);

        return repository;
    }

    private void scanBetweenMaps(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, Map<String, List<BasicFile>> repository)
            throws MyFileException, RepositoryException, IOException {
        List<BasicFile> deletedFiles, addedFiles, editedFiles;
        deletedFiles = repository.get(MapKeys.LIST_DELETED);
        addedFiles = repository.get(MapKeys.LIST_NEW);
        editedFiles = repository.get(MapKeys.LIST_CHANGED);

        if (oldFiles.size() == 0) {
            loadLastCommit();
        }

        for (Map.Entry<BasicFile, Blob> entry : newFiles.entrySet()) {
            if (!oldFiles.containsKey(entry.getKey())) {
                addFilesToList(addedFiles, entry.getValue());
            } else {
                Blob file = entry.getValue();
                if (!oldFiles.get(file).getSHA_ONE().equals(file.getSHA_ONE())) {
                    checkAndAddFilesToList(oldFiles, editedFiles, file);
                }
            }
        }

        for (Map.Entry<BasicFile, Blob> entry : oldFiles.entrySet()) {
            if (!newFiles.containsKey(entry.getKey())) {
                deletedFiles.add(entry.getValue());
            }
        }
    }

    public void loadLastCommit() throws MyFileException, RepositoryException, IOException {
        lastCommit = loadFromHEAD(branchesPath);
        if (lastCommit != null) {
            File lastWC = new File(objectPath + File.separator + lastCommit.getSHAONE());
            String wcSHA;
            try {
                BufferedReader br = new BufferedReader(new FileReader(lastWC));
                wcSHA = br.readLine();
            } catch (FileNotFoundException e) {
                throw new MyFileException(eErrorCodes.OPEN_FILE_FAILED, lastWC.toString());
            } catch (IOException e) {
                throw new MyFileException(eErrorCodes.READ_FROM_FILE_FAILED, lastWC.toString());
            }
            File mainFolder = FileManager.unZipFile(new File(objectPath + File.separator + wcSHA), objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
            rootFolder.setBlobMap(recoverOldRepository(Objects.requireNonNull(mainFolder), rootFolder));
        }
    }

    private Commit loadFromHEAD(Path branchesPath) {
        File head = new File(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD);
        if (head.exists()) {
            Commit commit;
            try {
                List<String> activeBranch = Files.readAllLines(Paths.get(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD));
                List<String> commitFile = Files.readAllLines(Paths.get(branchesPath + File.separator + activeBranch.get(0)));
                String commit_sha = commitFile.get(0);
                if (commit_sha != null)
                    commit = new Commit(commitFile.get(0), objectPath.toString());
                else
                    commit = null;
            } catch (IOException e) {
                commit = null;
            }
            return commit;
        }
        return null;
    }

    private BlobMap recoverOldRepository(File lastWC, Folder rootFolder) throws MyFileException, RepositoryException, IOException {
        if (!lastWC.exists()) {
            throw new MyFileException(eErrorCodes.FILE_NOT_EXIST, lastWC.toString());
        }

        File file = FileManager.unZipFile(lastWC, objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
        if (file == null) {
            throw new RepositoryException(eErrorCodes.ERROR_LOAD_REPOSITORY);
        } else {
            BlobMap files = new BlobMap(new TreeMap<>(Comparator.comparing(BasicFile::getFullPathName)));
            if (FilenameUtils.getExtension(file.toString()).equals(Settings.FOLDER_FILE_EXTENSION)) {
                List<String> folderContent;
                try {
                    folderContent = Files.readAllLines(file.toPath());
                } catch (IOException e) {
                    throw new MyFileException(eErrorCodes.READ_FROM_FILE_FAILED, file.toString());
                }
                for (String line : folderContent) {
                    String[] row = line.split(Settings.FOLDER_DELIMITER);
                    if (row.length != 0) {
                        if (row[2].equals(Settings.FILE_TYPE_IN_FOLDER_TABLE)) {
                            Blob temp = new Blob(rootFolder.getFilePath(), row[3]);
                            files.addToMap(temp, temp, eFileTypes.FILE);
                        } else {
                            Folder temp = new Folder(Paths.get(rootFolder.getFilePath() + File.separator + row[0]), row[3]);
                            files.addToMap(temp, temp, eFileTypes.FOLDER);
                            temp.setBlobMap(recoverOldRepository(Objects.requireNonNull(FileManager.unZipFile(new File(objectPath + File.separator + row[1]),
                                    objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER)), temp));
                        }
                    }
                }
            } else {
                Blob temp = new Blob(file.toPath(), rootFolder.getEditorName());
                files.addToMap(temp, temp, eFileTypes.FILE);
            }
            return files;
        }
    }

    private void checkAndAddFilesToList(Map<BasicFile, Blob> oldFiles, List<BasicFile> editedFiles, Blob file) {
        editedFiles.add(file);
        if (file.getType() == eFileTypes.FOLDER) {
            if (!oldFiles.get(file).getSHA_ONE().equals(file.getSHA_ONE())) {
                Map<BasicFile, Blob> files = ((Folder) file).getBlobMap().getFileBlobMap();
                for (Map.Entry<BasicFile, Blob> entry : files.entrySet()) {
                    if (!oldFiles.get(entry.getValue()).getSHA_ONE().equals(entry.getValue().getSHA_ONE())) {
                        checkAndAddFilesToList(oldFiles, editedFiles, entry.getValue());
                    }
                }
            }
        }
    }

    private void addFilesToList(List<BasicFile> addedFiles, BasicFile e) {
        addedFiles.add(e);
        if (e.getType() == eFileTypes.FOLDER) {
            Map<BasicFile, Blob> files = ((Folder) e).getBlobMap().getFileBlobMap();
            for (Map.Entry<BasicFile, Blob> entry : files.entrySet()) {
                addFilesToList(addedFiles, entry.getValue());
            }
        }
    }

    private void scanRecursiveFolder(Folder rootFolder, Blob blob) throws IOException {
        if (!(blob == rootFolder)) {
            rootFolder.AddBlob(blob);
        }
        if (blob.getType() == eFileTypes.FOLDER) {
            Folder temp_RootFolder = (Folder) blob;
            File folder = new File(blob.getFullPathName());
            File[] fileList = Objects.requireNonNull(folder.listFiles());
            for (File f : fileList) {
                if (!f.getName().equals(Settings.MAGIT_FOLDER)) {
                    if (f.isDirectory()) {
                        Folder temp = new Folder(f.toPath(), Settings.USER_ADMINISTRATOR);
                        temp.setRootFolder(temp_RootFolder);
                        scanRecursiveFolder(temp_RootFolder, temp);
                    } else {
                        Blob temp = new Blob(f.toPath(), Settings.USER_ADMINISTRATOR);
                        temp.setRootFolder(temp_RootFolder);
                        scanRecursiveFolder(temp_RootFolder, temp);
                    }
                }
            }
        }
    }

    public void updateRepository(Map<String, List<BasicFile>> files, String currentUser) throws IOException, MyFileException {
        boolean isSomeThingChanged = false;

        Map<BasicFile, Blob> tempMap, originalMap = rootFolder.getBlobMap().getFileBlobMap();
        List<BasicFile> fileList = files.get(MapKeys.LIST_DELETED);
        fileList.sort(Comparator.comparingInt(o -> o.getFullPathName().split("\"").length));
        Folder temp;

        for (BasicFile file : fileList) {
            temp = file.getRootFolder();
            tempMap = temp.getBlobMap().getFileBlobMap();
            tempMap.remove(file);
            isSomeThingChanged = true;
        }

        fileList = files.get(MapKeys.LIST_CHANGED);
        fileList.sort(Comparator.comparingInt(o -> o.getFullPathName().split("\"").length));

        for (BasicFile file : fileList) {
            temp = file.getRootFolder();
            tempMap = temp.getBlobMap().getFileBlobMap();
            tempMap.remove(file);
            putFile(tempMap, file, temp,currentUser);
            isSomeThingChanged = true;
        }

        fileList = files.get(MapKeys.LIST_NEW);
        fileList.sort(Comparator.comparingInt(o -> o.getFullPathName().split("\"").length));

        for (BasicFile file : fileList) {
            temp = file.getRootFolder();
            if (originalMap.containsKey(temp)) {
                temp = ((Folder) originalMap.get(temp));
                tempMap = temp.getBlobMap().getFileBlobMap();
            } else {
                tempMap = originalMap;
                temp = rootFolder;
            }
            putFile(tempMap, file, temp,currentUser);
            temp.calcFolderSHAONE();
            isSomeThingChanged = true;
        }

        if (isSomeThingChanged) {
            SHAONE = DigestUtils.sha1Hex(rootFolder.getBlobMap().toString());
            FileManager.zipFile(rootFolder, objectPath);
        }
    }

    private void putFile(Map<BasicFile, Blob> files, BasicFile file, Folder rootFolder, String currentUser) throws IOException {
        if (file.getType() == eFileTypes.FOLDER) {
            Folder temp = new Folder(((Blob) file).getFilePath(), currentUser);
            temp.setDate(new Date());
            temp.setRootFolder(rootFolder);
            files.put(temp, temp);
        } else {
            Blob temp = new Blob(((Blob) file).getFilePath(), currentUser);
            temp.setDate(new Date());
            temp.setRootFolder(rootFolder);
            files.put(temp, temp);
        }
    }

    public void loadBranches() throws IOException, RepositoryException {
        File[] branchesFiles = new File(branchesPath.toString()).listFiles();
        File headBranch = null;
        if (branchesFiles != null) {
            for (File file : branchesFiles) {
                if (!file.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                    List<String> commit = Files.readAllLines(file.toPath());
                    Branch branch = new Branch(file.getName(), new Commit(commit.get(0), objectPath.toString()), branchesPath.toString());
                    branches.add(branch);
                } else {
                    headBranch = file;
                }
            }
        }
        Branch activeBranch = getActiveBranch(headBranch);
        if (activeBranch == null)
            throw new RepositoryException(eErrorCodes.CANNOT_RECOVER_BRANCH);
        Branch head = new Branch(true, activeBranch);
        branches.add(head);
    }

    private Branch getActiveBranch(File headBranch) throws IOException {
        List<String> activeBranchName = Files.readAllLines(headBranch.toPath());
        String name = activeBranchName.get(0);
        for (Branch branch : branches) {
            if (branch.getName().equals(name))
                return branch;
        }
        return null;
    }

    public Branch getActiveBranch() {
        for (Branch branch : branches) {
            if (branch.getName().equals(Settings.MAGIT_BRANCH_HEAD))
                return branch.getActiveBranch();
        }
        return null;
    }
}