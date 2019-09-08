package controller.screen.main;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.stage.Stage;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import magit.utils.CustomAnimations;
import settings.Settings;
import utils.PairBranchCommit;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class TreeController {
    private Magit model;
    private MainController mainController;
    private Map<Integer, Color> colorMap = new HashMap<>();
    private List<PairBranchCommit> timeline = new LinkedList<>();
    private Map<Commit, Node> commitNodes = new HashMap<>();
    private Map<Double, List<Node>> nodesInColumn = new HashMap<>();
    private Stage stage;
    private Map<Branch, Point> pointMap = new HashMap<>();
    private double mouseXCords, mouseYCords;

    @FXML
    private ListView<PairBranchCommit> listView;
    @FXML
    private ImageView close;
    @FXML
    private HBox topBar, hBox;

    public void setMainController(MainController mainController) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.hBox.getChildren().add(0, buildCommitTree());
    }

    public void initialize() {

        close.imageProperty().bind(
                Bindings.when(close.hoverProperty())
                        .then(new Image(Settings.FXML_CLOSE_BUTTON_HOVER_IMG))
                        .otherwise(new Image(Settings.FXML_CLOSE_BUTTON_IMG))
        );

        this.close.setOnMouseClicked((event -> stage.close()));

        topBar.setOnMousePressed((event -> {
            this.mouseXCords = event.getSceneX();
            this.mouseYCords = event.getSceneY();
        }));

        topBar.setOnMouseDragged((event -> {
            stage.setX(event.getScreenX() - mouseXCords);
            stage.setY(event.getScreenY() - mouseYCords);
        }));
    }

    public Node getListOfCommitsNodes(Commit commit) {
        return commitNodes.getOrDefault(commit, null);
    }

    private ScrollPane buildCommitTree() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPadding(new Insets(10));
        Group root = new Group();
        new Thread(() -> {
            Map<Branch, List<PairBranchCommit>> commitMap = model.getAllCommits();
            List<Node> circles = warpDrawCommitTree(commitMap);
            Platform.runLater(() -> root.getChildren().addAll(circles));
            getBranchListDetails(listView);
        }).start();
        scrollPane.setContent(root);
        root.setRotate(180); // the first one will be the newest commit
        return scrollPane;
    }

    private void getBranchListDetails(ListView<PairBranchCommit> listView) {
        Collections.reverse(timeline);
        timeline = timeline.stream().distinct().collect(Collectors.toList());
        Platform.runLater(() -> listView.getItems().addAll(timeline));
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

        for (int i = 0; current != null; i++, current = getNextCommitByTimeLine(map)) {
            timeline.add(i, current);
            if (!commitNodeMap.containsKey(current.getCommit())) {
                Point coords = new Point(pointMap.get(current.getBranch()).x, currentRow.y);
                Node circle = createCircle(coords, current.getCommit(), pointedBranch);
                currentRow.setLocation(currentRow.x, currentRow.y + Settings.COMMIT_TREE_ADD_TO_Y);
                if (!commitNodes.containsKey(current.getCommit())) {
                    commitNodes.put(current.getCommit(), circle);
                    addToColumnMap(circle, coords);
                }
                commitNodeMap.put(current.getCommit(), coords);
                nodeList.add(circle);
            }
        }

        nodeList.addAll(wireUpAllNodes(commitNodeMap, branches));
        return nodeList;
    }

    private void addToColumnMap(Node node, Point coords) {
        Double x = coords.getX();
        if (!nodesInColumn.containsKey(x)) {
            nodesInColumn.put(x, new LinkedList<>());
        }
        nodesInColumn.get(x).add(node);
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
                    addToColumnMap(line, prevCircle);
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

    private ContextMenu createContextMenu(Commit commit, Branch branch) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem createNewBranch = new MenuItem(Settings.language.getString("FX_CREATE_NEW_BRANCH")),
                resetHeadBranch = new MenuItem(Settings.language.getString("FX_MENU_RESET_BRANCH")),
                mergeToThisBranch = new MenuItem(Settings.language.getString("FX_MERGE_TO_THIS_BRANCH")),
                deletePointBranch = new MenuItem(Settings.language.getString("FX_DELETE_POINTED_BRANCH"));

        if (branch != null) {
            deletePointBranch.setDisable(false);
            mergeToThisBranch.setDisable(false);
            deletePointBranch.setOnAction(event -> mainController.deleteBranch(branch));
            mergeToThisBranch.setOnAction(event -> mainController.createMergeTask(branch));
        } else {
            mergeToThisBranch.setDisable(true);
            deletePointBranch.setDisable(true);
        }
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
        line.toBack();
        return line;
    }

    // not working this fucking useless toFront() function!
/*    private void moveLinesToBackground(List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof Circle) {
                node.toFront();
            }
        }
    }*/

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void updateTree() {
        this.hBox.getChildren().remove(0);
        this.hBox.getChildren().add(0, buildCommitTree());
    }

    public void markAllCommits(Branch branch) {
        Point point = pointMap.get(branch);
        List<Node> nodes = nodesInColumn.get(point.getX());

        for (Node node : nodes) {
            CustomAnimations.commitTreeTransition(node).play();
        }
    }
}