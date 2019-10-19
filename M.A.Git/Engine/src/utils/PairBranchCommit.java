package utils;

import magit.Branch;
import magit.Commit;

import java.util.Objects;

public class PairBranchCommit {
    private Commit commit;
    private Branch branch;

    public PairBranchCommit(Commit commit, Branch branch) {
        this.commit = commit;
        this.branch = branch;
    }

    public Commit getCommit() {
        return commit;
    }

    public Branch getBranch() {
        return branch;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(branch.getName()).append("] ");
        stringBuilder.append(commit.getSHA_ONE(), 0, 6).append(" ");
        String comment = commit.getComment();
        stringBuilder.append(comment, 0, Math.min(comment.length(), 10)).append(" - ");
        stringBuilder.append(commit.getCreator());

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairBranchCommit that = (PairBranchCommit) o;
        return Objects.equals(commit, that.commit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commit);
    }
}
