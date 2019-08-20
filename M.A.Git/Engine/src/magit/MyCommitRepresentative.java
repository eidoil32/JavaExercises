package magit;

import puk.team.course.magit.ancestor.finder.CommitRepresentative;
import settings.Settings;

public class MyCommitRepresentative  implements CommitRepresentative {
    private Commit commit;

    public MyCommitRepresentative(Commit commit) {
        super();
        this.commit = commit;
    }

    @Override
    public String getSha1() {
        return commit.getSHA_ONE();
    }

    @Override
    public String getFirstPrecedingSha1() {
        if(commit.getPrevCommit() == null) {
            return Settings.EMPTY_STRING;
        }
        return commit.getPrevCommitSHA_ONE();
    }

    @Override
    public String getSecondPrecedingSha1() {
        if(commit.getAnotherPrevCommit() == null) {
            return Settings.EMPTY_STRING;
        }
        return commit.getAnotherPrevCommitSHA_ONE();
    }
}
