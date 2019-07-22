package com.magit.engine;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Repository {
    private Path currentPath, magitPath, objectPath, branchesPath;
    private Map<BasicFile,Blob> blobs;
    private Commit lastCommit;
    private List<Branch> branches;

    public Repository(Path repositoryPath) { // set clean repository
        this.currentPath = repositoryPath;
        this.magitPath = Paths.get(repositoryPath.toString() + "//.magit");
        this.objectPath = Paths.get(magitPath.toString() + "//object");
        this.branchesPath = Paths.get(magitPath.toString() + "//branches");
        this.blobs = new TreeMap<>(Comparator.comparing(BasicFile::getName));
        this.lastCommit = null;
        this.branches = new LinkedList<>();
        this.branches.add(new Branch("HEAD",null));
        createNewMagitFolder();
    }

    private void createNewMagitFolder() {
        try {
            FileUtils.forceMkdir(new File(magitPath.toUri()));
            FileUtils.forceMkdir(new File(objectPath.toUri()));
            FileUtils.forceMkdir(new File(branchesPath.toUri()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath = currentPath;
    }

    public Map<BasicFile, Blob> getBlobs() {
        return blobs;
    }

    public void setBlobs(Map<BasicFile, Blob> blobs) {
        this.blobs = blobs;
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
}
