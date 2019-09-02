package controller.screen.main;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private ListView<WarpBlob> diffDetailsListView;
    private ListView<String> commitDetailsListView;
    private TextFlow commitCommentLabel;


    public CommitsDetailsController(MainController mainController, TextFlow commitComment, ListView<WarpBlob> filesDiff, ListView<String> details) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.diffDetailsListView = filesDiff;
        this.commitDetailsListView = details;
        this.commitCommentLabel = commitComment;
    }

    private enum eType {
        DELETE, EDITED, NEW;
    }

    public class WarpBlob {
        private BasicFile file;
        private eType type;

        public WarpBlob(BasicFile file, eType type) {
            this.file = file;
            this.type = type;
        }

        public BasicFile getFile() {
            return file;
        }

        public eType getType() {
            return type;
        }
    }

    public void updateDiffListView(Map<MapKeys, List<BasicFile>> map) {
        new Thread(() -> {
            List<WarpBlob>
                    deleted = getDataFromBasicFile(
                            map.get(MapKeys.LIST_DELETED),
                            eType.DELETE),
                    edited = getDataFromBasicFile(
                            map.get(MapKeys.LIST_CHANGED),
                            eType.EDITED),
                    newFiles = getDataFromBasicFile(
                            map.get(MapKeys.LIST_NEW),
                            eType.NEW);

            List<WarpBlob> union = ListUtils.union(ListUtils.union(deleted, edited), newFiles);

            Platform.runLater(() -> {
                diffDetailsListView.getItems().clear();
                diffDetailsListView.getItems().addAll(union);
            });
        }).start();

        diffDetailsListView.setCellFactory(param -> new ListCell<WarpBlob>() {
            private ImageView imageView = new ImageView();
            @Override
            protected void updateItem(WarpBlob item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getFile().getName());
                    switch (item.getType()) {
                        case DELETE:
                            imageView.setImage(new Image(Settings.DELETE_FILE_IMAGE));
                            break;
                        case EDITED:
                            imageView.setImage(new Image(Settings.EDITED_FILE_IMAGE));
                            break;
                        case NEW:
                            imageView.setImage(new Image(Settings.NEW_FILE_IMAGE));
                            break;
                    }
                    setGraphic(imageView);
                }
            }
        });
    }


    private List<WarpBlob> getDataFromBasicFile(List<BasicFile> files, eType type) {
        List<WarpBlob> data = new LinkedList<>();
        for (BasicFile file : files) {
            data.add(new WarpBlob(file,type));
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
