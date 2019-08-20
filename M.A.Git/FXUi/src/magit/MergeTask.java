package magit;

import controller.Controller;
import controller.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.utils.MergeProperty;
import magit.utils.MyBooleanProperty;
import org.apache.commons.codec.digest.DigestUtils;
import settings.Settings;
import utils.eUserMergeChoice;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MergeTask extends Task<Void> {
    private Magit model;
    private Branch target;
    private Controller mainController;
    private MergeProperty mergeProperty = new MergeProperty();
    private MyBooleanProperty booleanProperty = new MyBooleanProperty();
    private Map<String, BlobMap> changes;
    private BlobMap userApprove;

    public MergeTask(Magit model, Branch target, Controller mainController) {
        this.model = model;
        this.target = target;
        this.mainController = mainController;
    }

    @Override
    protected Void call() {
        try {
            updateMessage(Settings.language.getString("FINDING_ANCESTOR_COMMIT"));
            updateProgress(0, 3);
            Commit ancestor = model.getAncestorCommit(target);

            updateProgress(1, 3);
            updateMessage(Settings.language.getString("SEARCH_FOR_DIFFERENCES"));
            try {
                changes = model.findChanges(ancestor, target);
            } catch (RepositoryException | MyFileException e) {
                Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
            }

            mergeProperty.addListener((observable -> {
                if (mergeProperty.isInError()) {
                    Platform.runLater(() -> IntroController.showAlert(mergeProperty.getErrorCode().getMessage()));
                    this.cancel();
                }
            }));

            booleanProperty.addListener(observable -> {
                if (booleanProperty.get()) {
                    updateProgress(2, 3);
                    updateMessage(Settings.language.getString("START_MARGIN"));

                    model.merge(changes, target, ancestor, userApprove);
                    updateProgress(3, 3);
                    updateMessage(Settings.language.getString("MERGE_COMPLETED_SUCCESSFULLY"));
                } else {
                    updateProgress(3, 3);
                    updateMessage(Settings.language.getString("MARGE_CANCELED"));
                }
            });

            userApprove = askUserWhatToTake(changes.get(Settings.KEY_CHANGE_MAP), changes);
        } catch (IOException e) {
            Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
        }
        return null;
    }

    private BlobMap askUserWhatToTake(BlobMap blobMap, Map<String, BlobMap> changes) {
        BlobMap finalMap = new BlobMap(new HashMap<>());
        for (Map.Entry<BasicFile, Blob> entry : blobMap.getMap().entrySet()) {
            if (this.isCancelled()) {
                booleanProperty.set(false);
            }
            if (entry.getValue().getType() == eFileTypes.FILE) {
                Map<eUserMergeChoice, Blob> duplicate = blobMap.getDuplicate(entry.getValue(), changes);
                mergeProperty.addListener(((observable, oldValue, newValue) -> {
                    if (!mergeProperty.isInError()) {
                        eUserMergeChoice choice = mergeProperty.getChoice();
                        if (choice != eUserMergeChoice.OTHER) {
                            finalMap.addToMap(duplicate.get(choice));
                        } else {
                            Blob temp = duplicate.get(eUserMergeChoice.ANCESTOR);
                            temp.setContent(mergeProperty.getContent());
                            temp.setSHA_ONE(DigestUtils.sha1Hex(temp.getContent()));
                            temp.setDate(new Date());
                            temp.setEditorName(model.getCurrentUser());
                            finalMap.addToMap(temp);
                        }
                    }
                }));
                Platform.runLater(() -> mainController.mergeWindow(mergeProperty, duplicate,booleanProperty));
            }
        }

        booleanProperty.set(true);
        return finalMap;
    }
}
