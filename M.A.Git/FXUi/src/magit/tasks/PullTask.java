package magit.tasks;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Magit;
import settings.Settings;

import java.io.IOException;

public class PullTask extends Task<Void> {
    private Magit model;

    public PullTask(Magit model) {
        super();
        this.model = model;
    }

    @Override
    protected Void call() {
        if (model.getRemoteRepository() == null) {
            updateStatus(Settings.language.getString("FX_PULL_COMMAND_FAILED"),3);
            Platform.runLater(()->IntroController.showError(Settings.language.getString("FX_CANNOT_PULL_FROM_NONE_REMOTE")));
            return null;
        }
        updateStatus(Settings.language.getString("FX_CHECKING_FOR_UPDATES"),1);
        try {
            if (model.getCurrentRepository().scanRepository(model.getCurrentUser()) != null) {
                updateStatus(Settings.language.getString("FX_PULL_COMMAND_FAILED"),3);
                Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_OPENED_ISSUES_FOUND")));
                return null;
            }
            updateStatus(Settings.language.getString("FX_START_PULL_DATA"), 2);
            model.pull();
            updateStatus(Settings.language.getString("FX_PULL_COMMAND_FINISH_SUCCESSFULLY"), 3);
        } catch (RepositoryException | MyFileException | IOException e) {
            updateStatus(Settings.language.getString("FX_PULL_COMMAND_FAILED"),3);
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
        }
        return null;
    }

    private void updateStatus(String message, int position) {
        updateProgress(position, 3);
        updateMessage(message);
    }
}