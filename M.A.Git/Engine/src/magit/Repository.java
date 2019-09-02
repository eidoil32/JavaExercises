package magit;

import exceptions.*;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import settings.Settings;
import utils.FileManager;
import utils.MapKeys;
import xml.basic.MagitRepository;
import xml.basic.MagitSingleBranch;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {
    private Path currentPath, magitPath, objectPath, branchesPath, repoNamePath;
    private Folder rootFolder;
    private Commit lastCommit;
    private List<Branch> branches, remoteTrackingBranches, remoteBranches;
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

    public Repository(Path repoNamePath, String currentUser, String trackingName, boolean... values) throws IOException, RepositoryException {
        this(repoNamePath, values[0], currentUser, trackingName);

    }


    private Repository(Path repositoryPath, boolean isOld, String currentUser, String trackingName) throws IOException, RepositoryException {
        if (isOld) {
            this.currentPath = repositoryPath;
            initialisePaths();
            if (trackingName != null) {
                this.remoteTrackingBranches = loadTrackingBranches(branchesPath.toString());
                this.remoteBranches = loadBranchesWithoutHead(branchesPath + File.separator + trackingName, trackingName);
            } else {
                this.branches = loadBranches(branchesPath.toString());
            }
            this.rootFolder = new Folder(repositoryPath, currentUser);
            this.name = loadFromRepositoryFile();
            this.lastCommit = loadFromHEAD(branchesPath);
            this.SHA_ONE = lastCommit == null ? null : lastCommit.getSHA_ONE();
        }
    }

    private List<Branch> loadBranchesWithoutHead(String pathToFolder, String trackingName) throws RepositoryException, IOException {
        List<Branch> branches = new LinkedList<>();

        File folder = new File(pathToFolder);
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            assert files != null;
            for (File file : files) {
                branches.add(new Branch(file, objectPath.toString(), trackingName));
            }
        } else {
            throw new RepositoryException(eErrorCodes.BRANCH_FOLDER_WRONG);
        }

        return branches;
    }

    private List<Branch> loadTrackingBranches(String path) throws IOException, RepositoryException {
        List<Branch> branches = loadBranches(path);
        List<Branch> remoteTrackingBranches = new LinkedList<>();

        RemoteTrackingBranch head = null;

        for (Branch branch : branches) {
            RemoteTrackingBranch temp = new RemoteTrackingBranch(branch, branchesPath.toString(), new File(path).getName());
            remoteTrackingBranches.add(temp);
            if (temp.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                head = temp;
            }
        }

        for (Branch branch : remoteTrackingBranches) {
            if (branch.getName().equals(head.getSHA_ONE())) {
                head.setActive(branch);
            }
        }

        return remoteTrackingBranches;
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
            if (Files.list(target.toPath()).findFirst().isPresent())
                throw new MyXMLException(eErrorCodesXML.TARGET_FOLDER_NOT_EMPTY, xmlMagit.getLocation());
        }

        Repository repository = new Repository(xmlMagit.getLocation());
        repository.setName(xmlMagit.getName());
        boolean remoteFlag = false;
        String headName = xmlMagit.getMagitBranches().getHead();

        MagitRepository.MagitRemoteReference remoteReference = xmlMagit.getMagitRemoteReference();
        String remoteRepositoryName = null;
        if (remoteReference != null && remoteReference.getName() != null && remoteReference.getLocation() != null) {
            remoteFlag = true;
            remoteRepositoryName = remoteReference.getName();
            new File(repository.getBranchesPath() + File.separator + remoteRepositoryName).mkdir();
            Magit.createRemoteRepositoryFile(remoteReference.getLocation(), remoteReference.getName(), repository.getMagitPath().toString());
        }

        List<MagitSingleBranch> branches = xmlMagit.getMagitBranches().getMagitSingleBranch();
        Branch tempHead = null;

        for (MagitSingleBranch branch : branches) {
            String commitID = branch.getPointedCommit().getId();
            Branch temp = Branch.XML_Parser(branch, repository, xmlMagit, commitID);
            if (temp != null) {
                if (branch.getName().equals(headName)) {
                    if (remoteFlag) {
                        tempHead = new RemoteTrackingBranch(new Branch(true, temp), repository.getBranchesPath().toString(), remoteRepositoryName);
                        tempHead.setActive(temp);
                    } else {
                        tempHead = new Branch(true, temp);
                    }
                    repository.addBranch(tempHead);
                    repository.updateHeadFile(branch.getName());
                    repository.lastCommit = temp.getCommit();
                }
                repository.addBranch(temp);
            }
        }

        if (tempHead == null) {
            throw new MyXMLException(eErrorCodesXML.HEAD_POINT_TO_NONSEXIST_BRANCH, headName);
        }

        return repository;
    }

    public void updateHeadFile(String branchName) throws FileNotFoundException {
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

    public void scanForDeletedFiles(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, List<BasicFile> deletedList) {
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

    public Map<MapKeys, List<BasicFile>> createMapForScanning() {
        Map<MapKeys, List<BasicFile>> map = new HashMap<>();
        map.put(MapKeys.LIST_NEW, new ArrayList<>());
        map.put(MapKeys.LIST_CHANGED, new ArrayList<>());
        map.put(MapKeys.LIST_DELETED, new ArrayList<>());

        return map;
    }

    public void scanBetweenMaps(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, Map<MapKeys, List<BasicFile>> repository) throws RepositoryException {
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
                String commit_sha = commitFile.get(0);
                if (!commit_sha.equals(Settings.EMPTY_COMMIT))
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

    private synchronized BlobMap recoverOldRepository(File lastWC, Folder rootFolder) throws MyFileException, RepositoryException, IOException {
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
                file.delete();
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
        Map<MapKeys, List<BasicFile>> repository = createMapForScanning();

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
                br.close();
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

    private List<Branch> loadBranches(String basicPath) throws IOException, RepositoryException {
        List<Branch> branches = new LinkedList<>();
        File[] branchesFiles = new File(basicPath).listFiles();
        File headBranch = null;
        if (branchesFiles != null) {
            for (File file : branchesFiles) {
                if (!file.isDirectory()) {
                    if (!file.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
                        List<String> commit = Files.readAllLines(file.toPath());
                        Branch branch = new Branch(file.getName());
                        if (commit.size() != 0) {
                            if (!commit.get(0).equals(Settings.EMPTY_COMMIT)) {
                                branch.setCommit(new Commit(commit.get(0), objectPath.toString()), basicPath);
                            } else {
                                branch.setCommit(null, basicPath);
                            }
                        } else {
                            branch.setCommit(null, basicPath);
                        }
                        branches.add(branch);
                    } else {
                        headBranch = file;
                    }
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

    public Branch getActiveBranch() throws IOException {
        Branch temp = new Branch(Settings.MAGIT_BRANCH_HEAD);
        if (remoteTrackingBranches != null) {
            int index = remoteTrackingBranches.indexOf(new RemoteTrackingBranch(temp, branchesPath.toString(), null));
            if (index != -1)
                return remoteTrackingBranches.get(index).getActiveBranch();
        } else {
            int index = branches.indexOf(temp);
            if (index != -1)
                return branches.get(index).getActiveBranch();
        }
        return null;
    }

    public void addBranch(Branch branch) {
        if (branch instanceof RemoteTrackingBranch) {
            if (remoteTrackingBranches == null) {
                remoteTrackingBranches = new LinkedList<>();
            }
            remoteTrackingBranches.add(branch);
        } else {
            if (branch.isIsRemote()) {
                if (remoteBranches == null) {
                    remoteBranches = new LinkedList<>();
                }
                remoteBranches.add(branch);
            } else {
                branches.add(branch);
            }
        }
    }

    public Branch searchBranch(String branchName) {
        Branch temp = new Branch(branchName);

        List<Branch> branches = getActiveBranches();
        int index = branches.indexOf(temp);
        if (index != -1) {
            return branches.get(index);
        } else {
            return null;
        }
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

    public void setName(String name) throws IOException {
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

        for (Branch branch : getActiveBranches()) {
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

    public void updateRemoteTrackingBranchList(List<Branch> remoteTrackingBranches) {
        this.remoteTrackingBranches = remoteTrackingBranches;
    }

    public void updateBranchesList(List<Branch> branches) {
        this.branches = branches;
    }

    public void setRootFolder(Folder rootFolder) {
        this.rootFolder = rootFolder;
    }

    public List<Branch> getRemoteTrackingBranches() {
        return remoteTrackingBranches;
    }

    public List<Branch> getActiveBranches() {
        if (remoteTrackingBranches != null) {
            if (branches != null)
                return ListUtils.union(remoteTrackingBranches, branches);
            else
                return remoteTrackingBranches;
        }
        return branches;
    }

    public void createHeadFileAfterClone() throws FileNotFoundException {
        Branch head = findHead(remoteTrackingBranches);
        if (head != null) {
            File headFile = new File(branchesPath + File.separator + Settings.MAGIT_BRANCH_HEAD);
            PrintWriter writer = new PrintWriter(headFile);
            writer.write(head.getActiveBranch().getName());
            writer.close();
        }
    }

    private Branch findHead(List<Branch> branches) {
        for (Branch branch : branches) {
            if (branch.isHead()) {
                return branch;
            }
        }
        return null;
    }

    public void updateRemoteBranchesList(List<Branch> branches) {
        this.remoteBranches = branches;
    }

    public List<Branch> getRemoteBranches() {
        return remoteBranches;
    }

    public List<Branch> getAllBranches() {
        if (branches != null) {
            return branches;
        } else {
            return ListUtils.union(remoteBranches, remoteTrackingBranches);
        }
    }
}