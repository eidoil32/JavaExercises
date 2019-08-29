package magit;

import controller.screen.main.MainController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import settings.Settings;
import utils.WarpBasicFile;

import java.util.Map;

public class CommitDataTask extends Task<Void> {
    private int work = 0, size;
    private Magit model;
    private Commit current, prev, anotherPrev;
    private MainController mainController;

    public CommitDataTask(Magit model, MainController mainController, Commit current) {
        this.model = model;
        this.mainController = mainController;
        this.current = current;
        this.prev = current.getPrevCommit();
        this.anotherPrev = current.getAnotherPrevCommit();
    }

    @Override
    protected Void call() throws Exception {
        updateProgress(0,5);
        BlobMap prevMap = model.getCurrentRepository().loadDataFromCommit(prev);
        updateProgress(1,5);
        BlobMap anotherPrevMap = model.getCurrentRepository().loadDataFromCommit(anotherPrev);
        updateProgress(2,5);

        BlobMap currentMap = model.getCurrentRepository().loadDataFromCommit(current);
        size = currentMap.getSize() - 1;
        TreeItem<WarpBasicFile> rootFolder = getItemsFromFolder(currentMap, model.getCurrentRepository().getRootFolder());
        Platform.runLater(() -> mainController.getCommitFileTree().setRoot(rootFolder)); //updating TreeView component

        updateProgress(3,5);
        mainController.updateButtonData(prevMap, currentMap, mainController.getCompareToFatherOneButton(), prev);
        updateProgress(4,5);
        mainController.updateButtonData(anotherPrevMap, currentMap, mainController.getCompareToFatherTwoButton(), anotherPrev);
        updateProgress(5,5);
        updateMessage(Settings.language.getString("FX_LOADING_COMMIT_DATA_FINISH_SUCCESSFULLY"));
        return null;
    }

    private TreeItem<WarpBasicFile> getItemsFromFolder(BlobMap blobMap, Folder root) {
        TreeItem<WarpBasicFile> rootFolder = new TreeItem<>(new WarpBasicFile(root));
        for (Map.Entry<BasicFile, Blob> entry : blobMap.getMap().entrySet()) {
            updateProgress(work++, size);
            if (entry.getValue().getType() == eFileTypes.FILE) {
                rootFolder.getChildren().add(new TreeItem<>(new WarpBasicFile(entry.getValue())));
            } else {
                TreeItem<WarpBasicFile> tempRootFolder = getItemsFromFolder(((Folder) entry.getValue()).getBlobMap(), (Folder) entry.getValue());
                rootFolder.getChildren().add(tempRootFolder);
            }
        }

        return rootFolder;
    }
}
