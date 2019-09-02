package magit.tasks;

import controller.screen.intro.IntroController;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Magit;
import settings.Settings;

import java.io.File;
import java.io.IOException;

public class CloneTask extends Task<Void> {
    private Magit model;
    private File targetFolder, baseFolder;
    private String newName;

    public CloneTask(Magit model, String newName,  File ... values) {
        this.model = model;
        this.targetFolder = values[0];
        this.baseFolder = values[1];
        this.newName = newName;
    }

    @Override
    protected Void call() {
        updateTask(0, Settings.language.getString("FX_CLONE_TASK_LOADING_DATA"));
        try {
            updateTask(1,Settings.language.getString("FX_CLONE_COPY_FILES_FROM_REMOTE_REPOSITORY"));
            Magit remote = model.magitClone(baseFolder,targetFolder);
            remote.getCurrentRepository().setName(newName);
            updateTask(2,Settings.language.getString("FX_CLONE_FINISH_SUCCESSFULLY"));
        } catch (IOException | RepositoryException e) {
            Platform.runLater(()-> IntroController.showError(e.getMessage()));
            updateTask(2,Settings.language.getString("FX_CLONE_FINISH_FAILED"));
        }
        return null;
    }

    private void updateTask(int work, String message) {
        int sumOfWork = 2;
        updateProgress(work, sumOfWork);
        updateMessage(message);
    }
}
