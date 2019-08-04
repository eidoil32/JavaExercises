package magit;

import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import languages.LangEN;
import settings.Settings;
import xml.basic.MagitSingleBranch;

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
        this.SHA_ONE = commit == null ? null : commit.getSHA_ONE();
        File file = new File(pathToBranchesFolder + File.separator + name);
        if (!file.createNewFile()) // branch already exists
        {
            throw new RepositoryException(eErrorCodes.BRANCH_ALREADY_EXIST);
        }
    }

    public Branch(boolean isHead, Branch activeBranch) {
        if (isHead) {
            this.name = Settings.MAGIT_BRANCH_HEAD;
            this.commit = null;
            this.SHA_ONE = activeBranch.getName();
            this.activeBranch = activeBranch;
        }
    }

    public static Branch XML_Parser(MagitSingleBranch singleBranch, Commit commit, String pathToBranchesFolder) throws IOException, RepositoryException {
        Branch temp = new Branch(singleBranch.getName(), commit, pathToBranchesFolder);
        PrintWriter writer = new PrintWriter(new File(pathToBranchesFolder + File.separator + temp.getName()));
        writer.print(temp.getSHA_ONE());
        writer.close();
        return temp;
    }

    public void setCommit(Commit commit, String pathToBranches) throws RepositoryException {
        this.commit = commit;
        if (commit != null) {
            PrintWriter writer;
            try {
                writer = new PrintWriter(pathToBranches + File.separator + name);
            } catch (FileNotFoundException e) {
                throw new RepositoryException(eErrorCodes.OPEN_BRANCH_FILE_FAILED);
            }
            writer.print(commit.getSHA_ONE());
            writer.close();
            this.SHA_ONE = commit.getSHA_ONE();
        }
    }

    public String getCommitDataHistory() {
        Commit commit = this.commit;
        StringBuilder stringBuilder = new StringBuilder();
        while (commit != null) {
            stringBuilder.append(commit).append(System.lineSeparator());
            commit = commit.getPrevCommit();
        }

        return stringBuilder.toString();
    }

    public Branch getActiveBranch() {
        return activeBranch;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Branch) {
            return name.equals(((Branch) obj).getName());
        }
        return false;
    }

    @Override
    public String toString() {
        return LangEN.BRANCH_NAME + name + System.lineSeparator()
                + LangEN.BRANCH_LAST_COMMIT_SHA + commit.getSHA_ONE() + System.lineSeparator()
                + LangEN.BRANCH_LAST_COMMIT_COMMENT + commit.getComment() + System.lineSeparator();
    }

    public String getName() {
        return name;
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public Commit getCommit() {
        return commit;
    }
}
