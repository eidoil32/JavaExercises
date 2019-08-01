package magit;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Branch {
    private String name, SHA_ONE;
    private Commit commit;
    private Branch activeBranch;

    public Branch(String name) {
        this.name = name;
        this.commit = null;
        this.SHA_ONE = null;
        this.activeBranch = null;
    }

    public Branch(String name, Commit commit, String pathToBranchesFolder) throws IOException, RepositoryException {
        this.name = name;
        this.commit = commit;
        this.activeBranch = null;
        this.SHA_ONE = commit == null ? null : commit.getSHAONE();
        File file = new File(pathToBranchesFolder + "\\" + name);
        if (!file.createNewFile()) // branch already exists
        {
            throw new RepositoryException(eErrorCodes.BRANCH_ALREADY_EXIST);
        }
    }

    public Branch(boolean isHead, Branch activeBranch) {
        this.name = "HEAD";
        this.commit = null;
        this.SHA_ONE = activeBranch.getName();
        this.activeBranch = activeBranch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public void setSHA_ONE(String SHA_ONE) {
        this.SHA_ONE = SHA_ONE;
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit, String pathToBranches, String branchName) throws RepositoryException {
        this.commit = commit;
        if (commit != null) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(pathToBranches + File.separator + branchName);
            } catch (FileNotFoundException e) {
                throw new RepositoryException(eErrorCodes.OPEN_BRANCH_FILE_FAILED);
            }
            writer.print(commit.getSHAONE());
            writer.close();
        }
    }

    public Branch getActiveBranch() {
        return activeBranch;
    }
}
