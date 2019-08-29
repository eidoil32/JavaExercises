package controller.screen.main;

import controller.screen.intro.IntroController;
import controller.screen.popups.DialogController;
import controller.screen.popups.MergeWindowController;
import controller.screen.popups.SelectController;
import controller.screen.popups.SmartPopUpController;
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
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.*;
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
    @FXML private BorderPane mainBoard;
    @FXML private Button commitBtn, scanRepositoryBtn, mergeBtn, compareToFatherOneBtn, compareToFatherTwoBtn;
    @FXML private CheckMenuItem menuItem_animation, menuItemShowCommitTree;
    @FXML private ImageView refreshTable;
    @FXML private Label executeCommandProgress, currentUser;
    @FXML private ListView<String> deletedFilesListView, editedFilesListView, newFilesListView,
            diffDetailsListView, commitDetailsListView;
    @FXML private Menu menuFile, menuRepository, menuTools, menuHelp;
    @FXML private MenuBar topMenuBar;
    @FXML private MenuButton repositoryListMenuBtn, branchListMenuBtn;
    @FXML private MenuItem menuItem_themeManager, menuItem_exportToXML,
            createNewBranchMenuItem, changeRepositoryMenuItem, menuItem_loadXMLRepository, menuItem_changeName,
            menuItem_quit, menuItem_changeRepository, menuItem_about, menuItem_createNewRepository, menuItem_manageBranches,
            menuItemFetch, menuItemPull, menuItemPush, menuItemMerge, menuItemCommit, menuItemResetBranch, menuItemClone;
    @FXML private ProgressBar executeCommandProgressBar;
    @FXML private Tab fileTreeTab, diffTab, commitTab;
    @FXML private TableColumn<List<String>, Date> dateCommitTableColumn;
    @FXML private TableColumn<List<String>, String> branchCommitTableColumn, commentCommitTableColumn, shaoneCommitTableColumn;
    @FXML private TableView<List<String>> commitTable;
    @FXML private TextFlow commitCommentLabel;
    @FXML private TitledPane newFileTab, deletedFileTab, editedFileTab;
    @FXML private TreeView<WarpBasicFile> commitFileTree;

    private boolean isAnimationTurnOn = false;
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
        updateCommitTree.addListener((observable -> {
            if (mainBoard.getRight() != null) {
                mainBoard.setRight(commitTreePane);
            }
        }));
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
        if (mainBoard.getRight() == null) {
            mainBoard.setRight(commitTreePane);
        } else {
            mainBoard.setRight(null);
        }
    }

    @FXML
    private void onMergeButton_Click() {
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
                    Platform.runLater(() -> checkOpenIssues.setValue(true));
                } else {
                    Platform.runLater(() -> IntroController.showAlert(Settings.language.getString("FX_MARGE_ABORT_OPEN_ISSUES")));
                }
            } catch (IOException | MyFileException | RepositoryException e) {
                Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
            }
        }).start();
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
                            updateMessage(Settings.language.getString("COMMIT_CREATED_SUCCESSFULLY"));
                            Platform.runLater(() -> {
                                mainTableController.initializeTableViewCommit(); // todo: add bind
                                openedChangesController.cleanListViews();
                                commitTreePane = treeController.buildCommitTree();
                                updateTree();
                            });
                        } catch (IOException | MyFileException | RepositoryException e) {
                            Platform.runLater(() -> IntroController.showAlert(e.getMessage()));
                        }
                        return null;
                    }
                };
                bindTaskToUIComponents(commit);
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
    private void onMenuItem_LoadXMLRepositoryClick() {
        File target = Utilities.fileChooser(Settings.language.getString("XML_FILE_REQUEST"), Settings.XML_FILE_REQUEST_TYPE, primaryStage.getScene());
        if (target != null) {
            introController.loadXMLFromFile(target, executeCommandProgressBar, (name, target1, magit) -> Platform.runLater(() -> {
                mainTableController.initializeTableViewCommit(); //todo: add bind
                updateBranchesMenuButton();
                commitTreePane = treeController.buildCommitTree();
            }));
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
            IntroController.showAlert(e.getMessage());
        }
    }

    @FXML
    private void onMenuItem_ChangeName_Click() {
        showPopup(stringProperty_CurrentUser, Settings.language.getString("PLEASE_ENTER_YOUR_NAME"), Settings.language.getString("USER_NAME_HINT"), null);
    }

    @FXML
    public void onCreateNewBranchMenuItem_Click() {
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
            IntroController.showAlert(e.getMessage());
        }
    }

    @FXML
    private void onMenuItemResetBranch() {
        resetBranch(model.getCurrentBranch(), true);
    }

    @FXML
    private void onMenuItemPush_Clicked() {

    }

    @FXML
    private void onMenuItemFetch_Clicked() {
        Task fetchTask = new FetchTask(model);
        bindTaskToUIComponents(fetchTask);
        new Thread(fetchTask).start();
    }

    @FXML
    private void onMenuItemPull_Clicked() {
        Task pullTask = new PullTask(model);
        bindTaskToUIComponents(pullTask);
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

    @FXML
    private void onScanRepositoryButtonClick() {
        openedChangesController.scanRepository();
    }

    private void selectBranch(SmartListener branch, List<Branch> branches, BooleanProperty flag) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = MainController.class.getResource(Settings.FXML_SELECT_POPUP);
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

    private void showPopup(StringProperty whatToUpdate, String question, String hint, MyBooleanProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
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

    public void resetBranch(Branch branch, boolean doCheckout) {
        StringProperty newSHA_ONE = new SimpleStringProperty();
        newSHA_ONE.addListener(((observable, oldValue, newValue) -> new Thread(() -> {
            try {
                model.changeBranchPoint(branch, newValue, doCheckout);
                Platform.runLater(() -> {
                    mainTableController.initializeTableViewCommit(); //todo: add bind
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

    public MainTableController getMainTableController() {
        return mainTableController;
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
            IntroController.showAlert(e.getMessage());
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
        treeController = new TreeController(this);
        this.commitTreePane = treeController.buildCommitTree();
    }

    public void setStringProperty_CurrentMagitState(StringProperty currentStatus) {
        this.stringProperty_CurrentState = currentStatus;
        currentStatus.addListener((observable, oldValue, newValue) -> Platform.runLater(() -> executeCommandProgress.setText(newValue)));
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
            IntroController.showAlert(e.getMessage());
        }
    }

    public void updateTree() {
        if (mainBoard.getRight() != null) {
            commitTreePane = treeController.buildCommitTree();
            mainBoard.setRight(commitTreePane);
        }
    }

    public void bindTaskToUIComponents(Task task) {
        this.executeCommandProgress.textProperty().bind(task.messageProperty());
        this.executeCommandProgressBar.progressProperty().bind(task.progressProperty());
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

    public void updateCommitDiffAndFileTree(Commit commit) {
        diffDetailsListView.getItems().clear();
        commitFileTree.setRoot(null);
        commitsDetailsController.updateCommitDetailsListView(commit);
        Task task = new CommitDataTask(model, this, commit);
        bindTaskToUIComponents(task);
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

    public TreeController getTreeController() {
        return this.treeController;
    }
}