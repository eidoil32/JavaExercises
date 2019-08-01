package magit;

import exceptions.MyFileException;
import org.apache.commons.codec.digest.DigestUtils;
import utils.FileManager;
import utils.MapKeys;
import utils.Settings;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Commit {
    private String SHA_ONE, prevCommitSHA_ONE;
    private String comment, creator;
    private Date date;
    private Commit prevCommit;

    public Commit() {
        this.prevCommit = null;
    }

    public Commit(Commit commit) {
        this.prevCommit = commit;
        if (commit != null) {
            this.prevCommitSHA_ONE = commit.getSHAONE();
        }
    }

    public Commit(String SHA_ONE, String pathToObject) throws IOException {
        List<String> commitContent = Files.readAllLines(Paths.get(pathToObject + File.separator + SHA_ONE));
        this.SHA_ONE = SHA_ONE;
        this.prevCommitSHA_ONE = commitContent.get(1);
        this.comment = commitContent.get(2);
        this.creator = commitContent.get(4);
        String dateInString = new SimpleDateFormat(Settings.DATE_FORMAT).format(new Date());
        SimpleDateFormat formatter = new SimpleDateFormat(Settings.DATE_FORMAT);
        try {
            this.date = formatter.parse(dateInString);
        } catch (ParseException e) {
            this.date = new Date();
        }
        if (!this.prevCommitSHA_ONE.equals(Settings.EMPTY_COMMIT)) {
            this.prevCommit = new Commit(this.prevCommitSHA_ONE, pathToObject);
        }
    }

    public String getSHAONE() {
        return SHA_ONE;
    }

    public void createCommitFile(Repository currentRepository, Map<MapKeys, List<BasicFile>> listMap, String currentUser, String comment)
            throws IOException, MyFileException {
        StringBuilder stringBuilder = new StringBuilder();

        this.date = new Date();
        this.creator = currentUser;

        createNewAndEditedFiles(listMap, currentRepository, currentUser);
        currentRepository.setSHAONE(currentRepository.getRootFolder().getSHA_ONE());
        stringBuilder.append(currentRepository.getSHAONE()).append(System.lineSeparator()); // repository sha-1
        stringBuilder.append(prevCommitSHA_ONE).append(System.lineSeparator()); //last commit sha-1
        this.comment = comment;
        stringBuilder.append(this.comment).append(System.lineSeparator());
        SimpleDateFormat dateFormat = new SimpleDateFormat(Settings.DATE_FORMAT);
        stringBuilder.append(dateFormat.format(date)).append(System.lineSeparator());
        stringBuilder.append(this.creator).append(System.lineSeparator());
        File commit = new File(currentRepository.getObjectPath().toString() + File.separator + DigestUtils.sha1Hex(stringBuilder.toString()));
        commit.createNewFile();
        FileWriter fileWriter = new FileWriter(commit);
        fileWriter.write(stringBuilder.toString());
        this.SHA_ONE = DigestUtils.sha1Hex(stringBuilder.toString());
        fileWriter.close();
    }

    private void createNewAndEditedFiles(Map<MapKeys, List<BasicFile>> listMap, Repository currentRepository, String currentUser) throws MyFileException, IOException {
        currentRepository.updateRepository(listMap, currentRepository.getRootFolder(), currentUser);
        zipAllChainOfInherit(currentRepository.getRootFolder(), currentRepository);
    }

    private void zipAllChainOfInherit(BasicFile file, Repository currentRepository) throws MyFileException {
        FileManager.zipFile(file, currentRepository.getObjectPath());
        Folder f = file.tryParseFolder();
        if (f != null) {
            for (Map.Entry<BasicFile, Blob> entry : f.getBlobMap().getMap().entrySet()) {
                zipAllChainOfInherit(entry.getValue(),currentRepository);
            }
        }
    }

    public String showAllHistory() {
        if (this.prevCommit == null)
            return "";
        else {
            StringBuilder stringBuilder = new StringBuilder();
            return stringBuilder.toString();
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void loadDataFromFile(Path objectPath, String commit_sha) throws IOException, ParseException {
        String prevCommit_sha;
        List<String> commitFile = Files.readAllLines(Paths.get(objectPath + File.separator + commit_sha));
        prevCommit_sha = commitFile.get(1);
        if (!prevCommit_sha.equals(Settings.EMPTY_COMMIT)) {
            this.prevCommit = new Commit(commitFile.get(1), objectPath.toString());
            this.prevCommit.loadDataFromFile(objectPath, prevCommit_sha);
        } else {
            this.prevCommit = null;
            this.prevCommitSHA_ONE = Settings.EMPTY_COMMIT;
        }
        this.comment = commitFile.get(2);
        this.SHA_ONE = commitFile.get(0);
        DateFormat df = new SimpleDateFormat(Settings.DATE_FORMAT);
        this.date = df.parse(commitFile.get(3));
        this.creator = commitFile.get(4);
    }
}