package magit.tasks;

import controller.screen.intro.IntroController;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import magit.Branch;
import magit.Magit;
import settings.Settings;

import java.io.IOException;

public class PushBranchTask extends Task<Void> {
    private Branch selectedBranch;
    private Magit model;

    public PushBranchTask(Branch selectedBranch, Magit model) {
        this.selectedBranch = selectedBranch;
        this.model = model;
    }

    @Override
    protected Void call() {
        try {
            if (model.getRemoteRepository() == null) {
                return showError(Settings.language.getString("FX_CANNOT_PUSH_BRANCH_TO_NONE_REMOTE"));
            } else if (selectedBranch == model.getCurrentBranch() &&
                    model.getCurrentRepository().scanRepository(model.getCurrentUser()) != null) {
                return showError(Settings.language.getString("FX_OPENED_CHANGES_FOR_SELECTED_BRANCH"));
            }
        } catch (RepositoryException | IOException | MyFileException e) {
            return showError(e.getMessage());
        }
        updateStatus(Settings.language.getString("FX_PUSH_BRANCH_CALCULATE_DELTA"), 1);
        try {
            model.pushBranch(selectedBranch);
        } catch (RepositoryException | IOException e) {
            return showError(e.getMessage());
        }
        updateStatus(Settings.language.getString("FX_PUSH_BRANCH_FINISH_SUCCESSFULLY"), 2);
        return null;
    }

    private void updateStatus(String message, int position) {
        updateProgress(position, 2);
        updateMessage(message);
    }

    private Void showError(String message) {
        Platform.runLater(() -> IntroController.showError(message));
        updateStatus(Settings.language.getString("FX_PUSH_BRANCH_COMMAND_FAILED"),2);
        return null;
    }
}
