package utils;

import magit.Branch;
import magit.Commit;

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
}
