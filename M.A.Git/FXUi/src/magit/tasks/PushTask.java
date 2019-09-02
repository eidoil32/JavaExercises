package magit.tasks;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Magit;
import settings.Settings;

import java.io.IOException;

public class PushTask extends Task<Void> {
    private Magit model;

    public PushTask(Magit model) {
        super();
        this.model = model;
    }

    @Override
    protected Void call() {
        if (model.getRemoteRepository() == null) {
            Platform.runLater(()-> IntroController.showError(Settings.language.getString("FX_RR_NOT_EXIST_NOT_WHERE_TO_PUSH")));
            return null;
        }
        updateStatus(Settings.language.getString("FX_CHECKING_FOR_UPDATES"),1);
        try {
            if (model.getRemoteRepository().getCurrentRepository().scanRepository(model.getCurrentUser()) != null) {
                Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_OPENED_ISSUES_FOUND_IN_RR")));
                return null;
            }
            updateStatus(Settings.language.getString("FX_START_PUSH"), 2);
            model.push();
            updateStatus(Settings.language.getString("FX_PUSH_COMMAND_FINISH_SUCCESSFULLY"), 3);
        } catch (RepositoryException | MyFileException | IOException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
            updateStatus(Settings.language.getString("FX_PUSH_COMMAND_FAILED"),3);
        }
        return null;
    }

    private void updateStatus(String message, int position) {
        updateProgress(position, 3);
        updateMessage(message);
    }
}
