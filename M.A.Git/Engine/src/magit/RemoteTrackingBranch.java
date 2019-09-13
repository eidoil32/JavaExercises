package magit;

import settings.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.List;

public class RemoteTrackingBranch extends Branch {
    public RemoteTrackingBranch(Branch branch, String pathToBranches, String newName) throws IOException {
        super(branch.getName());
        this.commit = branch.getCommit();
        this.SHA_ONE = branch.getSHA_ONE();
        this.remoteBranch = branch;

        if (!name.equals(Settings.MAGIT_BRANCH_HEAD)) {
            File file = new File(pathToBranches + File.separator + name);
            if (file.exists()) {
                List<String> content = Files.readAllLines(file.toPath());
                if (content.size() == 1) {
                    WriteToBranchFile(branch, file);
                }
            } else {
                WriteToBranchFile(branch, file);
            }
        }
    }

    private void WriteToBranchFile(Branch branch, File file) throws FileNotFoundException {
        PrintWriter branchFile = new PrintWriter(file);
        branchFile.write(branch.getSHA_ONE() != null ? branch.getSHA_ONE() : Settings.EMPTY_COMMIT);
        branchFile.write(System.lineSeparator() + Settings.IS_TRACKING_REMOTE_BRANCH);
        branchFile.close();
    }
}
