package magit.tasks;

import controller.screen.intro.IntroController;
import controller.screen.main.MainController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Branch;
import magit.Magit;
import settings.Settings;

import java.io.IOException;

public class PullTask extends Task<Void> {
    private Magit model;
    private MainController mainController;

    public PullTask(Magit model, MainController mainController) {
        super();
        this.model = model;
        this.mainController = mainController;
    }

    @Override
    protected Void call() {
        if (model.getRemoteRepository() == null) {
            Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_CANNOT_PULL_FROM_NONE_REMOTE")));
            return null;
        }
        updateStatus(Settings.language.getString("FX_CHECKING_FOR_UPDATES"), 1);
        try {
            if (model.getCurrentRepository().scanRepository(model.getCurrentUser()) != null) {
                Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_OPENED_ISSUES_FOUND")));
                return null;
            }
            updateStatus(Settings.language.getString("FX_START_PULL_DATA"), 2);
            Branch remote = model.pull();
            updateStatus(Settings.language.getString("START_MARGIN"), 2);
            Task merge = new MergeTask(model, remote, mainController);
            Platform.runLater(() -> {
                mainController.bindTaskToUIComponents(merge, false);
                new Thread(merge).start();
            });
            model.updateRemoteAfterMerge(remote);
            updateStatus(Settings.language.getString("FX_PULL_COMMAND_FINISH_SUCCESSFULLY"), 3);
        } catch (RepositoryException | MyFileException | IOException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
            updateStatus("FX_PULL_COMMAND_FAILED", 3);
        }
        return null;
    }

    private void updateStatus(String message, int position) {
        updateProgress(position, 3);
        updateMessage(message);
    }
}
