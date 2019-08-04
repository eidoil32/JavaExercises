package magit;

import exceptions.*;
import languages.LangEN;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.FileManager;
import utils.MapKeys;
import xml.basic.MagitRepository;
import xml.basic.MagitSingleCommit;
import xml.basic.MagitSingleFolder;
import xml.basic.PrecedingCommits;

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
    private String SHA_ONE, prevCommitSHA_ONE, anotherPrevCommitSHA_ONE, rootFolderSHA_ONE;
    private String comment, creator, content;
    private Date date;
    private Commit prevCommit, anotherPrevCommit;


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

    public Commit XML_Parser(MagitRepository xmlMagit, MagitSingleCommit commit, Folder rootFolder)
            throws MyXMLException, MyFileException {
        Commit temp = new Commit();

        temp.creator = commit.getAuthor();
        try {
            temp.date = new SimpleDateFormat(Settings.DATE_FORMAT).parse(commit.getDateOfCreation());
        } catch (ParseException e) {
            throw new MyXMLException(eErrorCodesXML.WRONG_DATE_FORMAT, commit.getDateOfCreation());
        }

        //String basicPath = FileManager.ignoreLastPartOfPath(xmlMagit.getLocation());

        temp.comment = commit.getMessage();
        temp.rootFolderSHA_ONE = rootFolder.getSHA_ONE();
        String pathToObject = xmlMagit.getLocation() + File.separator + Settings.MAGIT_FOLDER + File.separator + Settings.OBJECT_FOLDER;
        zipAllChainOfInherit(rootFolder, Paths.get(pathToObject));

        List<PrecedingCommits.PrecedingCommit> prevCommits = commit.getPrecedingCommits().getPrecedingCommit();
        if (prevCommits.size() > 2) {
            throw new MyXMLException(eErrorCodesXML.TOO_MANY_PREV_COMMITS, commit.getId());
        } else {
            for (PrecedingCommits.PrecedingCommit precedingCommit : prevCommits) {
                MagitSingleCommit tempCommit = XML_FindMagitCommit(xmlMagit.getMagitCommits().getMagitSingleCommit(), precedingCommit.getId());
                MagitSingleFolder tempFolder = Folder.findRootFolder(xmlMagit.getMagitFolders().getMagitSingleFolder(), tempCommit.getRootFolder().getId());
                addPrevCommit(XML_Parser(xmlMagit, tempCommit, Folder.XML_Parser(tempFolder, xmlMagit, null, xmlMagit.getLocation())));
            }
        }
        temp.calcContent();
        temp.SHA_ONE = DigestUtils.sha1Hex(temp.content);
        return temp;
    }

    private void addPrevCommit(Commit commit) {
        if (this.prevCommit != null) {
            if (this.anotherPrevCommit != null) {
                return;
            } else {
                this.anotherPrevCommit = commit;
                this.anotherPrevCommitSHA_ONE = commit.getSHA_ONE();
            }
        } else {
            this.prevCommit = commit;
            this.prevCommitSHA_ONE = commit.getSHA_ONE();
        }
    }

    public static MagitSingleCommit XML_FindMagitCommit(List<MagitSingleCommit> magitList, String commitID) throws MyXMLException {
        for (MagitSingleCommit commit : magitList) {
            if (commit.getId().equals(commitID)) {
                return commit;
            }
        }
        throw new MyXMLException(eErrorCodesXML.BRANCH_POINT_TO_NONSEXIST_COMMIT, commitID);
    }

    public String getSHA_ONE() {
        return SHA_ONE;
    }

    public void createCommitFile(Repository currentRepository, Map<MapKeys, List<BasicFile>> listMap, String currentUser, String comment)
            throws IOException, MyFileException, RepositoryException {

        this.date = new Date();
        this.creator = currentUser;
        this.comment = comment;
        createNewAndEditedFiles(listMap, currentRepository);
        this.rootFolderSHA_ONE = currentRepository.getRootFolder().getSHA_ONE();
        currentRepository.setSHA_ONE(this.rootFolderSHA_ONE);

        if (listMap == null)
            throw new RepositoryException(eErrorCodes.NOTHING_NEW);

        calcContent();
        this.SHA_ONE = DigestUtils.sha1Hex(this.content);

        File commit = new File(currentRepository.getObjectPath().toString() + File.separator + this.SHA_ONE);
        commit.createNewFile();
        FileWriter fileWriter = new FileWriter(commit);
        fileWriter.write(this.SHA_ONE);
        fileWriter.close();
    }

    private void calcContent() {
        this.content =
                this.rootFolderSHA_ONE + System.lineSeparator() + // repository sha-1
                        this.prevCommitSHA_ONE + System.lineSeparator() + //last commit sha-1
                        this.comment + System.lineSeparator() +
                        new SimpleDateFormat(Settings.DATE_FORMAT).format(this.date) + System.lineSeparator() +
                        this.creator + System.lineSeparator();
    }

    private void createNewAndEditedFiles(Map<MapKeys, List<BasicFile>> listMap, Repository currentRepository) throws MyFileException {
        currentRepository.updateRepository(listMap, currentRepository.getRootFolder());
        currentRepository.calcSHA_ONE();
        zipAllChainOfInherit(currentRepository.getRootFolder(), currentRepository.getObjectPath());
    }

    public void zipAllChainOfInherit(BasicFile file, Path objectPath) throws MyFileException {
        FileManager.zipFile(file, objectPath);
        Folder f = file.tryParseFolder();
        if (f != null) {
            for (Map.Entry<BasicFile, Blob> entry : f.getBlobMap().getMap().entrySet()) {
                zipAllChainOfInherit(entry.getValue(), objectPath);
            }
        }
    }

    public String getComment() {
        return comment;
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

    @Override
    public String toString() {
        return LangEN.COMMIT_SHA_ONE + SHA_ONE + System.lineSeparator()
                + LangEN.COMMIT_COMMENT + comment + System.lineSeparator()
                + LangEN.COMMIT_DATE + date + System.lineSeparator()
                + LangEN.COMMIT_CREATOR + creator + System.lineSeparator();
    }
}