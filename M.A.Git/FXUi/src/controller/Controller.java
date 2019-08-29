package controller;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.*;
import magit.utils.*;
import settings.Settings;
import utils.MapKeys;
import utils.PairBranchCommit;
import utils.WarpBasicFile;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

//TODO:: split controller to parts

public class Controller {
    @FXML
    private ProgressBar executeCommandProgressBar;
    @FXML
    private MenuItem menuItem_themeManager, menuItem_exportToXML,
            createNewBranchMenuItem, changeRepositoryMenuItem, menuItem_loadXMLRepository, menuItem_changeName,
            menuItem_quit, menuItem_changeRepository, menuItem_about, menuItem_createNewRepository, menuItem_manageBranches,
            menuItemFetch, menuItemPull, menuItemPush, menuItemMerge, menuItemCommit, menuItemResetBranch, menuItemClone;
    @FXML
    private CheckMenuItem menuItem_animation, menuItemShowCommitTree;
    @FXML
    private ImageView refreshTable;
    @FXML
    private MenuButton repositoryListMenuBtn, branchListMenuBtn;
    @FXML
    private Button commitBtn, scanRepositoryBtn, mergeBtn, compareToFatherOneBtn, compareToFatherTwoBtn;
    @FXML
    private TableView<List<String>> commitTable;
    @FXML
    private TableColumn<List<String>, String> branchCommitTableColumn, commentCommitTableColumn, shaoneCommitTableColumn;
    @FXML
    private TableColumn<List<String>, Date> dateCommitTableColumn;
    @FXML
    private ListView<String> deletedFilesListView, editedFilesListView, newFilesListView,
            diffDetailsListView, commitDetailsListView;
    @FXML
    private TreeView<WarpBasicFile> commitFileTree;
    @FXML
    private Tab fileTreeTab, diffTab, commitTab;
    @FXML
    private Label executeCommandProgress, currentUser;
    @FXML
    private TextFlow commitCommentLabel;
    @FXML
    private BorderPane mainBoard;
    @FXML
    private TitledPane newFileTab, deletedFileTab, editedFileTab;
    @FXML
    private Menu menuFile, menuRepository, menuTools, menuHelp;
    @FXML
    private MenuBar topMenuBar;

    private Node commitTreePane;
    private IntroController introController;
    private Magit model;
    private StringProperty stringProperty_CurrentUser, stringProperty_CurrentState;
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty(), languageProperty, themeProperty;
    private Stage primaryStage;
    private boolean isAnimationTurnOn = false;
    private MyBooleanProperty updateCommitTree = new MyBooleanProperty();

    public void setStringProperty_CurrentUser(StringProperty currentUser) {
        this.stringProperty_CurrentUser = currentUser;
        currentUser.setValue(currentUser.getValue());
        stringProperty_CurrentUser.addListener((observable, oldValue, newValue) -> {
            this.currentUser.setText(newValue);
        });
    }

    @FXML
    public void initialize() {
        this.branchCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
        this.branchCommitTableColumn.setCellFactory(object -> new TableCell<List<String>, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(Settings.EMPTY_STRING);
                } else {
                    if (!item.equals(Settings.EMPTY_STRING)) {
                        this.setId("commit-table-branch-name-column");
                    } else {
                        this.setId("");
                    }
                    setText(item);
                }
            }
        });
        this.commentCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        this.dateCommitTableColumn.setCellValueFactory(param -> {
            try {
                return new ReadOnlyObjectWrapper<>(new SimpleDateFormat(Settings.DATE_FORMAT).parse(param.getValue().get(2)));
            } catch (ParseException e) {
                Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
                return new ReadOnlyObjectWrapper<>(new Date());
            }
        });
        this.dateCommitTableColumn.setCellFactory(object -> new TableCell<List<String>, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    long difference = new Date().getTime() - item.getTime();
                    float daysBetween = (difference / (Settings.DATE_CALCULATE));
                    if ((int) daysBetween == 0) {
                        setText(Settings.language.getString("FX_COMMIT_TABLE_TODAY"));
                    } else if (daysBetween < Settings.MINIMUM_DAY_TO_SHOW) {
                        setText(String.format(Settings.language.getString("FX_COMMIT_TABLE_X_DAYS_BEFORE"), daysBetween));
                    } else {
                        setText(new SimpleDateFormat(Settings.FX_DATE_FORMAT).format(item));
                    }
                }
            }
        });
        this.shaoneCommitTableColumn.setCellValueFactory(param -> new

                ReadOnlyStringWrapper(param.getValue().

                get(3)));
        this.commitTable.getSelectionModel().
                selectedItemProperty().
                addListener(((observable, oldValue, newValue) ->
                {
                    if (newValue != null) {
                        Task task = new Task<Void>() {
                            @Override
                            protected Void call() {
                                try {
                                    Commit commit = model.getCommitData(newValue.get(3));
                                    Platform.runLater(() -> {
                                        commitCommentLabel.getChildren().clear();
                                        updateCommitDiffAndFileTree(commit);
                                    });
                                } catch (IOException e) {
                                    IntroController.showAlert(e.getMessage());
                                }
                                return null;
                            }
                        };
                        bindTaskToUIComponents(task);
                        new Thread(task).start();
                    }
                }));
        updateCommitTree.addListener((observable ->

        {
            if (mainBoard.getRight() != null) {
                mainBoard.setRight(commitTreePane);
            }
        }));
    }

    @FXML
    private void onNameLabel_Clicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                onMenuItem_ChangeName_Click(new ActionEvent());
            }
        }
    }

    private void updateCommitDiffAndFileTree(Commit commit) {
        diffDetailsListView.getItems().clear();
        commitFileTree.setRoot(null);
        updateCommitDetailsListView(commit);
        Task task = new CommitDataTask(model, this, commit);
        bindTaskToUIComponents(task);
        new Thread(task).start();
    }

    public ScrollPane buildCommitTree() {
        ScrollPane scrollPane = new ScrollPane();
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        Group root = new Group();
        vbox.getChildren().add(root);
        vbox.setPadding(new Insets(10));

        new Thread(() -> {
            Map<Branch, List<PairBranchCommit>> commitMap = model.getAllCommits();
            List<Node> circles = warpDrawCommitTree(commitMap);
            Platform.runLater(() -> root.getChildren().addAll(circles));
        }).start();
        double size = model.getCurrentRepository().getActiveBranches().size();
        size = (Settings.COMMIT_CIRCLE_RADIUS + Settings.COMMIT_SPACE_BETWEEN_CIRCLES) * size; //each branch take column width radius + 10 (for spaces)

        scrollPane.setContent(vbox);
        scrollPane.setMinWidth(size > 200 ? 200 : size);
        scrollPane.setMaxWidth(size > 200 ? 200 : size);
        scrollPane.setFitToWidth(true);
        commitTreePane = scrollPane;
        updateCommitTree.set(true);
        vbox.setRotate(180); // the first one will be the newest commit
        return scrollPane;
    }

    private PairBranchCommit getNextCommitByTimeLine(Map<Branch, List<PairBranchCommit>> map) { // (theta number of branches)
        PairBranchCommit recentCommit = null;
        List<PairBranchCommit> tempListToRemoveFrom = null;
        for (Map.Entry<Branch, List<PairBranchCommit>> entry : map.entrySet()) {
            List<PairBranchCommit> temp = entry.getValue();
            if (temp.size() > 0) {
                PairBranchCommit tempPair = temp.get(0);
                if (recentCommit == null || recentCommit.getCommit().getDate().compareTo(tempPair.getCommit().getDate()) > 0) {
                    recentCommit = tempPair;
                    tempListToRemoveFrom = temp;
                }
            }
        }

        if (recentCommit != null) {
            tempListToRemoveFrom.remove(recentCommit);
        }

        return recentCommit;
    }

    private List<Node> warpDrawCommitTree(Map<Branch, List<PairBranchCommit>> map) {
        PairBranchCommit current = getNextCommitByTimeLine(map);
        List<Node> nodeList = new LinkedList<>();
        Map<Commit, Point> commitNodeMap = new HashMap<>();
        Map<Branch, Point> pointMap = new HashMap<>();
        Map<Commit, Branch> pointedBranch = model.getPointedBranchesToCommitsMap();
        List<Branch> branches = model.getCurrentRepository().getActiveBranches();

        int x = Settings.COMMIT_TREE_START_X, y = Settings.COMMIT_TREE_START_Y;

        for (Branch branch : branches) {
            pointMap.put(branch, new Point(x, y));
            x += (int) Settings.COMMIT_SPACE_BETWEEN_CIRCLES;
        }

        Point currentRow = new Point(Settings.COMMIT_TREE_START_X, Settings.COMMIT_TREE_START_Y);

        while (current != null) {
            if (!commitNodeMap.containsKey(current.getCommit())) {
                Point coords = new Point(pointMap.get(current.getBranch()).x, currentRow.y);
                Circle circle = createCircle(coords, current.getCommit(), pointedBranch);
                currentRow.setLocation(currentRow.x, currentRow.y + Settings.COMMIT_TREE_ADD_TO_Y);
                commitNodeMap.put(current.getCommit(), coords);
                nodeList.add(circle);
            }
            current = getNextCommitByTimeLine(map);
        }

        nodeList.addAll(wireUpAllNodes(commitNodeMap, branches));
        return nodeList;
    }

    private List<Node> wireUpAllNodes(Map<Commit, Point> commitNodeMap, List<Branch> branches) {
        List<Node> nodeList = new LinkedList<>();

        for (Branch branch : branches) {
            Commit firstCommit = branch.getCommit();
            nodeList.addAll(createLines(commitNodeMap, firstCommit));
        }

        return nodeList;
    }

    private List<Node> createLines(Map<Commit, Point> commitNodeMap, Commit commit) {
        List<Node> nodeList = new LinkedList<>();

        if (commit != null) {
            Point coords = commitNodeMap.get(commit);
            Commit[] commits = new Commit[2];
            commits[0] = commit.getPrevCommit();
            commits[1] = commit.getAnotherPrevCommit();
            for (int i = 0; i < 2; i++) {
                Commit current = commits[i];
                if (current != null && commitNodeMap.containsKey(current)) {
                    Point prevCircle = commitNodeMap.get(current);
                    Line line = createLine(coords, prevCircle);
                    nodeList.add(line);
                    nodeList.addAll(createLines(commitNodeMap, current));
                }
            }
        }

        return nodeList;
    }

    private Circle createCircle(Point coordinates, Commit data, Map<Commit, Branch> pointedBranches) {
        Circle circle = new Circle(coordinates.getX(), coordinates.getY(), Settings.COMMIT_CIRCLE_RADIUS);
        circle.setOnMouseClicked(event -> Platform.runLater(() -> updateCommitDiffAndFileTree(data)));
        circle.setOnMouseEntered(event -> {
            ((Circle)event.getSource()).toFront();
            circle.setFill(Paint.valueOf("#FF4242"));
        });
        circle.setOnMouseExited(event -> circle.setFill(Paint.valueOf("#000000")));
        if (pointedBranches.containsKey(data)) {
            circle.setStrokeWidth(2);
            circle.setStroke(Paint.valueOf("#FF4242"));
            Tooltip custom = new Tooltip(pointedBranches.get(data).getName());
            Tooltip.install(circle, custom);
        }
        return circle;
    }

    public void updateButtonData(BlobMap anotherPrevMap, BlobMap map, Button button, Commit prevCommit) throws RepositoryException {
        if (prevCommit != null) {
            Map<MapKeys, List<BasicFile>> anotherPrevToCurrent = model.getCurrentRepository().createMapForScanning();
            model.getCurrentRepository().scanBetweenMaps(map.getMap(), anotherPrevMap.getMap(), anotherPrevToCurrent);
            model.getCurrentRepository().scanForDeletedFiles(map.getMap(), anotherPrevMap.getMap(), anotherPrevToCurrent.get(MapKeys.LIST_DELETED));
            button.setOnAction(event -> Platform.runLater(() -> updateDiffListView(anotherPrevToCurrent)));
            button.setDisable(false);
            Platform.runLater(() -> updateDiffListView(anotherPrevToCurrent));
        } else {
            button.setDisable(true);
        }
    }

    private void updateDiffListView(Map<MapKeys, List<BasicFile>> map) {
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

        diffDetailsListView.getItems().clear();
        diffDetailsListView.getItems().addAll(deleted);
        diffDetailsListView.getItems().addAll(edited);
        diffDetailsListView.getItems().addAll(newFiles);
    }

    private List<String> getDataFromBasicFile(List<BasicFile> files, String addon) {
        List<String> data = new LinkedList<>();
        for (BasicFile file : files) {
            data.add(addon + Settings.FX_DIFF_SEPARATOR + file.getType() + Settings.FX_DIFF_SEPARATOR + file.getName());
        }
        return data;
    }

    private Line createLine(Point src, Point dest) {
        Line line = new Line(src.x, src.y, dest.x, dest.y);
        line.setStrokeWidth(Settings.COMMIT_TREE_LINE_TICK);
        line.setOpacity(0.2);
        return line;
    }

    @FXML
    public void onMenuItemShowCommitTree_Click(ActionEvent event) {
        if (mainBoard.getRight() == null) {
            mainBoard.setRight(commitTreePane);
        } else {
            mainBoard.setRight(null);
        }
    }

    @FXML
    private void onMergeButton_Click(ActionEvent event) {
        MyBooleanProperty checkOpenIssues = new MyBooleanProperty();
        checkOpenIssues.addListener(((o, oldValue, newValue) -> {
            if (newValue) {
                BooleanProperty flag = new SimpleBooleanProperty();
                SmartListener<Branch> branch = new SmartListener<>();
                selectBranch(branch, model.getCurrentRepository().getActiveBranches(), flag);
                flag.addListener((observable -> {
                    if (branch.getItem() != null) {
                        if (!branch.getItem().equals(model.getCurrentBranch())) {
                            Task merge = new MergeTask(model, branch.getItem(), this);
                            bindTaskToUIComponents(merge);
                            new Thread(merge).start();
                        } else {
                            IntroController.showAlert(Settings.language.getString("CANNOT_MERGE_BRANCH_TO_ITSELF"));
                        }
                    }
                }));
            }
        }));
        new Thread(() -> {
            try {
                boolean val = model.getCurrentRepository().scanRepository(model.getCurrentUser()) == null;
                if (val) {
                    Platform.runLater(() -> checkOpenIssues.setValue(val));
                } else {
                    Platform.runLater(() -> IntroController.showAlert(Settings.language.getString("FX_MARGE_ABORT_OPEN_ISSUES")));
                }
            } catch (IOException | MyFileException | RepositoryException e) {
                Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
            }
        }).start();
    }

    private void selectBranch(SmartListener branch, List<Branch> branches, BooleanProperty flag) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_SELECT_POPUP);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            SelectController selectController = loader.getController();
            selectController.setQuestion(Settings.language.getString("FX_CHOOSE_BRANCH"));
            selectController.setListener(branch);
            ObservableList<Object> list = FXCollections.observableArrayList();
            for (Branch branch1 : branches) {
                if (!branch1.isHead())
                    list.add(branch1);
            }
            selectController.setListForChoice(list);
            selectController.setStage(stage);
            selectController.setFlag(flag);
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_SELECT_POPUP_HEIGHT + 50);
            stage.setMinWidth(Settings.MAGIT_UI_SELECT_POPUP_WIDTH + 50);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_SELECT_POPUP_WIDTH, Settings.MAGIT_UI_SELECT_POPUP_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.isRepositoryExists.addListener((observable, oldValue, newValue) -> {
            try {
                new MagitUI(model, primaryStage, introController);
            } catch (IOException e) {
                IntroController.showAlert(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage());
            }
        });
    }

    private void updateCommitDetailsListView(Commit commit) {
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

    @FXML
    private void onCommitButtonClick(ActionEvent event) {
        commitCommentPopup((observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.CANCEL_BTN_CLICKED_STRING)) {
                Task commit = new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            updateProgress(0, 1);
                            model.commitMagit(stringProperty_CurrentUser.get(), newValue);
                            updateProgress(1, 1);
                            updateMessage(Settings.language.getString("COMMIT_CREATED_SUCCESSFULLY"));
                            Platform.runLater(() -> {
                                initializeTableViewCommit(); //need to change to add only new line instead refresh all table!
                                cleanListViews();
                                commitTreePane = buildCommitTree();
                                updateTree();
                            });
                        } catch (IOException | MyFileException | RepositoryException e) {
                            IntroController.showAlert(e.getMessage());
                        }
                        return null;
                    }
                };
                bindTaskToUIComponents(commit);
                new Thread(commit).start();
            }
        });
    }

    public void commitCommentPopup(ChangeListener<String> command) {
        StringProperty comment = new SimpleStringProperty();
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_SMART_POPUP_BOX);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            comment.addListener(command);
            SmartPopUpController smartPopUpController = loader.getController();
            smartPopUpController.setStringProperty(comment);
            smartPopUpController.setTitle(Settings.language.getString("PLEASE_ENTER_YOUR_COMMENT"));
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT + 100);
            stage.setMinWidth(Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH + 50);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH, Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void updateTree() {
        if (mainBoard.getRight() != null) {
            buildCommitTree();
            mainBoard.setRight(commitTreePane);
        }
    }

    private void bindTaskToUIComponents(Task task) {
        this.executeCommandProgress.textProperty().bind(task.messageProperty());
        this.executeCommandProgressBar.progressProperty().bind(task.progressProperty());
    }

    public void initializeTableViewCommit() {
        Map<String, Object> results = model.getCurrentRepository().getAllCommits();
        List<Commit> commitList = new LinkedList<>(new HashSet<>((List<Commit>) results.get(Settings.KEY_COMMIT_LIST)));
        Map<Commit, Branch> headCommits = (Map<Commit, Branch>) results.get(Settings.KEY_COMMIT_BRANCH_LIST);

        final ObservableList<List<String>> data = FXCollections.observableArrayList();

        for (Commit commit : commitList) {
            List<String> unit = new ArrayList<>();
            if (headCommits.containsKey(commit)) {
                unit.add(headCommits.get(commit).getName());
            } else {
                unit.add(Settings.EMPTY_STRING);
            }
            String temp = commit.getComment();
            unit.add(temp.substring(Settings.MIN_COMMENT_SUBSTRING, Math.min(temp.length(), Settings.MAX_COMMENT_SUBSTRING)));
            unit.add(new SimpleDateFormat(Settings.DATE_FORMAT).format(commit.getDate()));
            unit.add(commit.getSHA_ONE());
            data.add(unit);
        }
        dateCommitTableColumn.setSortType(TableColumn.SortType.DESCENDING);
        commitTable.setItems(data);
        commitTable.getSortOrder().add(dateCommitTableColumn);
        commitTable.sort();
    }

    @FXML
    private void onMenuItem_AboutClick(ActionEvent event) {
        Stage about = new Stage();
        about.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));

        BorderPane root = new BorderPane();
        TextFlow text = new TextFlow();
        text.getChildren().add(new Text(Settings.language.getString("ABOUT_MAGIT")));
        text.getChildren().add(new Text(System.lineSeparator() + Settings.language.getString("COPYRIGHTS")));
        text.setPadding(new Insets(10));
        root.setCenter(text);
        about.setScene(new MyScene(root, 300, 200));
        about.setMinHeight(200);
        about.setMinWidth(300);
        about.initOwner(primaryStage);
        about.initModality(Modality.WINDOW_MODAL);
        about.show();
    }

    @FXML
    private void onMenuItem_AnimationClick(ActionEvent event) {
        this.isAnimationTurnOn = !isAnimationTurnOn;
    }

    @FXML
    private void onMenuItem_ExportToXMLClick(ActionEvent event) {
        final File target = Utilities.choiceFolderDialog(primaryStage.getScene());
        Task exportXML = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    updateProgress(0, 3);
                    if (target != null) {
                        updateMessage(Settings.language.getString("CONVERT_MAGIT_TO_XML"));
                        updateProgress(1, 3);
                        MagitRepository magitRepository = model.convertToXMLScheme();
                        updateProgress(2, 3);
                        updateMessage(Settings.language.getString("CREATE_XML_FILE"));
                        model.exportFile(magitRepository, target.getPath());
                        updateProgress(3, 3);
                        updateMessage(String.format(Settings.language.getString("EXPORT_TO_XML_SUCCESS"), target.getPath()));
                    } else {
                        updateProgress(3, 3);
                        Platform.runLater(() -> IntroController.showAlert(Settings.language.getString("EXPORT_TO_XML_CANCELED")));
                        updateMessage(Settings.language.getString("EXPORT_TO_XML_FAILED"));
                    }
                } catch (JAXBException | RepositoryException | IOException | MyFileException e) {
                    IntroController.showAlert(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage() +
                            Settings.language.getString("EXPORT_TO_XML_FAILED") + System.lineSeparator());
                }
                return null;
            }
        };
        bindTaskToUIComponents(exportXML);
        new Thread(exportXML).start();
    }

    @FXML
    private void onMenuItem_LoadXMLRepositoryClick(ActionEvent event) {
        File target = Utilities.fileChooser(Settings.language.getString("XML_FILE_REQUEST"), Settings.XML_FILE_REQUEST_TYPE, primaryStage.getScene());
        if (target != null) {
            introController.loadXMLFromFile(target, executeCommandProgressBar, (name, target1, magit) -> Platform.runLater(() -> {
                initializeTableViewCommit();
                updateBranchesMenuButton();
                commitTreePane = buildCommitTree();
            }));
        }
    }

    @FXML
    private void onMenuItem_QuitClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onMenuItem_ThemeManagerClick(ActionEvent event) {
        try {
            new SettingsUI(primaryStage, model, this, languageProperty, themeProperty, stringProperty_CurrentState);
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    @FXML
    private void onScanRepositoryButtonClick(ActionEvent event) {
        Task task = new Task() {
            @Override
            protected Void call() throws Exception {
                try {
                    updateProgress(0, 3);
                    Map<MapKeys, List<String>> data = model.showCurrentStatus();
                    Platform.runLater(() -> {
                        boolean alreadyOpenOne = false;
                        ObservableList<String> content = FXCollections.observableList(data.get(MapKeys.LIST_NEW));
                        newFilesListView.setItems(content);
                        updateProgress(1, 3);
                        if (content.size() != 0) {
                            newFileTab.setExpanded(true);
                            alreadyOpenOne = true;
                        }
                        updateProgress(2, 3);
                        content = FXCollections.observableList(data.get(MapKeys.LIST_CHANGED));
                        editedFilesListView.setItems(content);
                        if (content.size() != 0 && !alreadyOpenOne) {
                            editedFileTab.setExpanded(true);
                            alreadyOpenOne = true;
                        }

                        content = FXCollections.observableList(data.get(MapKeys.LIST_DELETED));
                        updateProgress(3, 3);
                        deletedFilesListView.setItems(content);
                        if (content.size() != 0 && !alreadyOpenOne) {
                            deletedFileTab.setExpanded(true);
                        }
                    });
                    updateMessage(Settings.language.getString("FX_SHOW_STATUS_FOUND_SOMETHING"));
                } catch (RepositoryException | MyFileException | IOException e) {
                    if (e instanceof RepositoryException) {
                        if (((RepositoryException) e).getCode() == eErrorCodes.NOTHING_TO_SEE) {
                            Platform.runLater(() -> cleanListViews());
                        }
                    }
                    Platform.runLater(() -> {
                        IntroController.showAlert(e.getMessage());
                        newFileTab.setExpanded(false);
                        if (editedFileTab.isExpanded())
                            editedFileTab.setExpanded(false);
                        if (deletedFileTab.isExpanded())
                            deletedFileTab.setExpanded(false);
                    });
                }
                return null;
            }
        };
        bindTaskToUIComponents(task);
        new Thread(task).start();
    }

    private void cleanListViews() {
        newFilesListView.setItems(null);
        deletedFilesListView.setItems(null);
        editedFilesListView.setItems(null);
    }

    @FXML
    private void onMenuItem_ChangeName_Click(ActionEvent event) {
        showPopup(stringProperty_CurrentUser, Settings.language.getString("PLEASE_ENTER_YOUR_NAME"), Settings.language.getString("USER_NAME_HINT"), null);
    }

    public void showPopup(StringProperty whatToUpdate, String question, String hint, MyBooleanProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_DIALOG_BOX);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            DialogController dialogController = loader.getController();
            dialogController.setQuestion(question);
            dialogController.setProperty(whatToUpdate);
            dialogController.setPromptText(hint);
            if (booleanProperty != null) {
                dialogController.setCheckBoxData(booleanProperty.getAdditionalData(), booleanProperty);
            }
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_DIALOG_BOX_HEIGHT);
            stage.setMinWidth(Settings.MAGIT_UI_DIALOG_BOX_WIDTH);
            stage.setResizable(false);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void setModel(Magit model) {
        this.model = model;
        commitTreePane = buildCommitTree();
    }

    public void setStringProperty_CurrentMagitState(StringProperty currentStatus) {
        this.stringProperty_CurrentState = currentStatus;
        currentStatus.addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> executeCommandProgress.setText(newValue));
        });
    }

    public void updateBranchesSecondRowData() {
        this.branchListMenuBtn.setText(model.getCurrentBranch().getName());
        this.repositoryListMenuBtn.setText(model.getRootFolder().toString());
        List<Branch> branches = model.getCurrentRepository().getActiveBranches();
        for (Branch branch : branches) {
            if (!branch.getName().equals(Settings.MAGIT_BRANCH_HEAD) && branch != model.getCurrentBranch()) {
                MenuItem anotherBranch = new MenuItem(branch.getName());
                anotherBranch.setOnAction(event -> branchFunction(branch));
                branchListMenuBtn.getItems().add(0, anotherBranch);
            }
        }
    }

    private void branchFunction(Branch branch) {
        BooleanProperty confirm = new SimpleBooleanProperty(false);
        confirm.addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                new Thread(() -> {
                    try {
                        if (!model.tryCheckout(branch.getName())) {
                            BooleanProperty forceCheckout = new SimpleBooleanProperty(false);
                            forceCheckout.addListener(((observable1, oldValue1, newValue1) -> {
                                try {
                                    model.checkout(branch.getName());
                                    Platform.runLater(this::updateBranchesMenuButton);
                                } catch (RepositoryException | IOException | MyFileException e) {
                                    IntroController.showAlert(e.getMessage());
                                }
                            }));
                            Platform.runLater(() -> confirmPopup(forceCheckout, Settings.language.getString("IGNORE_OPENED_ISSUES")));
                        }
                        Platform.runLater(this::updateBranchesMenuButton);
                    } catch (RepositoryException | IOException | MyFileException e) {
                        Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
                    }
                }).start();
            }
        }));
        confirmPopup(confirm, String.format(Settings.language.getString("CHECKOUT_QUESTION"), branch.getName()));
    }

    private void confirmPopup(BooleanProperty confirm, String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        alert.setContentText(text);
        alert.getButtonTypes().setAll(Utilities.getYesAndNoButtons());
        alert.showAndWait().ifPresent(type -> {
            if (type.getButtonData() == ButtonBar.ButtonData.YES) {
                confirm.setValue(true);
            }
        });
    }

    public void updateBranchesMenuButton() {
        branchListMenuBtn.getItems().clear();
        branchListMenuBtn.getItems().add(new SeparatorMenuItem());
        branchListMenuBtn.getItems().add(createNewBranchMenuItem);
        updateBranchesSecondRowData();
    }

    @FXML
    public void onCreateNewBranchMenuItem_Click(ActionEvent event) {
        StringProperty newBranchName = new SimpleStringProperty();
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.EMPTY_STRING)) {
                try {
                    model.tryCreateNewBranch(newValue);
                    updateBranchesMenuButton();
                } catch (RepositoryException | IOException e) {
                    IntroController.showAlert(e.getMessage());
                }
            }
        };
        newBranchName.addListener(listener);
        MyBooleanProperty booleanProperty = null;
        if (model.getRemoteRepository() != null) {
            booleanProperty = new MyBooleanProperty();
            booleanProperty.setAdditionalData(Settings.language.getString("FX_BRANCH_MANAGER_IS_REMOTE_TRACKING_BRANCH"));
            booleanProperty.addListener((observable -> {
                newBranchName.removeListener(listener);
                newBranchName.addListener((ob, oldValue, newValue) -> {
                    if (!newValue.equals(Settings.EMPTY_STRING)) {
                        MyBooleanProperty booleanProperty1 = new MyBooleanProperty();
                        SmartListener<Branch> branch = new SmartListener<>();
                        Platform.runLater(() -> selectBranch(branch, model.getCurrentRepository().getRemoteBranches(), booleanProperty1));
                        booleanProperty1.addListener((observable1 -> {
                            try {
                                model.tryCreateNewRemoteTrackingBranch(newValue, branch.getItem());
                                updateBranchesMenuButton();
                            } catch (RepositoryException | IOException e) {
                                IntroController.showAlert(e.getMessage());
                            }
                        }));
                    }
                });
            }));
        }
        showPopup(newBranchName, Settings.language.getString("PLEASE_ENTER_BRANCH_NAME"), Settings.language.getString("BRANCH_NAME_HINT"), booleanProperty);
    }

    @FXML
    private void onChangeRepositoryMenuItem_Click(ActionEvent event) {
        File selectedDirectory = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (selectedDirectory != null) {
            IntroController.loadExistsRepository(model, selectedDirectory, isRepositoryExists, this.executeCommandProgressBar);
        }
    }

    @FXML
    private void onMenuItem_CreateNewRepositoryClick(ActionEvent event) {
        File selectedDirectory = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (selectedDirectory != null) {
            IntroController.createNewRepository(selectedDirectory, model, isRepositoryExists);
        }
    }

    @FXML
    private void onMenuItem_ManageBranches_Click(ActionEvent event) {
        try {
            new BranchManagerUI(primaryStage, model, this);
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void setLanguageProperty(BooleanProperty languageProperty) {
        this.languageProperty = languageProperty;
    }

    public void mergeWindow(BlobMap[] userChoice, Map<String, BlobMap> duplicates, MergeProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_MERGE_WINDOW);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            MergeWindowController mergeWindowController = loader.getController();
            if (!mergeWindowController.setFilesToCheck(duplicates)) {
                booleanProperty.set(1);
                return;
            }
            mergeWindowController.setUserChoiceArray(userChoice);
            mergeWindowController.setCurrentUser(model.getCurrentUser());
            mergeWindowController.setStage(stage);
            mergeWindowController.setConflictFinishProperty(booleanProperty);
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_MERGE_WINDOW_HEIGHT);
            stage.setMinWidth(Settings.MAGIT_UI_MERGE_WINDOW_WIDTH);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_MERGE_WINDOW_WIDTH, Settings.MAGIT_UI_MERGE_WINDOW_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);

            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void setIntroController(IntroController introController) {
        this.introController = introController;
    }

    public BorderPane getMainBoard() {
        return mainBoard;
    }

    public Button getCompareToFatherTwoButton() {
        return compareToFatherTwoBtn;
    }

    public Button getCompareToFatherOneButton() {
        return compareToFatherOneBtn;
    }

    public TreeView<WarpBasicFile> getCommitFileTree() {
        return commitFileTree;
    }

    @FXML
    private void onMenuItemResetBranch(ActionEvent event) {
        resetBranch(model.getCurrentBranch(), true);
    }

    public void resetBranch(Branch branch, boolean doCheckout) {
        StringProperty newSHA_ONE = new SimpleStringProperty();
        newSHA_ONE.addListener(((observable, oldValue, newValue) -> new Thread(() -> {
            try {
                model.changeBranchPoint(branch, newValue, doCheckout);
                Platform.runLater(() -> {
                    initializeTableViewCommit();
                    updateTree();
                });
            } catch (RepositoryException | IOException | MyFileException e) {
                Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
            }
        }).start()));
        showPopup(newSHA_ONE,
                String.format(Settings.language.getString("CHOOSE_SHA_ONE_FOR_BRANCH"), model.getCurrentBranch()),
                Settings.language.getString("FX_HINT_ENTER_SHA_ONE"), null);
    }

    @FXML
    private void onMenuItemPush_Clicked(ActionEvent event) {

    }

    @FXML
    private void onMenuItemFetch_Clicked(ActionEvent event) {

    }

    @FXML
    private void onMenuItemPull_Clicked(ActionEvent event) {

    }

    @FXML
    void onMenuItemClone_Clicked(ActionEvent event) {
        File targetFolder = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (targetFolder != null) {
            StringProperty newName = new SimpleStringProperty();
            newName.addListener(((observable, oldValue, newValue) -> {
                try {
                    model.checkCleanDir(targetFolder.toString());
                    Task clone = new CloneTask(model, newValue, targetFolder, new File(model.getRootFolder().toString()));
                    bindTaskToUIComponents(clone);
                    new Thread(clone).start();
                } catch (RepositoryException e) {
                    IntroController.showAlert(e.getMessage());
                }
            }));
            showPopup(newName,
                    Settings.language.getString("PLEASE_ENTER_REPOSITORY_NAME"),
                    Settings.language.getString("USER_NAME_HINT"), null);
        } else {
            this.stringProperty_CurrentState.setValue(Settings.language.getString("FX_CLONE_COMMAND_CANCELED"));
        }
    }

    public void setThemeProperty(MyBooleanProperty themeProperty) {
        this.themeProperty = themeProperty;
    }

    public void updateRepositoryHistory() {
        if (model.getRemoteRepository() != null) {
            String name = model.getRemoteRepository().getCurrentRepository().getName() +
                    " (" + model.getRemoteRepository().getRootFolder().toString() + ")";
            if (name.length() > Settings.FX_MAX_NAME_OF_REMOTE_REPOSITORY) {
                name = name.substring(0,Settings.FX_MAX_NAME_OF_REMOTE_REPOSITORY);
            }
            MenuItem remoteRepository = new MenuItem(name);
            this.repositoryListMenuBtn.getItems().add(0,remoteRepository);
        }
    }
}