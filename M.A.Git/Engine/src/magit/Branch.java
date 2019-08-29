package magit;

import exceptions.MyFileException;
import exceptions.MyXMLException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import settings.Settings;
import xml.basic.MagitRepository;
import xml.basic.MagitSingleBranch;
import xml.basic.MagitSingleCommit;
import xml.basic.MagitSingleFolder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    protected String name, SHA_ONE;
    protected Commit commit;
    protected Branch activeBranch, remoteBranch;
    private boolean isRemote; // if true cannot work on this branch

    public Branch(String name) {
        if (name.contains(File.separator)) {
            this.name = new File(name).getName();
        } else {
            this.name = name;
        }
        this.commit = null;
        this.SHA_ONE = null;
        this.activeBranch = null;
    }

    public Branch(Branch branch) {
        this.name = branch.getName();
        this.commit = branch.getCommit();
        this.SHA_ONE = this.getSHA_ONE();
        this.activeBranch = this.getActiveBranch();
    }

    public Branch(String name, Commit commit, String pathToBranchesFolder) throws IOException, RepositoryException {
        this.name = name;
        this.commit = commit;
        this.activeBranch = null;
        this.SHA_ONE = commit == null ? null : commit.getSHA_ONE();
        File file = new File(pathToBranchesFolder + File.separator + name);
        file.createNewFile();
    }

    public Branch(boolean isHead, Branch activeBranch) {
        if (isHead) {
            this.name = Settings.MAGIT_BRANCH_HEAD;
            this.commit = null;
            this.SHA_ONE = activeBranch.getName();
            this.activeBranch = activeBranch;
        }
    }

    public Branch(File file, String pathToObject) throws IOException {
        this.name = file.getName();
        this.commit = new Commit(Files.readAllLines(file.toPath()).get(0), pathToObject);
        this.SHA_ONE = commit.getSHA_ONE();
        this.activeBranch = null;
        this.isRemote = true;
    }

    public static Branch XML_Parser(MagitSingleBranch singleBranch, Repository repository, MagitRepository xmlRepository, String commitID)
            throws IOException, RepositoryException, MyXMLException, MyFileException {
        String trackingName = xmlRepository.getMagitRemoteReference() != null ? xmlRepository.getMagitRemoteReference().getName() : null;

        if (commitID.equals(Settings.EMPTY_STRING)) {
            Branch remote = new Branch(singleBranch.getName(), null, repository.getBranchesPath().toString());
            if (singleBranch.isTracking()) {
                return new RemoteTrackingBranch(remote, repository.getBranchesPath().toString(), trackingName);
            } else {
                return remote;
            }
        }

        MagitSingleCommit pointedMagitCommit = Commit.XML_FindMagitCommit(xmlRepository.getMagitCommits().getMagitSingleCommit(), commitID);
        MagitSingleFolder pointedRootFolder = Folder.findRootFolder(xmlRepository.getMagitFolders().getMagitSingleFolder(), pointedMagitCommit.getRootFolder().getId());
        Folder rootFolder = Folder.XML_Parser(pointedRootFolder, xmlRepository, null, xmlRepository.getLocation());
        Commit pointedCommit = new Commit().XML_Parser(xmlRepository, pointedMagitCommit, rootFolder);

        if (singleBranch.getName().equals(xmlRepository.getMagitBranches().getHead())) {
            repository.setRootFolder(rootFolder);
        }

        Branch temp = new Branch(singleBranch.getName(), pointedCommit, repository.getBranchesPath().toString());
        PrintWriter writer = new PrintWriter(new File(repository.getBranchesPath() + File.separator + singleBranch.getName()));
        writer.write(pointedCommit.getSHA_ONE());
        if (singleBranch.isTracking()) {
            temp = new RemoteTrackingBranch(temp, repository.getBranchesPath().toString(), trackingName);
            writer.write(System.lineSeparator() + Settings.IS_TRACKING_REMOTE_BRANCH);
            PrintWriter remoteBranchWriter = new PrintWriter(new File(repository.getBranchesPath() + File.separator + singleBranch.getTrackingAfter()));
            remoteBranchWriter.write(pointedCommit.getSHA_ONE());
            remoteBranchWriter.close();
        }

        temp.setRemote(singleBranch.isIsRemote());

        writer.close();
        return temp;
    }

    public void setCommit(Commit commit, String pathToBranches) throws RepositoryException {
        this.commit = commit;
        if (commit != null) {
            File file = new File(pathToBranches + File.separator + name);
            List<String> content = new LinkedList<>();
            PrintWriter writer;
            try {
                content = Files.readAllLines(file.toPath());
                content.remove(0);
                content.add(0, commit.getSHA_ONE());
                writer = new PrintWriter(pathToBranches + File.separator + name);
                writer.write(content.get(0));
                if (content.size() == 2) {
                    writer.write(System.lineSeparator() + content.get(1));
                }
                writer.close();
                this.SHA_ONE = commit.getSHA_ONE();
            } catch (FileNotFoundException e) {
                throw new RepositoryException(eErrorCodes.OPEN_BRANCH_FILE_FAILED);
            } catch (IOException e) {
                throw new RepositoryException(eErrorCodes.OPEN_FILE_FAILED);
            }
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String consoleToString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Settings.language.getString("BRANCH_NAME_HINT")).append(name).append(System.lineSeparator());
        if (commit != null) {
            stringBuilder.append(Settings.language.getString("BRANCH_LAST_COMMIT_SHA")).append(commit.getSHA_ONE()).append(System.lineSeparator());
            stringBuilder.append(Settings.language.getString("BRANCH_LAST_COMMIT_COMMENT")).append(commit.getComment()).append(System.lineSeparator());
        } else {
            stringBuilder.append(Settings.language.getString("BRANCH_NONE_POINT_COMMIT")).append(System.lineSeparator());
        }
        return stringBuilder.toString();
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

    public List<Commit> getAllCommits() {
        if (commit != null) {
            return commit.getChainOfCommits();
        }
        return new LinkedList<>();
    }

    public boolean isHead() {
        return name.equals(Settings.MAGIT_BRANCH_HEAD);
    }

    public RemoteTrackingBranch createRemoteTrackingBranch(String pathToBranches, String remoteName) throws IOException {
        return new RemoteTrackingBranch(this, pathToBranches, remoteName);
    }

    public Branch makeRemote(String repositoryName, Path branchesPath) throws IOException {
        String newName = repositoryName + File.separator + this.name;
        File branchFile = new File(branchesPath + File.separator + newName);
        branchFile.createNewFile();
        PrintWriter writer = new PrintWriter(branchFile);
        writer.write(this.SHA_ONE);
        writer.close();

        Branch branch = new Branch(this);
        branch.setRemoteBranch(this);
        branch.setRemote(true);
        return branch;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    private void setRemoteBranch(Branch branch) {
        this.remoteBranch = branch;
    }

    public void setActive(Branch branch) {
        this.activeBranch = branch;
    }

    public Branch getRemoteBranch() {
        return remoteBranch;
    }

    public boolean isIsRemote() {
        return isRemote;
    }
}