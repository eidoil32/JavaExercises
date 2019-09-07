package controller.screen.main;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import settings.Settings;
import utils.PairBranchCommit;

import java.awt.*;
import java.util.List;
import java.util.*;

public class TreeController {
    private Magit model;
    private MainController mainController;
    private Map<Integer, Color> colorMap = new HashMap<>();
    private List<Node> labelList = new LinkedList<>();
    private List<PairBranchCommit> timeline = new LinkedList<>();
    private Map<Commit, List<Node>> commitNodes = new HashMap<>();

    public TreeController(MainController mainController) {
        this.model = mainController.getModel();
        this.mainController = mainController;
    }

    public List<Node> getListOfCommitsNodes(Commit commit) {
        return commitNodes.getOrDefault(commit, null);
    }

    public ScrollPane buildCommitTree() {
        ScrollPane scrollPane = new ScrollPane();
        HBox root = new HBox();
        VBox vBox = new VBox();
        HBox.setHgrow(vBox, Priority.ALWAYS);
        vBox.getStyleClass().add(Settings.CSS_TREE_VBOX_CLASS);
        vBox.setAlignment(Pos.CENTER);
        Group group = new Group();
        vBox.setPadding(new Insets(10));
        ListView<String> branchDetails = new ListView<>();
        branchDetails.getStyleClass().add(Settings.COMMIT_TREE_LISTVIEW_CSS);
        Button button = createShowMoreButton(branchDetails);
        vBox.getChildren().addAll(group, button);
        branchDetails.setPadding(new Insets(50,0,0,0));
        root.getChildren().addAll(vBox, branchDetails);
        new Thread(() -> {
            Map<Branch, List<PairBranchCommit>> commitMap = model.getAllCommits();
            List<Node> circles = warpDrawCommitTree(commitMap);
            Platform.runLater(() -> group.getChildren().addAll(circles));
            Platform.runLater(() -> group.getChildren().addAll(labelList));
            getBranchListDetails(branchDetails);
        }).start();
        double size = model.getCurrentRepository().getAllBranches().size();
        size = (Settings.COMMIT_CIRCLE_RADIUS + Settings.COMMIT_SPACE_BETWEEN_CIRCLES) * size; //each branch take column width radius + 10 (for spaces)

        scrollPane.setContent(root);
        scrollPane.setMinWidth(size > 200 ? size : 200);
        int sceneWidth = 300;
        scrollPane.setMaxWidth(Math.min(sceneWidth, scrollPane.getMinWidth()));
        scrollPane.setFitToWidth(true);
        vBox.setRotate(180); // the first one will be the newest commit
        return scrollPane;
    }

    private Button createShowMoreButton(ListView<String> branchDetails) {
        Button button = new Button(Settings.language.getString("FX_TREE_BUTTON_SHOW_DETAILS"));
        ;
        button.setOnAction(e -> branchDetails.setVisible(!branchDetails.isVisible()));
        button.setRotate(180);
        button.getStyleClass().add(Settings.COMMIT_TREE_HIDE_SHOW_BUTTON);

        return button;
    }

    private void getBranchListDetails(ListView<String> listView) {
        List<String> list = new LinkedList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = timeline.size() - 1; i >= 0; i--, stringBuilder.setLength(0)) {
            PairBranchCommit current = timeline.get(i);
            stringBuilder.append("[").append(current.getBranch().getName()).append("] ");
            stringBuilder.append(current.getCommit().getSHA_ONE(), 0, 6).append(" ");
            String comment = current.getCommit().getComment();
            stringBuilder.append(comment, 0, Math.min(comment.length(), 10)).append(" - ");
            stringBuilder.append(current.getCommit().getCreator());
            list.add(stringBuilder.toString());
        }

        Platform.runLater(() -> listView.getItems().addAll(list));
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

    private Color nextColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    private List<Node> warpDrawCommitTree(Map<Branch, List<PairBranchCommit>> map) {
        PairBranchCommit current = getNextCommitByTimeLine(map);
        List<Node> nodeList = new LinkedList<>();
        Map<Commit, Point> commitNodeMap = new HashMap<>();
        Map<Branch, Point> pointMap = new HashMap<>();
        Map<Commit, Branch> pointedBranch = model.getPointedBranchesToCommitsMap();
        List<Branch> branches = model.getCurrentRepository().getAllBranches();

        int x = Settings.COMMIT_TREE_START_X, y = Settings.COMMIT_TREE_START_Y;

        for (Branch branch : branches) {
            pointMap.put(branch, new Point(x, y));
            if (!colorMap.containsKey(x)) {
                colorMap.put(x, nextColor());
            }
            x += (int) Settings.COMMIT_SPACE_BETWEEN_CIRCLES;
        }

        Point currentRow = new Point(Settings.COMMIT_TREE_START_X, Settings.COMMIT_TREE_START_Y);

        int i = 0;

        while (current != null) {
            timeline.add(i++, current);
            if (!commitNodeMap.containsKey(current.getCommit())) {
                Point coords = new Point(pointMap.get(current.getBranch()).x, currentRow.y);
/*                if (pointedBranch.containsKey(current.getCommit())) {
                    Node branchLabel = createBranchLabel(coords, current);
                    labelList.add(branchLabel);
                }*/
                Node circle = createCircle(coords, current.getCommit(), pointedBranch);
                currentRow.setLocation(currentRow.x, currentRow.y + Settings.COMMIT_TREE_ADD_TO_Y);
                if (!commitNodes.containsKey(current.getCommit())) {
                    List<Node> tempList = new LinkedList<>();
                    tempList.add(circle);
                    commitNodes.put(current.getCommit(), tempList);
                } else {
                    commitNodes.get(current.getCommit()).add(circle);
                }
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
            if (!branch.isHead()) {
                Commit firstCommit = branch.getCommit();
                nodeList.addAll(createLines(commitNodeMap, firstCommit));
            }
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
                    CubicCurve line = createLine(coords, prevCircle);
                    nodeList.add(line);
                    nodeList.addAll(createLines(commitNodeMap, current));
                }
            }
        }

        return nodeList;
    }

    private Color colorMapByX(int srcX) {
        return colorMap.get(srcX);
    }

    private Node createCircle(Point coordinates, Commit data, Map<Commit, Branch> pointedBranches) {
        Circle circle = new Circle(coordinates.getX(), coordinates.getY(), Settings.COMMIT_CIRCLE_RADIUS);
        circle.setOnMouseEntered(event -> {
            ((Circle) event.getSource()).toFront();
            circle.setFill(Settings.getBrighter(Color.valueOf(circle.getFill().toString())));
        });
        circle.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);
        circle.setOnMouseExited(event -> circle.setFill(colorMapByX(coordinates.x)));
        circle.setFill(colorMapByX(coordinates.x));
        ContextMenu contextMenu = createContextMenu(data, pointedBranches.get(data));
        circle.setOnMouseClicked((event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(circle, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY) {
                mainController.updateCommitDiffAndFileTree(data);
            }
        }));
        return circle;
    }

    private Node addBranchLabel(Circle circle, Point coords, Commit data, Branch branch) {
        String branchName = branch.getName(), firstLetter = Character.valueOf(branchName.charAt(0)).toString();

        Label label = new Label(firstLetter);
        if (colorMapByX(coords.x).getBrightness() < 40) {
            label.setTextFill(Color.WHITE);
        }
        label.setAlignment(Pos.TOP_LEFT);
        label.setRotate(180);
        StackPane stackPane = new StackPane();
        stackPane.setLayoutX(coords.getX());
        stackPane.setLayoutY(coords.getY());
        stackPane.setOnMouseEntered((event) -> {
            label.setText(branchName);
        });
        stackPane.setOnMouseExited((event -> {
            label.setText(firstLetter);
        }));
        stackPane.getChildren().addAll(circle, label);
        return stackPane;
    }

    private ContextMenu createContextMenu(Commit commit, Branch branch) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem createNewBranch = new MenuItem(Settings.language.getString("FX_CREATE_NEW_BRANCH")),
                resetHeadBranch = new MenuItem(Settings.language.getString("FX_MENU_RESET_BRANCH")),
                mergeToThisBranch = new MenuItem(Settings.language.getString("FX_MERGE_TO_THIS_BRANCH")),
                deletePointBranch = new MenuItem(Settings.language.getString("FX_DELETE_POINTED_BRANCH"));

        if (branch != null) {
            deletePointBranch.setDisable(false);
            mergeToThisBranch.setDisable(false);
        } else {
            mergeToThisBranch.setDisable(true);
            deletePointBranch.setDisable(true);
        }

        deletePointBranch.setOnAction(event -> mainController.deleteBranch(branch));
        mergeToThisBranch.setOnAction(event -> mainController.createMergeTask(branch));
        createNewBranch.setOnAction(event -> mainController.createNewBranchPointedToCommit(commit));
        resetHeadBranch.setOnAction(event -> mainController.resetBranchFunction(model.getCurrentBranch(), true, commit.getSHA_ONE()));

        contextMenu.getItems().addAll(createNewBranch, resetHeadBranch, mergeToThisBranch, deletePointBranch);
        return contextMenu;
    }

    private CubicCurve createLine(Point src, Point dest) {
        CubicCurve line = new CubicCurve(
                src.x, src.y,
                src.x, dest.y,
                src.x, dest.y,
                dest.x, dest.y);
        line.setStroke(colorMapByX(src.x));
        line.setStrokeWidth(4);
        line.setFill(null);
        return line;
    }

}
