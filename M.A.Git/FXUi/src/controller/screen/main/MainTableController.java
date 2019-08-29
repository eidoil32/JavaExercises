package controller.screen.main;

import controller.screen.intro.IntroController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import settings.Settings;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainTableController {
    private Magit model;
    private MainController mainController;
    private TableColumn<List<String>, Date> dateCommitTableColumn;
    private TableColumn<List<String>, String> branchCommitTableColumn, commentCommitTableColumn, shaoneCommitTableColumn;
    private TableView<List<String>> commitTable;

    @SafeVarargs
    public MainTableController(MainController mainController, TableColumn<List<String>, Date> dateColumn, TableColumn<List<String>, String>... columns) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.dateCommitTableColumn = dateColumn;
        this.branchCommitTableColumn = columns[0];
        this.commentCommitTableColumn = columns[1];
        this.shaoneCommitTableColumn = columns[2];
        this.commitTable = mainController.getTableView();
        initializeTableColumns();
        initializeTableViewCommit();
    }

    private void initializeTableColumns() {
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
        this.shaoneCommitTableColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(3)));
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
}