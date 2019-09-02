package magit.tasks;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Magit;
import settings.Settings;

import java.io.IOException;

public class FetchTask extends Task<Void> {
    private Magit model;

    public FetchTask(Magit model) {
        super();
        this.model = model;
    }

    @Override
    protected Void call() {
        if (model.getRemoteRepository() == null) {
            Platform.runLater(()-> IntroController.showError(Settings.language.getString("FX_CANNOT_FETCH_FROM_NONE_REMOTE_REPOSITORY")));
            return null;
        }
        updateStatus(Settings.language.getString("FX_CHECKING_FOR_UPDATES"),1);
        try {
            if (model.getCurrentRepository().scanRepository(model.getCurrentUser()) != null) {
                Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_OPENED_ISSUES_FOUND")));
                return null;
            }
            updateStatus(Settings.language.getString("FX_START_FETCHING"), 2);
            model.fetch(model.getRemoteRepository());
            updateStatus(Settings.language.getString("FX_FETCH_COMMAND_FINISH_SUCCESSFULLY"), 3);
        } catch (RepositoryException | MyFileException | IOException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
        }
        return null;
    }

    private void updateStatus(String message, int position) {
        updateProgress(position, 3);
        updateMessage(message);
    }
}
