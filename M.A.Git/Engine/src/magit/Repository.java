package magit;

import exceptions.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import settings.Settings;
import utils.FileManager;
import utils.MapKeys;
import xml.basic.MagitRepository;
import xml.basic.MagitSingleBranch;
import xml.basic.MagitSingleCommit;
import xml.basic.MagitSingleFolder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            this.lastCommit = loadFromHEAD(branchesPath);
            this.SHA_ONE = lastCommit == null ? null : lastCommit.getSHA_ONE();
        }
    }

    private Repository(String repositoryPath) throws IOException {
        this.currentPath = Paths.get(repositoryPath);
        initialisePaths();
        createRepositoryFolders();
        this.branches = new LinkedList<>();
    }

    private void createRepositoryFolders() throws IOException {
        new File(magitPath.toString()).mkdirs();
        new File(objectPath.toString()).mkdirs();
        new File(branchesPath.toString()).mkdirs();
        new File(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD).createNewFile();
        new File(repoNamePath.toString()).createNewFile();
    }

    public static Repository XML_RepositoryFactory(MagitRepository xmlMagit)
            throws IOException, MyXMLException, RepositoryException, MyFileException {
        if (new File(xmlMagit.getLocation() + File.separator + Settings.MAGIT_FOLDER).exists()) {
            throw new MyXMLException(eErrorCodesXML.ALREADY_EXIST_FOLDER, xmlMagit.getLocation());
        } else if (new File(xmlMagit.getLocation()).exists()) {
            File target = new File(xmlMagit.getLocation());
            if (target.listFiles() != null && target.listFiles().length > 0)
                throw new MyXMLException(eErrorCodesXML.TARGET_FOLDER_NOT_EMPTY, xmlMagit.getLocation());
        }
        boolean hasHead = false;
        Repository repository = new Repository(xmlMagit.getLocation());
        repository.setName(xmlMagit.getName());
        Commit tempInstance = new Commit();
        String headName = xmlMagit.getMagitBranches().getHead();

        List<MagitSingleFolder> folders = xmlMagit.getMagitFolders().getMagitSingleFolder();
        List<MagitSingleBranch> branches = xmlMagit.getMagitBranches().getMagitSingleBranch();

        for (MagitSingleBranch branch : branches) {
            String commitID = branch.getPointedCommit().getId();
            Branch temp;
            Folder rootFolder;
            if (commitID.equals(Settings.EMPTY_STRING)) {
                temp = new Branch(branch.getName(), null, repository.branchesPath.toString());
                rootFolder = new Folder(Paths.get(xmlMagit.getLocation()), Settings.language.getString("USER_ADMINISTRATOR"));
            } else {
                MagitSingleCommit pointedMagitCommit = Commit.XML_FindMagitCommit(xmlMagit.getMagitCommits().getMagitSingleCommit(), commitID);
                MagitSingleFolder pointedRootFolder = Folder.findRootFolder(folders, pointedMagitCommit.getRootFolder().getId());
                rootFolder = Folder.XML_Parser(pointedRootFolder, xmlMagit, null, xmlMagit.getLocation());
                Commit pointedCommit = tempInstance.XML_Parser(xmlMagit, pointedMagitCommit, rootFolder);
                temp = Branch.XML_Parser(branch, pointedCommit, repository.getBranchesPath().toString());
            }
            repository.addBranch(temp);
            if (branch.getName().equals(headName)) {
                repository.addBranch(new Branch(true, temp));
                repository.updateHeadFile(headName);
                repository.lastCommit = temp.getCommit();
                repository.rootFolder = rootFolder;
                hasHead = true;
            }
        }

        if (!hasHead) {
            throw new MyXMLException(eErrorCodesXML.HEAD_POINT_TO_NONSEXIST_BRANCH, headName);
        }

        return repository;
    }

    private void updateHeadFile(String branchName) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(new File(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD));
        writer.print(branchName);
        writer.close();
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
        this.magitPath = Paths.get(this.currentPath + File.separator + Settings.MAGIT_FOLDER);
        this.objectPath = Paths.get(magitPath + File.separator + Settings.OBJECT_FOLDER);
        this.branchesPath = Paths.get(magitPath + File.separator + Settings.BRANCHES_FOLDER);
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
                    addAllSubFilesToDeleteList(folder, deletedList);
                    deletedList.add(blob);
                } else {
                    Folder newFolder = (Folder) newFiles.get(folder);
                    scanForDeletedFiles(newFolder.getBlobMap().getMap(), folder.getBlobMap().getMap(), deletedList);
                }
            }
        }
    }

    private void addAllSubFilesToDeleteList(Folder folder, List<BasicFile> deletedList) {
        for (Map.Entry<BasicFile, Blob> entry : folder.getBlobMap().getMap().entrySet()) {
            deletedList.add(entry.getValue());
            if (entry.getValue().getType() == eFileTypes.FOLDER) {
                addAllSubFilesToDeleteList((Folder) entry.getValue(), deletedList);
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
            Commit commit = null;
            try {
                List<String> activeBranch = Files.readAllLines(Paths.get(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD));
                List<String> commitFile = Files.readAllLines(Paths.get(branchesPath + File.separator + activeBranch.get(0)));
                if (commitFile.size() == 1) {
                    String commit_sha = commitFile.get(0);
                    if (!commit_sha.equals(Settings.EMPTY_COMMIT))
                        commit = new Commit(commitFile.get(0), objectPath.toString());
                    else
                        commit = null;
                }
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
                            Blob temp = Blob.BlobFactory(objectPath, row, rootFolder);
                            files.addToMap(temp);
                        } else {
                            Folder temp = new Folder();
                            temp.completeMissingData(row, rootFolder.getFullPathName(), rootFolder);
                            files.addToMap(temp);
                            temp.setBlobMap(recoverOldRepository(Objects.requireNonNull(FileManager.unZipFile(new File(objectPath + File.separator + row[1]),
                                    objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER)), temp));
                        }
                    }
                }
            } else {
                Blob temp = new Blob(file.toPath(), rootFolder.getEditorName());
                files.addToMap(temp);
            }
            File tempFolder = new File(objectPath + File.separator + Settings.TEMP_UNZIP_FOLDER);
            tempFolder.delete();
            return files;
        }
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

        if (noChanged(repository)) {
            return null;
        }
        return repository;
    }

    private boolean noChanged(Map<MapKeys, List<BasicFile>> repository) {
        for (Map.Entry<MapKeys, List<BasicFile>> entry : repository.entrySet()) {
            if (entry.getValue().size() > 0)
                return false;
        }
        return true;
    }

    public BlobMap loadDataFromCommit(Commit commit) throws MyFileException, RepositoryException, IOException {
        lastCommit = commit;
        if (lastCommit != null) {
            File lastWC = new File(objectPath + File.separator + lastCommit.getSHA_ONE());
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

    private List<Branch> loadBranches() throws IOException, RepositoryException {
        List<Branch> branches = new LinkedList<>();
        File[] branchesFiles = new File(branchesPath.toString()).listFiles();
        File headBranch = null;
        if (branchesFiles != null) {
            for (File file : branchesFiles) {
                if (!file.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                    List<String> commit = Files.readAllLines(file.toPath());
                    Branch branch = new Branch(file.getName());
                    if (commit.size() != 0) {
                        if (!commit.get(0).equals(Settings.EMPTY_COMMIT)) {
                            branch.setCommit(new Commit(commit.get(0), objectPath.toString()), branchesPath.toString());
                        } else {
                            branch.setCommit(null, branchesPath.toString());
                        }
                    } else {
                        branch.setCommit(null,branchesPath.toString());
                    }
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

    public void setSHA_ONE(String SHA_ONE) {
        this.SHA_ONE = SHA_ONE;
    }

    public Folder getRootFolder() {
        return rootFolder;
    }

    private void setName(String name) throws IOException {
        File repoName = new File(repoNamePath.toString());
        PrintWriter writer;
        try {
            writer = new PrintWriter(repoName);
        } catch (FileNotFoundException e) {
            repoName.createNewFile();
            writer = new PrintWriter(repoName);
        }
        writer.print(name);
        writer.close();
        this.name = name;
    }

    public Map<String, Object> getAllCommits() {
        Map<String, Object> objectMap = new HashMap<>();
        List<Commit> commitList = new LinkedList<>();
        Map<Commit, Branch> headCommits = new HashMap<>();

        for (Branch branch : this.branches) {
            if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                if (branch.getCommit() != null) {
                    headCommits.put(branch.getCommit(), branch);
                    commitList.addAll(branch.getAllCommits());
                }
            }
        }

        objectMap.put(Settings.KEY_COMMIT_BRANCH_LIST, headCommits);
        objectMap.put(Settings.KEY_COMMIT_LIST, commitList);

        return objectMap;
    }

    public void updateRootFolderFiles(Map<String, BlobMap> changes, BlobMap userApprove) {
    }
}