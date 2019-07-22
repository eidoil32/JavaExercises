package com.magit.engine;

public class Branch {
    private String name, SHA_ONE;

    public Branch(String name, Commit commit) {
        this.name = name;
        this.SHA_ONE = commit == null ? null : commit.getSHAONE();
    }
}
