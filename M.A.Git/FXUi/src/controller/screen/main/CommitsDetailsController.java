package controller.screen.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import magit.BasicFile;
import magit.Commit;
import magit.Magit;
import org.apache.commons.collections4.ListUtils;
import settings.Settings;
import utils.MapKeys;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommitsDetailsController {
    private Magit model;
    private MainController mainController;
    private ListView<String> diffDetailsListView, commitDetailsListView;
    private TextFlow commitCommentLabel;

    @SafeVarargs
    public CommitsDetailsController(MainController mainController, TextFlow commitComment, ListView<String> ... listViews) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.diffDetailsListView = listViews[0];
        this.commitDetailsListView = listViews[1];
        this.commitCommentLabel = commitComment;
    }

    public void updateDiffListView(Map<MapKeys, List<BasicFile>> map) {
        new Thread(() -> {
            List<String>
                    deleted = getDataFromBasicFile(
                    map.get(MapKeys.LIST_DELETED),
                    Settings.language.getString("FX_DIFF_DELETED_ITEM")),
                    edited = getDataFromBasicFile(
                            map.get(MapKeys.LIST_CHANGED),
                            Settings.language.getString("FX_DIFF_EDITED_ITEM")),
                    newFiles = getDataFromBasicFile(
                            map.get(MapKeys.LIST_NEW),
                            Settings.language.getString("FX_DIFF_NEW_ITEM"));

            List<String> union = ListUtils.union(ListUtils.union(deleted, edited), newFiles);

            Platform.runLater(() -> {
                diffDetailsListView.getItems().clear();
                diffDetailsListView.getItems().addAll(union);
            });
        }).start();
    }


    private List<String> getDataFromBasicFile(List<BasicFile> files, String addon) {
        List<String> data = new LinkedList<>();
        for (BasicFile file : files) {
            data.add(addon + Settings.FX_DIFF_SEPARATOR + file.getType() + Settings.FX_DIFF_SEPARATOR + file.getName());
        }
        return data;
    }

    public void updateCommitDetailsListView(Commit commit) {
        List<String> commitData = new LinkedList<>();
        commitData.add(Settings.language.getString("COMMIT_SHA_ONE") + commit.getSHA_ONE());
        commitData.add(Settings.language.getString("COMMIT_DATE") + commit.getDate());
        commitData.add(Settings.language.getString("COMMIT_CREATOR") + commit.getCreator());
        if (commit.getPrevCommit() != null) {
            commitData.add(Settings.language.getString("COMMIT_PREV_SHA_ONE") + commit.getPrevCommitSHA_ONE());
            if (commit.getAnotherPrevCommit() != null) {
                commitData.add(Settings.language.getString("COMMIT_PREV_SHA_ONE") + commit.getAnotherPrevCommitSHA_ONE());
            }
        }
        ObservableList<String> content = FXCollections.observableList(commitData);
        commitDetailsListView.setItems(content);
        commitDetailsListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton().equals(MouseButton.PRIMARY)) {
                    if (event.getClickCount() == 2) {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(commit.getSHA_ONE());
                        clipboard.setContent(content);
                    }
                }
            }
        });
        Text comment = new Text(commit.getComment());
        commitCommentLabel.getChildren().clear();
        commitCommentLabel.getChildren().add(comment);
    }
}
