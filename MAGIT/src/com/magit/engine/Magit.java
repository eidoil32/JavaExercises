package com.magit.engine;

import com.magit.exceptions.RepositoryException;
import com.magit.settings.LangEN;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;

public class Magit {
    private Path rootFolder;
    private Branch currentBranch;
    private String currentUser;
    private Repository currentRepository;

    public Magit()
    {
        currentUser = LangEN.USER_ADMINISTRATOR;
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

    public void setCurrentBranch(Branch currentBranch) {
        this.currentBranch = currentBranch;
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
        currentBranch = currentRepository.getBranches().get(0);
    }

    public void commitMagit() throws IOException, ParseException, RepositoryException {
        Commit newCommit = new Commit(currentBranch.getCommit());
        newCommit.createCommitFile(currentRepository,currentRepository.scanRepository());
        currentBranch.setCommit(newCommit);
    }
}
