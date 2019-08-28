package magit;

import settings.Settings;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public class RemoteTrackingBranch extends Branch {
    public RemoteTrackingBranch(Branch branch, String pathToBranches, String remoteRepositoryName) throws IOException {
        super(branch.getName());
        this.commit = branch.getCommit();
        this.SHA_ONE = branch.getSHA_ONE();
        this.remoteBranch = branch;

        if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD)) {
            File file = new File(pathToBranches + File.separator + branch.getName());
            List<String> content = Files.readAllLines(file.toPath());
            if (content.size() == 1) {
                PrintWriter branchFile = new PrintWriter(file);
                branchFile.write(branch.getSHA_ONE());
                branchFile.write(System.lineSeparator() + Settings.IS_TRACKING_REMOTE_BRANCH);
                branchFile.close();
            }
        }
    }
}
