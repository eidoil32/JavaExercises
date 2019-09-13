package controller.screen.main;

import controller.screen.intro.IntroController;
import exceptions.RepositoryException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.stage.Stage;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import magit.utils.CustomAnimations;
import magit.utils.Utilities;
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
    private boolean expandedTree;

    @FXML
    private ImageView close, expandTree;
    @FXML
    private HBox topBar, branchTable;
    @FXML
    private AnchorPane left, right;

    public void setMainController(MainController mainController) {
        this.model = mainController.getModel();
        this.mainController = mainController;
        this.left.getChildren().add(0, buildCommitTree(expandedTree));
    }

    private void setExpandedTree(boolean expandedTree) {
        this.expandedTree = expandedTree;
    }

    public void initialize() {
        close.imageProperty().bind(
                Bindings.when(close.hoverProperty())
                        .then(new Image(Settings.FXML_CLOSE_BUTTON_HOVER_IMG))
                        .otherwise(new Image(Settings.FXML_CLOSE_BUTTON_IMG))
        );

        expandTree.imageProperty().bind(
                Bindings.when(expandTree.hoverProperty())
                        .then(new Image(Settings.FXML_EXPAND_BUTTON_HOVER_IMG))
                        .otherwise(new Image(Settings.FXML_EXPAND_BUTTON_IMG))
        );

        close.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);
        expandTree.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);

        expandTree.setOnMouseClicked(event -> {
            setExpandedTree(!expandedTree);
            timeline = new LinkedList<>();
            updateTree();
        });

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

    private ScrollPane buildCommitTree(boolean isExtendedTree) {
        ScrollPane scrollPane = customScrollPane();
        scrollPane.setHvalue(scrollPane.getHvalue());
        Group root = new Group();
        new Thread(() -> {
            try {
                Map<Branch, List<PairBranchCommit>> commitMap = model.getAllCommits(isExtendedTree);
                List<Node> circles = warpDrawCommitTree(commitMap);
                Platform.runLater(() -> root.getChildren().addAll(circles));
                if (isExtendedTree) {
                    model.deleteBranch(Settings.TEMP_BRANCH_FOR_EXPANDED_TREE);
                }
                getBranchListDetails();
            } catch (RepositoryException e) {
                Platform.runLater(() -> IntroController.showError(e.getMessage()));
            }
        }).start();
        scrollPane.setContent(root);
        root.setRotate(180); // the first one will be the newest commit
        return scrollPane;
    }

    private void getBranchListDetails() {
        ScrollPane scrollPane = customScrollPane();
        VBox root = new VBox();
        scrollPane.setContent(root);
        scrollPane.setPadding(new Insets(-5, 0, 0, 0));
        timeline.sort((o1, o2) -> o2.getCommit().getDate().compareTo(o1.getCommit().getDate()));
        timeline = timeline.stream().distinct().collect(Collectors.toList());
        for (int i = 10, j = 0; j < timeline.size(); i += Settings.COMMIT_TREE_ADD_TO_Y, j++) {
            Node temp = createBlock(timeline.get(j));
            root.getChildren().add(temp);
        }

        Platform.runLater(() -> right.getChildren().add(scrollPane));
    }

    private Node createBlock(PairBranchCommit pairBranchCommit) {
        AnchorPane anchorPane = new AnchorPane();
        Label label = new Label(pairBranchCommit.toString());
        anchorPane.getChildren().add(label);
        AnchorPane.setLeftAnchor(label, 3.0);
        AnchorPane.setTopAnchor(label, 0.0);
        AnchorPane.setBottomAnchor(label, 0.0);
        anchorPane.setPrefWidth(Settings.COMMIT_TREE_RECTANGLE_WIDTH_DETAILS);
        anchorPane.setPrefHeight(Settings.COMMIT_TREE_RECTANGLE_HEIGHT_DETAILS);

        label.setId(Settings.CSS_COMMIT_TREE_LABEL);
        anchorPane.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);
        return anchorPane;
    }

    private ScrollPane customScrollPane() {
        ScrollPane scrollPane = new ScrollPane();
        AnchorPane.setBottomAnchor(scrollPane, 0.0);
        AnchorPane.setTopAnchor(scrollPane, 0.0);
        AnchorPane.setLeftAnchor(scrollPane, 0.0);
        AnchorPane.setRightAnchor(scrollPane, 0.0);
        scrollPane.setPadding(new Insets(10));

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

    private Color nextColor() {
        Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);
        return Color.rgb(red, green, blue);
    }

    private List<Node> warpDrawCommitTree(Map<Branch, List<PairBranchCommit>> map) {
        PairBranchCommit current = getNextCommitByTimeLine(map);
        List<Node> nodeList = new LinkedList<>(), tempList = new LinkedList<>();
        Map<Commit, Point> commitNodeMap = new HashMap<>();
        Map<Commit, Branch> pointedBranch = model.getPointedBranchesToCommitsMap();
        List<Branch> branches = model.getCurrentRepository().getAllBranches(expandedTree);

        int x = Settings.COMMIT_TREE_START_X, y = Settings.COMMIT_TREE_START_Y;

        createBranchPointAndColorMap(branches, x, y);

        Platform.runLater(() -> {
            branchTable.getChildren().clear();
            branchTable.getChildren().addAll(createBranchLegend());
        });

        Point currentRow = new Point(Settings.COMMIT_TREE_START_X, Settings.COMMIT_TREE_START_Y);

        for (int i = 0; current != null; i++, current = getNextCommitByTimeLine(map)) {
            timeline.add(i, current);
            if (!commitNodeMap.containsKey(current.getCommit())) {
                Point coords = new Point(pointMap.get(current.getBranch()).x, currentRow.y);
                Node circle = createCircle(coords, current, pointedBranch);
                currentRow.setLocation(currentRow.x, currentRow.y + Settings.COMMIT_TREE_ADD_TO_Y);
                if (!commitNodes.containsKey(current.getCommit())) {
                    commitNodes.put(current.getCommit(), circle);
                    addToColumnMap(circle, coords);
                }
                commitNodeMap.put(current.getCommit(), coords);
                tempList.add(circle);
            }
        }

        nodeList.addAll(wireUpAllNodes(commitNodeMap, branches));
        nodeList.addAll(tempList);
        return nodeList;
    }

    private void createBranchPointAndColorMap(List<Branch> branches, int x, int y) {
        for (Branch branch : branches) {
            pointMap.put(branch, new Point(x, y));
            if (!colorMap.containsKey(x)) {
                colorMap.put(x, nextColor());
            }
            x += (int) Settings.COMMIT_SPACE_BETWEEN_CIRCLES;
        }
    }

    private List<Node> createBranchLegend() {
        List<Node> nodeList = new LinkedList<>();

        for (Map.Entry<Branch, Point> entry : pointMap.entrySet()) {
            if (!entry.getKey().isHead()) {
                Color color = colorMap.get(entry.getValue().x);
                Circle circle = new Circle(Settings.COMMIT_CIRCLE_RADIUS, color);
                Label label = new Label(entry.getKey().getName());
                label.setPadding(new Insets(0, 5, 0, 5));
                Separator separator = new Separator(Orientation.VERTICAL);
                circle.onMouseClickedProperty().bind(label.onMouseClickedProperty());
                label.setOnMouseClicked((event -> markAllCommits(entry.getKey())));
                HBox.setHgrow(label, Priority.ALWAYS);
                nodeList.add(circle);
                nodeList.add(label);
                nodeList.add(separator);
            }
        }

        for (Node node : nodeList) {
            node.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);
        }

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

    private Node createCircle(Point coordinates, PairBranchCommit data, Map<Commit, Branch> pointedBranches) {
        Circle circle = new Circle(coordinates.getX(), coordinates.getY(), Settings.COMMIT_CIRCLE_RADIUS);
        Color color = colorMapByX(coordinates.x);
        circle.setOnMouseEntered(event -> {
            ((Circle) event.getSource()).toFront();
            circle.setFill(color.brighter());
        });
        circle.getStyleClass().add(Settings.MOUSE_HAND_ON_HOVER);
        circle.setOnMouseExited(event -> circle.setFill(color));
        circle.setFill(color);
        ContextMenu contextMenu = createContextMenu(data.getCommit(), pointedBranches.get(data.getCommit()));
        circle.setOnMouseClicked((event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(circle, event.getScreenX(), event.getScreenY());
            } else if (event.getButton() == MouseButton.PRIMARY) {
                mainController.updateCommitDiffAndFileTree(data.getCommit());
                markAllCommits(data.getBranch());
            }
        }));

        if (pointedBranches.containsKey(data.getCommit())) {
            Utilities.installToolTip(circle, data.getBranch().getName());
            circle.setStrokeWidth(Settings.COMMIT_CIRCLE_STROKE_WIDTH);
            if (color.equals(color.darker())) {
                circle.setStroke(Color.RED);
            } else {
                circle.setStroke(color.darker());
            }
        }

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
        line.setStrokeWidth(Settings.COMMIT_TREE_STROKE_WIDTH);
        line.setFill(null);
        line.toBack();
        return line;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return this.stage;
    }

    public void updateTree() {
        this.left.getChildren().remove(0);
        this.left.getChildren().add(0, buildCommitTree(expandedTree));
    }

    public void markAllCommits(Branch branch) {
        Point point = pointMap.get(branch);
        List<Node> nodes = nodesInColumn.get(point.getX());

        CustomAnimations.playSeriesAnimations(nodes);
    }
}