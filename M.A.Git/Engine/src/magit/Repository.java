package magit;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import utils.FileManager;
import utils.MapKeys;
import utils.Settings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Repository {
    private Path currentPath, magitPath, objectPath, branchesPath, repoNamePath;
    private Folder rootFolder;
    private Commit lastCommit;
    private List<Branch> branches;
    private String name, SHA_ONE;

    public Repository(Path repositoryPath, String currentUser, String name) throws RepositoryException, IOException { // set clean repository
        this.currentPath = repositoryPath;
        initialisePaths();
        createNewMagitFolder();
        createRepositoryFile(name);
        this.rootFolder = new Folder(repositoryPath, currentUser);
        this.lastCommit = null;
        this.branches = new LinkedList<>();
        Branch master = new Branch(Settings.MAGIT_BRANCH_MASTER);
        Branch head = new Branch(true, master);
        this.branches.add(master);
        this.branches.add(head);
        this.name = name;
        this.SHA_ONE = null;
    }

    public Repository(Path repositoryPath, boolean isOld, String currentUser) throws IOException, RepositoryException {
        if (isOld) {
            this.currentPath = repositoryPath;
            initialisePaths();
            this.branches = loadBranches();
            this.rootFolder = new Folder(repositoryPath, currentUser);
            this.name = loadFromRepositoryFile();
            this.SHA_ONE = lastCommit == null ? null : lastCommit.getSHAONE();
        }
    }

    private void scanRecursiveFolder(Folder rootFolder, Blob blob, String currentUser) throws IOException {
        if (!(blob == rootFolder)) {
            rootFolder.AddBlob(blob);
        }
        Folder temp_RootFolder = blob.tryParseFolder();
        if (temp_RootFolder != null) {
            File folder = new File(blob.getFullPathName());
            File[] fileList = Objects.requireNonNull(folder.listFiles());
            for (File f : fileList) {
                Blob temp;
                if (!f.getName().equals(Settings.MAGIT_FOLDER)) {
                    if (f.isDirectory()) {
                        temp = new Folder(f.toPath(), currentUser);
                    } else {
                        temp = new Blob(f.toPath(), currentUser);
                    }
                    temp.setRootFolder(temp_RootFolder);
                    scanRecursiveFolder(temp_RootFolder, temp, currentUser);
                }
            }
        }
    }

    private void createRepositoryFile(String name) throws IOException {
        File createRepositoryName = new File(repoNamePath.toString());
        createRepositoryName.createNewFile();
        PrintWriter writer = new PrintWriter(createRepositoryName);
        writer.print(name);
        writer.close();
    }

    private String loadFromRepositoryFile() throws IOException {
        return Files.readAllLines(repoNamePath).get(0);
    }

    private void initialisePaths() {
        this.magitPath = Paths.get(this.currentPath + "//" + Settings.MAGIT_FOLDER);
        this.objectPath = Paths.get(magitPath + "//" + Settings.OBJECT_FOLDER);
        this.branchesPath = Paths.get(magitPath + "//" + Settings.BRANCHES_FOLDER);
        this.repoNamePath = Paths.get(magitPath + File.separator + Settings.REPOSITORY_NAME);
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

    private void scanForDeletedFiles(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, List<BasicFile> deletedList) {
        for (Map.Entry<BasicFile, Blob> entry : oldFiles.entrySet()) {
            Blob blob = entry.getValue();
            if (blob.getType() == eFileTypes.FILE) {
                if (!newFiles.containsKey(blob)) {
                    deletedList.add(blob);
                }
            } else {
                Folder folder = (Folder) blob;
                if (!newFiles.containsKey(folder)) {
                    deletedList.add(folder);
                } else {
                    Folder newFolder = (Folder) newFiles.get(folder);
                    scanForDeletedFiles(newFolder.getBlobMap().getMap(), folder.getBlobMap().getMap(), deletedList);
                }
            }
        }
    }

    private void scanBetweenMaps(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, Map<MapKeys, List<BasicFile>> repository) throws RepositoryException {
        List<BasicFile> addedFiles, editedFiles;
        addedFiles = repository.get(MapKeys.LIST_NEW);
        editedFiles = repository.get(MapKeys.LIST_CHANGED);

        for (Map.Entry<BasicFile, Blob> entry : newFiles.entrySet()) {
            Blob blob = entry.getValue();
            if (blob.getType() == eFileTypes.FILE) {
                if (oldFiles.containsKey(blob)) {
                    Blob originalBlob = oldFiles.get(blob);
                    if (!originalBlob.getSHA_ONE().equals(blob.getSHA_ONE())) {
                        editedFiles.add(blob);
                    }
                } else {
                    addedFiles.add(blob);
                }
            } else {
                Folder folder = blob.tryParseFolder();
                if (folder != null) {
                    if (!oldFiles.containsKey(folder)) {
                        addedFiles.add(folder);
                        addAllFolderFiles(addedFiles, folder.getBlobMap().getMap());
                    } else {
                        Folder originalFolder = (Folder) oldFiles.get(folder);
                        scanBetweenMaps(folder.getBlobMap().getMap(), originalFolder.getBlobMap().getMap(), repository);
                    }
                } else
                    throw new RepositoryException(eErrorCodes.PARSE_BLOB_TO_FOLDER_FAILED);
            }
        }
    }

    private void addAllFolderFiles(List<BasicFile> fileList, Map<BasicFile, Blob> map) {
        for (Map.Entry<BasicFile, Blob> entry : map.entrySet()) {
            Blob blob = entry.getValue();
            fileList.add(blob);
            if (blob.getType() == eFileTypes.FOLDER)
                addAllFolderFiles(fileList, ((Folder) blob).getBlobMap().getMap());
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
            BlobMap files = new BlobMap(new HashMap<>());
            if (FilenameUtils.getExtension(file.toString()).equals(Settings.FOLDER_FILE_EXTENSION)) {
                List<String> folderContent;
                try {
                    folderContent = Files.readAllLines(file.toPath());
                } catch (IOException e) {
                    throw new MyFileException(eErrorCodes.READ_FROM_FILE_FAILED, file.getPath());
                }
                if (!file.delete()) {
                    throw new MyFileException(eErrorCodes.DELETE_FILE_FAILED, file.getPath());
                }
                for (String line : folderContent) {
                    String[] row = line.split(Settings.FOLDER_DELIMITER);
                    if (row.length > 1) {
                        if (row[2].equals(Settings.FILE_TYPE_IN_FOLDER_TABLE)) {
                            Blob temp = recoverFromSHA(objectPath, row, rootFolder);
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
            File tempFolder = new File(objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
            tempFolder.delete();
            return files;
        }
    }

    private Blob recoverFromSHA(Path objectPath, String[] row, Folder rootFolder) throws IOException, MyFileException {
        Blob blob = new Blob();
        File file = FileManager.unZipFile(new File(objectPath + File.separator + row[1]), objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
        blob.setRootFolder(rootFolder);
        blob.setEditorName(row[3]);
        String date = row[4];
        SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT);
        try {
            blob.setDate(dateFormat.parse(date));
        } catch (ParseException e) {
            blob.setDate(new Date());
        }

        blob.setContent(Files.readAllLines(Objects.requireNonNull(file).toPath()));
        blob.setSHA_ONE(row[1]);
        blob.setFilePath(Paths.get(rootFolder.getFilePath() + File.separator + row[0]));
        blob.setName(row[0]);
        blob.setType(eFileTypes.FILE);
        if (!file.delete()) {
            throw new MyFileException(eErrorCodes.DELETE_FILE_FAILED, file.getPath());
        }
        return blob;
    }

    public Map<MapKeys, List<BasicFile>> scanRepository(String currentUser) throws IOException, MyFileException, RepositoryException {
        Map<MapKeys, List<BasicFile>> repository = new HashMap<>();
        repository.put(MapKeys.LIST_NEW, new ArrayList<>());
        repository.put(MapKeys.LIST_CHANGED, new ArrayList<>());
        repository.put(MapKeys.LIST_DELETED, new ArrayList<>());

        Folder rootFolder = new Folder(currentPath, currentUser);
        rootFolder.setRootFolder(null);

        scanRecursiveFolder(rootFolder, rootFolder, currentUser);

        if (this.rootFolder.getBlobMap().getMap().size() == 0) {
            this.rootFolder.setBlobMap(loadDataFromCommit(loadFromHEAD(branchesPath)));
        }

        scanBetweenMaps(rootFolder.getBlobMap().getMap(), this.rootFolder.getBlobMap().getMap(), repository);
        scanForDeletedFiles(rootFolder.getBlobMap().getMap(), this.rootFolder.getBlobMap().getMap(), repository.get(MapKeys.LIST_DELETED));

        return repository;
    }

    public BlobMap loadDataFromCommit(Commit commit) throws MyFileException, RepositoryException, IOException {
        lastCommit = commit;
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
            return recoverOldRepository(Objects.requireNonNull(mainFolder), rootFolder);
        }
        return new BlobMap(new HashMap<>());
    }

    public void updateRepository(Map<MapKeys, List<BasicFile>> files, Folder rootFolder) {
        for (Map.Entry<MapKeys, List<BasicFile>> listEntry : files.entrySet()) {
            for (BasicFile file : listEntry.getValue()) {
                listEntry.getKey().execute(rootFolder.getBlobMap(), file, rootFolder);
            }
        }
    }

    public List<Branch> loadBranches() throws IOException, RepositoryException {
        List<Branch> branches = new LinkedList<>();
        File[] branchesFiles = new File(branchesPath.toString()).listFiles();
        File headBranch = null;
        if (branchesFiles != null) {
            for (File file : branchesFiles) {
                if (!file.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                    List<String> commit = Files.readAllLines(file.toPath());
                    Branch branch = new Branch(file.getName());
                    branch.setCommit(new Commit(commit.get(0), objectPath.toString()), branchesPath.toString());
                    branches.add(branch);
                } else {
                    headBranch = file;
                }
            }
        }
        Branch activeBranch = getActiveBranch(Objects.requireNonNull(headBranch), branches);
        if (activeBranch == null)
            throw new RepositoryException(eErrorCodes.CANNOT_RECOVER_BRANCH);
        Branch head = new Branch(true, activeBranch);
        branches.add(head);
        return branches;
    }

    private Branch getActiveBranch(File headBranch, List<Branch> branches) throws IOException {
        List<String> activeBranchName = Files.readAllLines(headBranch.toPath());
        String name = activeBranchName.get(0);
        Branch temp = new Branch(name);
        int index = branches.indexOf(temp);
        if (index != -1)
            return branches.get(index);
        return null;
    }

    public Branch getActiveBranch() {
        Branch temp = new Branch(Settings.MAGIT_BRANCH_HEAD);
        int index = branches.indexOf(temp);
        if (index != -1)
            return branches.get(index).getActiveBranch();
        return null;
    }

    public void addBranch(Branch branch) {
        branches.add(branch);
    }

    public Branch searchBranch(String branchName) {
        Branch temp = new Branch(branchName);
        int index = branches.indexOf(temp);
        return (index != -1) ? branches.get(index) : null;
    }

    public void calcSHA_ONE() {
        rootFolder.calcFolderSHAONE();
    }

    public Path getMagitPath() {
        return magitPath;
    }

    public Path getObjectPath() {
        return objectPath;
    }

    public Path getBranchesPath() {
        return branchesPath;
    }

    public Path getCurrentPath() {
        return currentPath;
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

    public String getName() {
        return name;
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public void setSHA_ONE(String SHA_ONE) {
        this.SHA_ONE = SHA_ONE;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }
}