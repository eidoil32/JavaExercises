package com.magit.engine;

import com.magit.exceptions.RepositoryException;
import com.magit.exceptions.eErrorCodes;
import com.magit.settings.LangEN;
import com.magit.settings.MapKeys;
import com.magit.ui.Main;
import com.magit.utils.ParseSHA;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {
    private Path currentPath, magitPath, objectPath, branchesPath;
    private Folder rootFolder;
    private Commit lastCommit;
    private List<Branch> branches;
    private String name, SHAONE;
    private final String headFile = "/HEAD";

    public Repository(Path repositoryPath) throws RepositoryException, IOException { // set clean repository
        this.currentPath = repositoryPath;
        this.magitPath = Paths.get(repositoryPath.toString() + "//.magit");
        this.objectPath = Paths.get(magitPath.toString() + "//object");
        this.branchesPath = Paths.get(magitPath.toString() + "//branches");
        this.rootFolder = new Folder(repositoryPath, Main.engine.getSystem().getCurrentUser());
        this.lastCommit = null;
        this.branches = new LinkedList<>();
        this.branches.add(new Branch(LangEN.MAGIT_BRANCH_HEAD_NAME, null));
        this.name = repositoryPath.getName(repositoryPath.getNameCount() - 1).toString();
        createNewMagitFolder();
        this.SHAONE = DigestUtils.sha1Hex(ParseSHA.parseSHA(this));
    }

    private boolean createNewMagitFolder() throws RepositoryException {
        try {
            FileUtils.forceMkdir(new File(magitPath.toUri()));
            FileUtils.forceMkdir(new File(objectPath.toUri()));
            FileUtils.forceMkdir(new File(branchesPath.toUri()));
            File branchHEAD = new File(branchesPath.toString() + headFile);
            branchHEAD.createNewFile();
            FileWriter headCommit = new FileWriter(branchHEAD);
            headCommit.write(0);
            headCommit.close();
            return true;
        } catch (IOException e) {
            throw new RepositoryException(eErrorCodes.CREATE_MAGIT_FOLDER_FAILED);
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

    public String getHeadFile() {
        return headFile;
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

    public Map<String, List<BasicFile>> scanRepository() throws IOException {
        Map<String, List<BasicFile>> repository = new HashMap<>();
        repository.put(MapKeys.LIST_NEW, new ArrayList<>());
        repository.put(MapKeys.LIST_CHANGED, new ArrayList<>());
        repository.put(MapKeys.LIST_DELETED, new ArrayList<>());


        BlobMap newBlobMap = new BlobMap(new HashMap<>());
        Folder rootFolder = new Folder(currentPath, "NONE");
        scanRecursiveFolder(rootFolder, rootFolder);

        scanBetweenMaps(rootFolder.getBlobMap().getFileBlobMap(), this.rootFolder.getBlobMap().getFileBlobMap(), repository);

        return repository;
    }

    private void scanBetweenMaps(Map<BasicFile, Blob> newFiles, Map<BasicFile, Blob> oldFiles, Map<String, List<BasicFile>> repository) {
        List<BasicFile> deletedFiles, addedFiles, editedFiles;
        deletedFiles = repository.get(MapKeys.LIST_DELETED);
        addedFiles = repository.get(MapKeys.LIST_NEW);
        editedFiles = repository.get(MapKeys.LIST_CHANGED);

        for (Map.Entry<BasicFile, Blob> entry : newFiles.entrySet()) {
            if (!oldFiles.containsKey(entry.getKey())) {
                addedFiles.add(entry.getValue());
            } else {
                Blob file = entry.getValue();
                if (file.getType() == eFileTypes.FILE) {
                    if (!oldFiles.get(file).getSHA_ONE().equals(file.getSHA_ONE())) {
                        editedFiles.add(file);
                    }
                }
            }
        }

        for (Map.Entry<BasicFile, Blob> entry : oldFiles.entrySet()) {
            if (!newFiles.containsKey(entry.getKey())) {
                deletedFiles.add(entry.getValue());
            }
        }
    }

    private void scanRecursiveFolder(Folder rootFolder, Blob file) throws IOException {
        if (!(file == rootFolder)) {
            rootFolder.AddBlob(file);
        }
        if (file.getType() == eFileTypes.FOLDER) {
            File folder = new File(file.getFullPathName());
            File[] fileList = Objects.requireNonNull(folder.listFiles());
            for (File f : fileList) {
                if (!f.getName().equals(".magit"))
                    scanRecursiveFolder((Folder) file, new Blob(f.toPath(), "NONE"));
            }
        }
    }

    public void updateRepository(Map<String, List<BasicFile>> files) throws IOException {
        Map<BasicFile, Blob> currentFiles = rootFolder.getBlobMap().getFileBlobMap();
        List<BasicFile> fileList = files.get(MapKeys.LIST_DELETED);
        for (BasicFile file : fileList) {
            currentFiles.remove(file);
        }
        fileList = files.get(MapKeys.LIST_CHANGED);
        for (BasicFile file : fileList) {
            currentFiles.remove(file);
            putFile(currentFiles,file);
        }
        fileList = files.get(MapKeys.LIST_NEW);
        for (BasicFile file : fileList) {
            putFile(currentFiles,file);
        }
    }

    private void putFile(Map<BasicFile, Blob> files, BasicFile file) throws IOException {
        if (file.getType() == eFileTypes.FOLDER) {
            Folder temp = new Folder(((Blob)file).getFilePath(), Main.engine.getSystem().getCurrentUser());
            temp.setDate(new Date());
            files.put(temp, temp);
        } else {
            Blob temp = new Blob(((Blob)file).getFilePath(), Main.engine.getSystem().getCurrentUser());
            temp.setDate(new Date());
            files.put(temp, temp);
        }
    }
}





















