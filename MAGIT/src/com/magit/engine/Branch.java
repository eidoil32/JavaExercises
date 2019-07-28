package com.magit.engine;

public class Branch {
    private String name, SHA_ONE;
    private Commit commit;

    public Branch(String name, Commit commit) {
        this.name = name;
        this.commit = commit;
        this.SHA_ONE = commit == null ? null : commit.getSHAONE();
    }

    public Commit getCommit() {
        return commit;
    }

    public void setCommit(Commit commit) {
        this.commit = commit;
    }
}
