package controller;

import exceptions.MyFileException;
import exceptions.RepositoryException;
import exceptions.eErrorCodes;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import magit.*;
import magit.utils.MergeProperty;
import magit.utils.MyBooleanProperty;
import magit.utils.SmartListener;
import magit.utils.Utilities;
import settings.Settings;
import utils.MapKeys;
import utils.eUserMergeChoice;
import xml.basic.MagitRepository;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class Controller {
    @FXML
    private ProgressBar executeCommandProgressBar;
    @FXML
    private MenuItem menuItem_themeManager, menuItem_exportToXML,
            createNewBranchMenuItem, changeRepositoryMenuItem, menuItem_loadXMLRepository, menuItem_changeName,
            menuItem_quit, menuItem_changeRepository, menuItem_about, menuItem_createNewRepository, menuItem_manageBranches;
    @FXML
    private CheckMenuItem menuItem_animation;
    @FXML
    private ImageView refreshTable;
    @FXML
    private MenuButton repositoryListMenuBtn, branchListMenuBtn;
    @FXML
    private Button commitBtn, scanRepositoryBtn, mergeBtn;
    @FXML
    private TableView<List<String>> commitTable;
    @FXML
    private TableColumn<List<String>, String> branchCommitTableColumn, commentCommitTableColumn, dateCommitTableColumn, shaoneCommitTableColumn;
    @FXML
    private ListView<String> deletedFilesListView, editedFilesListView, newFilesListView,
            diffDetailsListView, commitDetailsListView, fileTreeDetailsListView;
    @FXML
    private Tab fileTreeTab, diffTab, commitTab;
    @FXML
    private Label executeCommandProgress, currentUser;
    @FXML
    private TextFlow commitCommentLabel;

    private Magit model;
    private StringProperty stringProperty_CurrentUser, stringProperty_CurrentState;
    private BooleanProperty isRepositoryExists = new SimpleBooleanProperty(), languageProperty;
    private Stage primaryStage;
    private boolean isAnimationTurnOn = false;

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
        this.commentCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
        this.dateCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(2)));
        this.shaoneCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(3)));
        this.commitTable.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                new Thread(() -> {
                    try {
                        Commit commit = model.getCommitData(newValue.get(3));
                        Platform.runLater(() -> {
                            commitCommentLabel.getChildren().clear();
                            updateCommitDetailsListView(commit);
                        });
                    } catch (IOException e) {
                        IntroController.showAlert(e.getMessage());
                    }
                }).start();
            }
        }));
    }

    @FXML
    private void onMergeButton_Click(ActionEvent event) {
        BooleanProperty flag = new SimpleBooleanProperty();
        SmartListener branch = new SmartListener() {
            private Branch branch;

            @Override
            public Object getItem() {
                return branch;
            }

            @Override
            public void setItem(Object item) {
                this.branch = (Branch) item;
            }
        };
        selectBranch(branch, model.getCurrentRepository().getBranches(), flag);
        flag.addListener((observable -> {
            if (branch.getItem() != null) {
                if (!branch.getItem().equals(model.getCurrentBranch())) {
                    Task merge = new MergeTask(model, (Branch) branch.getItem(), this);
                    bindTaskToUIComponents(merge);
                    new Thread(merge).start();
                } else {
                    IntroController.showAlert(Settings.language.getString("CANNOT_MERGE_BRANCH_TO_ITSELF"));
                }
            }
        }));
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
            selectController.setQuestion(Settings.language.getString("CHOOSE_BRANCH_TO_MERGE"));
            selectController.setListener(branch);
            selectController.setListForChoice(FXCollections.observableList(branches));
            selectController.setStage(stage);
            selectController.setFlag(flag);
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_SELECT_POPUP_HEIGHT + 100);
            stage.setMinWidth(Settings.MAGIT_UI_SELECT_POPUP_WIDTH + 50);
            stage.setScene(new Scene(root, Settings.MAGIT_UI_SELECT_POPUP_WIDTH, Settings.MAGIT_UI_SELECT_POPUP_HEIGHT));
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
                new MagitUI(model, primaryStage);
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
        ObservableList<String> content = FXCollections.observableList(commitData);
        commitDetailsListView.setItems(content);
        Text comment = new Text(commit.getComment());
        commitCommentLabel.getChildren().add(comment);
    }

    @FXML
    private void onCommitButtonClick(ActionEvent event) {
        StringProperty comment = new SimpleStringProperty();
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_SMART_POPUP_BOX);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            comment.addListener((observable, oldValue, newValue) -> {
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
            SmartPopUpController smartPopUpController = loader.getController();
            smartPopUpController.setStringProperty(comment);
            smartPopUpController.setTitle(Settings.language.getString("PLEASE_ENTER_YOUR_COMMENT"));
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT + 100);
            stage.setMinWidth(Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH + 50);
            stage.setScene(new Scene(root, Settings.MAGIT_UI_SMART_POPUP_MAX_WIDTH, Settings.MAGIT_UI_SMART_POPUP_MAX_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
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
        about.setScene(new Scene(root, 300, 200));
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
            IntroController.loadXMLRepository(target, model, isRepositoryExists);
        }
    }

    @FXML
    private void onMenuItem_QuitClick(ActionEvent event) {
        Platform.exit();
    }

    @FXML
    private void onMenuItem_ThemeManagerClick(ActionEvent event) {
        try {
            new SettingsUI(primaryStage, model, this, languageProperty, stringProperty_CurrentState);
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    @FXML
    private void onScanRepositoryButtonClick(ActionEvent event) {
        try {
            Map<MapKeys, List<String>> data = model.showCurrentStatus();
            ObservableList<String> content = FXCollections.observableList(data.get(MapKeys.LIST_NEW));
            newFilesListView.setItems(content);
            content = FXCollections.observableList(data.get(MapKeys.LIST_DELETED));
            deletedFilesListView.setItems(content);
            content = FXCollections.observableList(data.get(MapKeys.LIST_CHANGED));
            editedFilesListView.setItems(content);
        } catch (RepositoryException | MyFileException | IOException e) {
            if (e instanceof RepositoryException) {
                if (((RepositoryException) e).getCode() == eErrorCodes.NOTHING_TO_SEE) {
                    cleanListViews();
                }
            }
            IntroController.showAlert(e.getMessage());
        }
    }

    private void cleanListViews() {
        newFilesListView.setItems(null);
        deletedFilesListView.setItems(null);
        editedFilesListView.setItems(null);
    }

    @FXML
    private void onMenuItem_ChangeName_Click(ActionEvent event) {
        showPopup(stringProperty_CurrentUser, Settings.language.getString("PLEASE_ENTER_YOUR_NAME"), Settings.language.getString("USER_NAME_HINT"));
    }

    public void showPopup(StringProperty whatToUpdate, String question, String hint) {
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
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_DIALOG_BOX_HEIGHT);
            stage.setMinWidth(Settings.MAGIT_UI_DIALOG_BOX_WIDTH);
            stage.setResizable(false);
            stage.setScene(new Scene(root, Settings.MAGIT_UI_DIALOG_BOX_WIDTH, Settings.MAGIT_UI_DIALOG_BOX_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }

    public void setModel(Magit model) {
        this.model = model;
    }

    public void setStringProperty_CurrentMagitState(StringProperty currentStatus) {
        this.stringProperty_CurrentState = currentStatus;
        currentStatus.addListener((observable, oldValue, newValue) -> {
            this.executeCommandProgress.setText(newValue);
        });
    }

    public void updateBranchesSecondRowData() {
        this.branchListMenuBtn.setText(model.getCurrentBranch().getName());
        this.repositoryListMenuBtn.setText(model.getRootFolder().toString());
        List<Branch> branches = model.getCurrentRepository().getBranches();
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
        newBranchName.addListener(((observable, oldValue, newValue) -> {
            if (!newValue.equals(Settings.EMPTY_STRING)) {
                try {
                    model.tryCreateNewBranch(newValue);
                    updateBranchesMenuButton();
                } catch (RepositoryException | IOException e) {
                    IntroController.showAlert(e.getMessage());
                }
            }
        }));
        showPopup(newBranchName, Settings.language.getString("PLEASE_ENTER_BRANCH_NAME"), Settings.language.getString("BRANCH_NAME_HINT"));
    }

    @FXML
    private void onChangeRepositoryMenuItem_Click(ActionEvent event) {
        File selectedDirectory = Utilities.choiceFolderDialog(primaryStage.getScene());
        if (selectedDirectory != null) {
            IntroController.loadExistsRepository(model, selectedDirectory, isRepositoryExists);
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

    public void mergeWindow(MergeProperty mergeProperty, Map<eUserMergeChoice, Blob> duplicate, MyBooleanProperty booleanProperty) {
        Stage stage = new Stage();
        Pane root;
        try {
            FXMLLoader loader = new FXMLLoader();
            URL mainFXML = Controller.class.getResource(Settings.FXML_MERGE_WINDOW);
            loader.setLocation(mainFXML);
            loader.setResources(Settings.language);
            root = loader.load();
            MergeWindowController mergeWindowController = loader.getController();
            List<Blob> files = new ArrayList<>(3);
            files.add(0, duplicate.get(eUserMergeChoice.ANCESTOR));
            files.add(1, duplicate.get(eUserMergeChoice.ACTIVE));
            files.add(2, duplicate.get(eUserMergeChoice.TARGET));
            mergeWindowController.setFileText(files);
            mergeWindowController.setMergeProperty(mergeProperty);
            mergeWindowController.setStage(stage);
            mergeWindowController.setBooleanProperty(booleanProperty);
            stage.setTitle(Settings.language.getString("MAGIT_WINDOW_TITLE"));
            stage.setMinHeight(Settings.MAGIT_UI_MERGE_WINDOW_HEIGHT);
            stage.setMinWidth(Settings.MAGIT_UI_MERGE_WINDOW_WIDTH);
            stage.setScene(new Scene(root, Settings.MAGIT_UI_MERGE_WINDOW_WIDTH, Settings.MAGIT_UI_MERGE_WINDOW_HEIGHT));
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.show();
        } catch (IOException e) {
            IntroController.showAlert(e.getMessage());
        }
    }
}