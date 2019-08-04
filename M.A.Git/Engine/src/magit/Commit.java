package magit;

import exceptions.*;
import languages.LangEN;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.FileManager;
import utils.MapKeys;
import xml.basic.MagitSingleCommit;

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
    private String SHA_ONE, prevCommitSHA_ONE, rootFolderSHA_ONE;
    private String comment, creator;
    private Date date;
    private Commit prevCommit;

    public Commit() {
        this.prevCommit = null;
    }

    public Commit(Commit commit) {
        this.prevCommit = commit;
        if (commit != null) {
            this.prevCommitSHA_ONE = commit.getSHA_ONE();
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

    public static Commit XML_Parser(MagitSingleCommit commit, Folder rootFolder) throws MyXMLException {
        Commit temp = new Commit();
        temp.creator = commit.getAuthor();
        try {
            temp.date = new SimpleDateFormat(Settings.DATE_FORMAT).parse(commit.getDateOfCreation());
        } catch (ParseException e) {
            throw new MyXMLException(eErrorCodesXML.WRONG_DATE_FORMAT,commit.getDateOfCreation());
        }
        temp.comment = commit.getMessage();
        temp.rootFolderSHA_ONE = rootFolder.getSHA_ONE();
        temp.SHA_ONE =
        return temp;
    }


    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public void createCommitFile(Repository currentRepository, Map<MapKeys, List<BasicFile>> listMap, String currentUser, String comment)
            throws IOException, MyFileException, RepositoryException {
        StringBuilder stringBuilder = new StringBuilder();

        this.date = new Date();
        this.creator = currentUser;
        this.comment = comment;
        createNewAndEditedFiles(listMap, currentRepository);
        this.rootFolderSHA_ONE = currentRepository.getRootFolder().getSHA_ONE();
        currentRepository.setSHA_ONE(this.rootFolderSHA_ONE);

        if(listMap == null)
            throw new RepositoryException(eErrorCodes.NOTHING_NEW);


        stringBuilder.append(this.rootFolderSHA_ONE).append(System.lineSeparator()); // repository sha-1
        stringBuilder.append(this.prevCommitSHA_ONE).append(System.lineSeparator()); //last commit sha-1
        stringBuilder.append(this.comment).append(System.lineSeparator());
        stringBuilder.append(new SimpleDateFormat(Settings.DATE_FORMAT).format(this.date)).append(System.lineSeparator());
        stringBuilder.append(this.creator).append(System.lineSeparator());

        this.SHA_ONE = DigestUtils.sha1Hex(stringBuilder.toString());

        File commit = new File(currentRepository.getObjectPath().toString() + File.separator + this.SHA_ONE));
        commit.createNewFile();
        FileWriter fileWriter = new FileWriter(commit);
        fileWriter.write(stringBuilder.toString());

        fileWriter.close();
    }

    private void createNewAndEditedFiles(Map<MapKeys, List<BasicFile>> listMap, Repository currentRepository) throws MyFileException {
        currentRepository.updateRepository(listMap, currentRepository.getRootFolder());
        currentRepository.calcSHA_ONE();
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

    public String getRootFolderSHA_ONE() {
        return rootFolderSHA_ONE;
    }

    public void setRootFolderSHA_ONE(String rootFolderSHA_ONE) {
        this.rootFolderSHA_ONE = rootFolderSHA_ONE;
    }

    public void loadDataFromFile(Path objectPath, String commit_sha) throws IOException, RepositoryException {
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
        //this.SHA_ONE = commitFile.get(0);
        this.SHA_ONE = commit_sha;
        this.rootFolderSHA_ONE = commitFile.get(0);
        // TODO:: check who effects changing SHA_ONE to RootFolderSHA_ONE
        DateFormat df = new SimpleDateFormat(Settings.DATE_FORMAT);
        try {
            this.date = df.parse(commitFile.get(3));
        } catch (ParseException e) {
            throw new RepositoryException(eErrorCodes.WRONG_DATE_FORM);
        }
        this.creator = commitFile.get(4);
    }

    public Commit getPrevCommit() {
        return prevCommit;
    }

    public void setSHA_ONE(String SHA_ONE) {
        this.SHA_ONE = SHA_ONE;
    }

    public void setPrevCommitSHA_ONE(String prevCommitSHA_ONE) {
        this.prevCommitSHA_ONE = prevCommitSHA_ONE;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPrevCommit(Commit prevCommit) {
        this.prevCommit = prevCommit;
    }

    @Override
    public String toString() {
        return LangEN.COMMIT_SHA_ONE + SHA_ONE + System.lineSeparator()
                + LangEN.COMMIT_COMMENT + comment + System.lineSeparator()
                + LangEN.COMMIT_DATE + date + System.lineSeparator()
                + LangEN.COMMIT_CREATOR + creator + System.lineSeparator();
    }
}