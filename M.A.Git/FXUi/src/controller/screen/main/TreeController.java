package controller.screen.main;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import magit.Branch;
import magit.Commit;
import magit.Magit;
import settings.Settings;
import utils.PairBranchCommit;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TreeController {
    private Magit model;
    private MainController mainController;

    public TreeController(MainController mainController) {
        this.model = mainController.getModel();
        this.mainController = mainController;
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
        double size = model.getCurrentRepository().getAllBranches().size();
        size = (Settings.COMMIT_CIRCLE_RADIUS + Settings.COMMIT_SPACE_BETWEEN_CIRCLES) * size; //each branch take column width radius + 10 (for spaces)

        scrollPane.setContent(vbox);
        scrollPane.setMinWidth(size > 200 ? 200 : size);
        scrollPane.setMaxWidth(size > 200 ? 200 : size);
        scrollPane.setFitToWidth(true);
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
        List<Branch> branches = model.getCurrentRepository().getAllBranches();

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
        circle.setOnMouseClicked(event -> Platform.runLater(() -> mainController.updateCommitDiffAndFileTree(data)));
        circle.setOnMouseEntered(event -> {
            ((Circle) event.getSource()).toFront();
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

    private Line createLine(Point src, Point dest) {
        Line line = new Line(src.x, src.y, dest.x, dest.y);
        line.setStrokeWidth(Settings.COMMIT_TREE_LINE_TICK);
        line.setOpacity(0.2);
        return line;
    }
}
