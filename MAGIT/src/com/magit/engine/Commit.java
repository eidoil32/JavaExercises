package com.magit.engine;

import java.util.Date;

public class Commit {
    private String SHA_ONE, prevCommitSHA_ONE;
    private String comment, creator;
    private Date date;

    public String getSHAONE() {
        return SHA_ONE;
    }
}
