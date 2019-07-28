package com.magit.engine;

import com.magit.exceptions.RepositoryException;
import com.magit.settings.LangEN;
import com.magit.settings.MapKeys;
import com.magit.ui.Main;
import com.magit.utils.FileManager;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public String getSHAONE() {
        return SHA_ONE;
    }

    public void createCommitFile(Repository currentRepository, Map<String, List<BasicFile>> listMap)
            throws IOException, RepositoryException {
        createNewAndEditedFiles(listMap, currentRepository);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(currentRepository.getSHAONE()).append("\n"); // repository sha-1
        stringBuilder.append(prevCommitSHA_ONE).append("\n"); //last commit sha-1
        stringBuilder.append(Main.engine.askUserForString(LangEN.ENTER_COMMENT_FOR_COMMIT, eTypeOfString.T_COMMIT)).append("\n");
        SimpleDateFormat dateFormat = new SimpleDateFormat(LangEN.DATE_FORMAT);
        date = new Date();
        stringBuilder.append(dateFormat.format(date)).append("\n");
        stringBuilder.append(Main.engine.getSystem().getCurrentUser()).append("\n");
        File commit = new File(currentRepository.getObjectPath().toString() + "\\" + DigestUtils.sha1Hex(stringBuilder.toString()));
        commit.createNewFile();
        FileWriter fileWriter = new FileWriter(commit);
        fileWriter.write(stringBuilder.toString());
        fileWriter.close();
    }

    private void createNewAndEditedFiles(Map<String, List<BasicFile>> listMap, Repository currentRepository) throws RepositoryException, IOException {
        List<BasicFile> newFiles = listMap.get(MapKeys.LIST_NEW);
        for (BasicFile file : newFiles) {
            FileManager.createFile(file, currentRepository.getObjectPath());
        }
        List<BasicFile> editedFiles = listMap.get(MapKeys.LIST_CHANGED);
        for (BasicFile file : editedFiles) {
            FileManager.createFile(file, currentRepository.getObjectPath());
        }
        currentRepository.updateRepository(listMap);
    }
}