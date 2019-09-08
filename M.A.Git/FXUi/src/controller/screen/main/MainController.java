package controller.screen.main;

import controller.screen.intro.IntroController;
import controller.screen.popups.*;
import exceptions.MyFileException;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import magit.*;
import magit.tasks.*;
import magit.utils.*;
import settings.Settings;
import utils.MapKeys;
import utils.WarpBasicFile;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainController {
    @FXML
    private BorderPane mainBoard;
    @FXML
    private Button commitBtn, scanRepositoryBtn, mergeBtn, compareToFatherOneBtn, compareToFatherTwoBtn;
    @FXML
    private CheckMenuItem menuItem_animation, menuItemShowCommitTree;
    @FXML
    private Label executeCommandProgress, currentUser, currentRepositoryName;
    @FXML
    private ListView<String> deletedFilesListView, editedFilesListView, newFilesListView,
            commitDetailsListView;
    @FXML
    private ListView<CommitsDetailsController.WarpBlob> diffDetailsListView;
    @FXML
    private Menu menuFile, menuRepository, menuTools, menuHelp;
    @FXML
    private MenuBar topMenuBar;
    @FXML
    private MenuButton repositoryListMenuBtn, branchListMenuBtn;
    @FXML
    private MenuItem menuItem_themeManager, menuItem_exportToXML,
            createNewBranchMenuItem, changeRepositoryMenuItem, menuItem_loadXMLRepository, menuItem_changeName,
            menuItem_quit, menuItem_changeRepository, menuItem_about, menuItem_createNewRepository, menuItem_manageBranches,
            menuItemFetch, menuItemPull, menuItemPush, menuItemMerge, menuItemCommit, menuItemResetBranch, menuItemClone;
    @FXML
    private ProgressBar executeCommandProgressBar;
    @FXML
    private Tab fileTreeTab, diffTab, commitTab;
    @FXML
    private TableColumn<List<String>, Date> dateCommitTableColumn;
    @FXML
    private TableColumn<List<String>, String> branchCommitTableColumn, commentCommitTableColumn, shaoneCommitTableColumn;
    @FXML
    private TableView<List<String>> commitTable;
    @FXML
    private TextFlow commitCommentLabel;
    @FXML
    private TitledPane newFileTab, deletedFileTab, editedFileTab;
    @FXML
    private TreeView<WarpBasicFile> commitFileTree;
    @FXML
    private AnchorPane bottomPane;

    private boolean isAnimationTurnOn = false, isTreeAlreadyShown = false;
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty(), languageProperty, themeProperty;
    private CommitsDetailsController commitsDetailsController;
    private IntroController introController;
    private Magit model;
    private MainTableController mainTableController;
    private MyBooleanProperty updateCommitTree = new MyBooleanProperty();
    private Node commitTreePane;
    private OpenedChangesController openedChangesController;
    private Stage primaryStage;
    private StringProperty stringProperty_CurrentUser, stringProperty_CurrentState;
    private TreeController treeController;

    @FXML
    public void initialize() {
        Tooltip currentRepositoryName = new Tooltip(), currentRepositoryLocation = new Tooltip();
        currentRepositoryName.textProperty().bind(this.currentRepositoryName.textProperty());
        Tooltip.install(this.currentRepositoryName, currentRepositoryName);
        currentRepositoryLocation.textProperty().bind(repositoryListMenuBtn.textProperty());
        Tooltip.install(repositoryListMenuBtn, currentRepositoryLocation);
        this.commitTable.getSelectionModel().
                selectedItemProperty().
                addListener(((observable, oldValue, newValue) -> {
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
                                    IntroController.showError(e.getMessage());
                                }
                                return null;
                            }
                        };
                        bindTaskToUIComponents(task, false);
                        new Thread(task).start();
                    }
                }));
        commitFileTree.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                FileViewer.showFileViewer(event, commitFileTree, getClass(), primaryStage);
            }
        });
    }

    public void refreshData() {
        mainTableController.initializeTableViewCommit();
        updateTree();
        updateBranchesMenuButton();
    }

    @FXML
    private void onNameLabel_Clicked(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                onMenuItem_ChangeName_Click();
            }
        }
    }

    @FXML
    public void onMenuItemShowCommitTree_Click() {
        if (menuItemShowCommitTree.isSelected() && !isTreeAlreadyShown) {
            isTreeAlreadyShown = true;
            Stage stage = new Stage();
            Pane root;
            try {
                FXMLLoader loader = new FXMLLoader();
                URL mainFXML = MainController.class.getResource(Settings.FXML_TREE_WINDOW);
                loader.setLocation(mainFXML);
                loader.setResources(Settings.language);
                root = loader.load();
                treeController = loader.getController();
                treeController.setMainController(this);
                treeController.setStage(stage);
                stage.setMinHeight(Settings.MAGIT_UI_TREE_WINDOW_HEIGHT);
                stage.setMinWidth(Settings.MAGIT_UI_TREE_WINDOW_WIDTH);
                Scene scene = new MyScene(root, Settings.MAGIT_UI_TREE_WINDOW_WIDTH, Settings.MAGIT_UI_TREE_WINDOW_HEIGHT);
                Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - Settings.MAGIT_UI_TREE_WINDOW_WIDTH);
                stage.setY(primaryScreenBounds.getMinY() + primaryScreenBounds.getHeight() - Settings.MAGIT_UI_TREE_WINDOW_HEIGHT);
                scene.setFill(Color.TRANSPARENT);
                stage.initStyle(StageStyle.TRANSPARENT);
                stage.setScene(scene);
                stage.show();
                stage.setOnHidden((event) -> {
                    menuItemShowCommitTree.setSelected(false);
                    isTreeAlreadyShown = false;
                });
                primaryStage.setOnHiding((event -> stage.close()));

            } catch (IOException e) {
                IntroController.showError(e.getMessage());
            }
        } else {
            treeController.getStage().close();
            isTreeAlreadyShown = false;
        }
    }

    @FXML
    private void onMergeButton_Click() {
        MyBooleanProperty checkOpenIssues = new MyBooleanProperty();
        checkOpenIssues.addListener(((o, oldValue, newValue) -> {
            if (newValue) {
                BooleanProperty flag = new SimpleBooleanProperty();
                SmartListener<Branch> branch = new SmartListener<>();
                selectBranch(branch, model.getCurrentRepository().getAllBranches(), flag);
                flag.addListener((observable -> {
                    if (branch.getItem() != null) {
                        if (!branch.getItem().equals(model.getCurrentBranch())) {
                            createMergeTask(branch.getItem());
                        } else {
                            IntroController.showError(Settings.language.getString("CANNOT_MERGE_BRANCH_TO_ITSELF"));
                        }
                    }
                }));
            }
        }));
        new Thread(() -> {
            try {
                boolean val = model.getCurrentRepository().scanRepository(model.getCurrentUser()) == null;
                if (val) {
                    Platform.runLater(() -> checkOpenIssues.setValue(true));
                } else {
                    Platform.runLater(() -> IntroController.showError(Settings.language.getString("FX_MARGE_ABORT_OPEN_ISSUES")));
                }
            } catch (IOException | MyFileException | RepositoryException e) {
                Platform.runLater(() -> IntroController.showError(e.getMessage()));
            }
        }).start();
    }

    public void createMergeTask(Branch branch) {
        Task merge = new MergeTask(model, branch, this);
        bindTaskToUIComponents(merge, true);
        new Thread(merge).start();
    }

    @FXML
    private void onCommitButtonClick() {
        commitCommentPopup((observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.CANCEL_BTN_CLICKED_STRING)) {
                Task commit = new Task<Void>() {
                    @Override
                    protected Void call() {
                        try {
                            updateProgress(0, 1);
                            model.commitMagit(stringProperty_CurrentUser.get(), newValue);
                            updateProgress(1, 1);
                            new Thread(() -> {
                                boolean status = false;
                                while (!status) {
                                    status = animationOnAddingNewCommit();
                                }
                            }).start();
                            updateMessage(Settings.language.getString("COMMIT_CREATED_SUCCESSFULLY"));
                        } catch (IOException | MyFileException | RepositoryException e) {
                            Platform.runLater(() -> IntroController.showError(e.getMessage()));
                        }
                        return null;
                    }
                };
                bindTaskToUIComponents(commit, true);
                new Thread(commit).start();
            }
        });
    }

    @FXML
    private void onMenuItem_AboutClick() {
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
    private void onMenuItem_AnimationClick() {
        this.isAnimationTurnOn = !isAnimationTurnOn;
        Settings.ANIMATION_DURATION = Duration.millis(isAnimationTurnOn ? Settings.ANIMATION_DURATION_TWO_SECONDS : 0);
        updateMessage(Settings.language.getString(isAnimationTurnOn ? "FX_ANIMATION_IS_TURN_ON" : "FX_ANIMATION_IS_TURN_OFF"));
    }

    @FXML
    private void onMenuItem_ExportToXMLClick() {
        final File target = Utilities.choiceFolderDialog(primaryStage.getScene());
        Task exportXML = new Task() {
            @Override
            protected Object call() {
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
                        Platform.runLater(() -> IntroController.showError(Settings.language.getString("EXPORT_TO_XML_CANCELED")));
                        updateMessage(Settings.language.getString("EXPORT_TO_XML_FAILED"));
                    }
                } catch (JAXBException | RepositoryException | IOException | MyFileException e) {
                    IntroController.showError(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage() +
                            Settings.language.getString("EXPORT_TO_XML_FAILED") + System.lineSeparator());
                }
                return null;
            }
        };
        bindTaskToUIComponents(exportXML, true);
        new Thread(exportXML).start();
    }

    @FXML
    private void onMenuItem_LoadXMLRepositoryClick() {
        File target = Utilities.fileChooser(Settings.language.getString("XML_FILE_REQUEST"), Settings.XML_FILE_REQUEST_TYPE, primaryStage.getScene());
        if (target != null) {
            introController.loadXMLFromFile(target, executeCommandProgressBar, (name, target1, magit) -> refreshData());
        }
    }

    @FXML
    private void onMenuItem_QuitClick() {
        Platform.exit();
    }

    @FXML
    private void onMenuItem_ThemeManagerClick() {
        try {
            new SettingsUI(primaryStage, model, this, languageProperty, themeProperty, stringProperty_CurrentState);
        } catch (IOException e) {
            IntroController.showError(e.getMessage());
        }
    }

    @FXML
    private void onMenuItem_ChangeName_Click() {
        showPopup(stringProperty_CurrentUser, Settings.language.getString("PLEASE_ENTER_YOUR_NAME"), Settings.language.getString("USER_NAME_HINT"), null);
    }

    public void createNewBranchPointedToCommit(Commit commit) {
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.EMPTY_STRING)) {
                try {
                    model.tryCreateNewBranch(newValue, commit);
                    refreshData();
                } catch (RepositoryException | IOException e) {
                    IntroController.showError(e.getMessage());
                }
            }
        };
        createNewBranch(listener);
    }

    @FXML
    public void onCreateNewBranchMenuItem_Click() {
        ChangeListener<String> listener = (observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.EMPTY_STRING)) {
                SmartListener<Commit> commit = new SmartListener<>();
                BooleanProperty commitProperty = new MyBooleanProperty();
                commitProperty.addListener(observable1 -> {
                    try {
                        model.tryCreateNewBranch(newValue, commit.getItem());
                        refreshData();
                    } catch (RepositoryException | IOException e) {
                        IntroController.showError(e.getMessage());
                    }
                });
                Platform.runLater(() -> selectCommit(commit, model.getCommitList(), commitProperty));
            }
        };
        createNewBranch(listener);
    }

    private void createNewBranch(ChangeListener<String> listener) {
        StringProperty newBranchName = new SimpleStringProperty();
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
                                refreshData();
                            } catch (RepositoryException | IOException e) {
                                IntroController.showError(e.getMessage());
                            }
                        }));
                    }
                });
            }));
        }
        showPopup(newBranchName, Settings.language.getString("PLEASE_ENTER_BRANCH_NAME"), Settings.language.getString("BRANCH_NAME_HINT"), booleanProperty);
    }

    @FXML
    private void onChangeRepositoryMenuItem_Click() {
        File selectedDirectory = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (selectedDirectory != null) {
            IntroController.loadExistsRepository(model, selectedDirectory, isRepositoryExists, this.executeCommandProgressBar);
        }
    }

    @FXML
    private void onMenuItem_CreateNewRepositoryClick() {
        File selectedDirectory = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (selectedDirectory != null) {
            IntroController.createNewRepository(selectedDirectory, model, isRepositoryExists);
        }
    }

    @FXML
    private void onMenuItem_ManageBranches_Click() {
        try {
            new BranchManagerUI(primaryStage, model, this);
        } catch (IOException e) {
            IntroController.showError(e.getMessage());
        }
    }

    @FXML
    private void onMenuItemResetBranch() {
        resetBranch(model.getCurrentBranch(), true);
    }

    @FXML
    private void onMenuItemPush_Clicked() {
        Task pushTask = new PushTask(model);
        bindTaskToUIComponents(pushTask, true);
        new Thread(pushTask).start();
    }

    @FXML
    private void onMenuItemFetch_Clicked() {
        Task fetchTask = new FetchTask(model);
        bindTaskToUIComponents(fetchTask, true);
        new Thread(fetchTask).start();
    }

    @FXML
    private void onMenuItemPull_Clicked() {
        Task pullTask = new PullTask(model);
        bindTaskToUIComponents(pullTask, true);
        new Thread(pullTask).start();
    }

    @FXML
    private void onMenuItemClone_Clicked() {
        File targetFolder = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (targetFolder != null) {
            StringProperty newName = new SimpleStringProperty();
            newName.addListener(((observable, oldValue, newValue) -> {
                try {
                    model.checkCleanDir(targetFolder.toString());
                    Task clone = new CloneTask(model, newValue, targetFolder, new File(model.getRootFolder().toString()));
                    bindTaskToUIComponents(clone, true);
                    new Thread(clone).start();
                } catch (RepositoryException e) {
                    IntroController.showError(e.getMessage());
                }
            }));
            showPopup(newName,
                    Settings.language.getString("PLEASE_ENTER_REPOSITORY_NAME"),
                    Settings.language.getString("USER_NAME_HINT"), null);
        } else {
            this.stringProperty_CurrentState.setValue(Settings.language.getString("FX_CLONE_COMMAND_CANCELED"));
        }
    }

    @FXML
    private void onScanRepositoryButtonClick() {
        openedChangesController.scanRepository();
    }

    private void selectBranch(SmartListener branch, List<Branch> branches, BooleanProperty flag) {
        try {
            ObservableList<Object> list = FXCollections.observableArrayList();
            for (Branch branch1 : branches) {
                if (!branch1.isHead())
                    list.add(branch1);
            }
            showSelectPopup(branch, list, flag, Settings.language.getString("FX_CHOOSE_BRANCH"));
        } catch (IOException e) {
            IntroController.showError(e.getMessage());
        }
    }

    private void selectCommit(SmartListener commit, List<Commit> commits, BooleanProperty flag) {
        try {
            ObservableList<Object> list = FXCollections.observableArrayList();
            list.addAll(commits);
            commit.setConverter(new StringConverter<Object>() {
                @Override
                public String toString(Object object) {
                    return ((Commit) object).getSHA_ONE();
                }

                @Override
                public Object fromString(String string) {
                    return list.stream().filter(o -> ((Commit) o).getSHA_ONE().equals(string)).findFirst();
                }
            });
            showSelectPopup(commit, list, flag, Settings.language.getString("FX_CHOOSE_POINTED_COMMIT"));
        } catch (IOException e) {
            IntroController.showError(e.getMessage());
        }
    }

    private void showSelectPopup(SmartListener smartListener, ObservableList<Object> observableList, BooleanProperty flag, String question) throws IOException {
        Stage stage = new Stage();
        Pane root;
        FXMLLoader loader = new FXMLLoader();
        URL mainFXML = MainController.class.getResource(Settings.FXML_SELECT_POPUP);
        loader.setLocation(mainFXML);
        loader.setResources(Settings.language);
        root = loader.load();
        SelectController selectController = loader.getController();
        selectController.setQuestion(question);
        selectController.setListener(smartListener);
        selectController.setListForChoice(observableList);
        selectController.setStage(stage);
        selectController.setFlag(flag);
        StringConverter<Object> converter = smartListener.getConverter();
        if (converter != null) {
            selectController.setConverter(converter);
        }
        stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
        stage.setMinHeight(Settings.MAGIT_UI_SELECT_POPUP_HEIGHT + 50);
        stage.setMinWidth(Settings.MAGIT_UI_SELECT_POPUP_WIDTH + 50);
        stage.setScene(new MyScene(root, Settings.MAGIT_UI_SELECT_POPUP_WIDTH, Settings.MAGIT_UI_SELECT_POPUP_HEIGHT));
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.show();
    }

    private void showPopup(StringProperty whatToUpdate, String question, String hint, MyBooleanProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
            int smaller = 0;
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = MainController.class.getResource(Settings.FXML_DIALOG_BOX);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            DialogController dialogController = loader.getController();
            dialogController.setQuestion(question);
            dialogController.setProperty(whatToUpdate);
            dialogController.setPromptText(hint);
            if (booleanProperty != null) {
                dialogController.setCheckBoxData(booleanProperty.getAdditionalData(), booleanProperty);
            } else {
                smaller = -50;
            }
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_DIALOG_BOX_HEIGHT + smaller);
            stage.setMinWidth(Settings.MAGIT_UI_DIALOG_BOX_WIDTH);
            stage.setResizable(false);
            stage.setScene(new MyScene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT + smaller));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showError(e.getMessage());
        }
    }

    public void resetBranch(Branch branch, boolean doCheckout) {
        StringProperty newSHA_ONE = new SimpleStringProperty();
        newSHA_ONE.addListener(
                ((observable, oldValue, newValue) -> new Thread(
                        () -> resetBranchFunction(branch, doCheckout, newValue)).start()));
        showPopup(newSHA_ONE,
                String.format(Settings.language.getString("CHOOSE_SHA_ONE_FOR_BRANCH"), model.getCurrentBranch()),
                Settings.language.getString("FX_HINT_ENTER_SHA_ONE"), null);
    }

    public void resetBranchFunction(Branch branch, boolean doCheckout, String sha_one) {
        try {
            model.changeBranchPoint(branch, sha_one, doCheckout);
            Platform.runLater(this::refreshData);
        } catch (RepositoryException | IOException | MyFileException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
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
                name = name.substring(0, Settings.FX_MAX_NAME_OF_REMOTE_REPOSITORY);
            }
            MenuItem remoteRepository = new MenuItem(name);
            this.repositoryListMenuBtn.getItems().add(0, remoteRepository);
        }
    }

    public Magit getModel() {
        return model;
    }

    public TableView<List<String>> getTableView() {
        return commitTable;
    }

    public TitledPane getFilesTab(int key) {
        switch (key) {
            case Settings.FX_DELETED_TAB_KEY:
                return deletedFileTab;
            case Settings.FX_NEW_TAB_KEY:
                return newFileTab;
            case Settings.FX_EDIT_TAB_KEY:
                return editedFileTab;
        }
        return null;
    }

    public void setLanguageProperty(BooleanProperty languageProperty) {
        this.languageProperty = languageProperty;
    }

    public void mergeWindow(BlobMap[] userChoice, Map<String, BlobMap> duplicates, MergeProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = MainController.class.getResource(Settings.FXML_MERGE_WINDOW);
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
            IntroController.showError(e.getMessage());
        }
    }

    public void setIntroController(IntroController introController) {
        this.introController = introController;
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

    public void setModel(Magit model) {
        this.model = model;
        commitsDetailsController = new CommitsDetailsController(this, commitCommentLabel, diffDetailsListView, commitDetailsListView);
        mainTableController = new MainTableController(this,
                dateCommitTableColumn, branchCommitTableColumn, commentCommitTableColumn, shaoneCommitTableColumn);
        openedChangesController = new OpenedChangesController(this, newFilesListView, deletedFilesListView, editedFilesListView);
        //this.commitTreePane = treeController.buildCommitTree();
        this.currentRepositoryName.textProperty().setValue(model.getCurrentRepository().getName());
    }

    public void setStringProperty_CurrentMagitState(StringProperty currentStatus) {
        this.stringProperty_CurrentState = currentStatus;
        currentStatus.addListener(((observable, oldValue, newValue) -> Platform.runLater(() -> executeCommandProgress.setText(newValue))));
        //currentStatus.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> stringProperty_CurrentState.setValue(newValue)));
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
                                } catch (RepositoryException | IOException | MyFileException e) {
                                    IntroController.showError(e.getMessage());
                                }
                            }));
                            Platform.runLater(() -> confirmPopup(forceCheckout, Settings.language.getString("IGNORE_OPENED_ISSUES")));
                        }
                        Platform.runLater(this::refreshData);
                    } catch (RepositoryException | IOException | MyFileException e) {
                        Platform.runLater(() -> IntroController.showError(e.getMessage()));
                    }
                }).start();
            }
        }));
        confirmPopup(confirm, String.format(Settings.language.getString("CHECKOUT_QUESTION"), branch.getName()));
    }

    private void confirmPopup(BooleanProperty confirm, String text) {
        Utilities.customAlert(Alert.AlertType.WARNING,
                type -> {
                    if (type.getButtonData() == ButtonBar.ButtonData.YES) confirm.setValue(true);
                },
                Utilities.getYesAndNoButtons(),
                Settings.language.getString("MAGIT_WINDOW_TITLE"),
                text);
    }

    private boolean animationOnAddingNewCommit() {
        boolean isActivated = false;
        Commit lastCommit = model.getCurrentBranch().getCommit();
        Node node = treeController.getListOfCommitsNodes(lastCommit);

        if (node != null) {
            Circle circle = (Circle) node;
            CustomAnimations.fillTransition(circle).play();
            isActivated = true;
        }

        return isActivated;
    }

    private void updateBranchesMenuButton() {
        branchListMenuBtn.getItems().clear();
        branchListMenuBtn.getItems().add(new SeparatorMenuItem());
        branchListMenuBtn.getItems().add(createNewBranchMenuItem);
        updateBranchesSecondRowData();
    }

    public void commitCommentPopup(ChangeListener<String> command) {
        StringProperty comment = new SimpleStringProperty();
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = MainController.class.getResource(Settings.FXML_SMART_POPUP_BOX);
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
            IntroController.showError(e.getMessage());
        }
    }

    private void updateTree() {
        if (isTreeAlreadyShown)
            treeController.updateTree();
    }

    public void bindTaskToUIComponents(Task task, boolean includeUpdate) {
        this.executeCommandProgress.textProperty().bind(task.messageProperty());
        this.executeCommandProgressBar.progressProperty().bind(task.progressProperty());
        if (includeUpdate) {
            task.setOnSucceeded((event -> refreshData()));
        }
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        this.bottomPane.minHeightProperty().bind(primaryStage.heightProperty().multiply(0.3));
        this.isRepositoryExists.addListener((observable, oldValue, newValue) -> {
            try {
                new MagitUI(model, primaryStage, introController);
            } catch (IOException e) {
                IntroController.showError(Settings.language.getString("UNKNOWN_FATAL_ERROR") + e.getMessage());
            }
        });
    }

    public void updateCommitDiffAndFileTree(Commit commit) {
        diffDetailsListView.getItems().clear();
        commitFileTree.setRoot(null);
        commitsDetailsController.updateCommitDetailsListView(commit);
        Task task = new CommitDataTask(model, this, commit);
        bindTaskToUIComponents(task, false);
        new Thread(task).start();
    }

    public void updateButtonData(BlobMap anotherPrevMap, BlobMap map, Button button, Commit prevCommit) throws RepositoryException {
        if (prevCommit != null) {
            Map<MapKeys, List<BasicFile>> anotherPrevToCurrent = model.getCurrentRepository().createMapForScanning();
            model.getCurrentRepository().scanBetweenMaps(map.getMap(), anotherPrevMap.getMap(), anotherPrevToCurrent);
            model.getCurrentRepository().scanForDeletedFiles(map.getMap(), anotherPrevMap.getMap(), anotherPrevToCurrent.get(MapKeys.LIST_DELETED));
            button.setOnAction(event -> commitsDetailsController.updateDiffListView(anotherPrevToCurrent));
            button.setDisable(false);
            commitsDetailsController.updateDiffListView(anotherPrevToCurrent);
        } else {
            button.setDisable(true);
        }
    }

    public void setStringProperty_CurrentUser(StringProperty currentUser) {
        this.stringProperty_CurrentUser = currentUser;
        currentUser.setValue(currentUser.getValue());
        stringProperty_CurrentUser.addListener((observable, oldValue, newValue) -> this.currentUser.setText(newValue));
    }

    public void deleteBranch(Branch branch) {
        try {
            model.deleteBranch(branch.getName());
            refreshData();
            Platform.runLater(() -> updateMessage(Settings.language.getString("BRANCH_DELETE_SUCCESSFULLY")));
        } catch (RepositoryException e) {
            Platform.runLater(() -> IntroController.showError(e.getMessage()));
        }
    }

    private void updateMessage(String message) {
        this.stringProperty_CurrentState.setValue(message);
    }

    public void markBranchInTree(Branch branch) {
        if (isTreeAlreadyShown) {
            treeController.markAllCommits(branch);
        }
    }
}