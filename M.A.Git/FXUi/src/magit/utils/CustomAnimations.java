package magit.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.util.Duration;
import settings.Settings;

import java.util.LinkedList;
import java.util.List;

public class CustomAnimations {
    private static List<Transition> allAnimations = new LinkedList<>();
    public static FillTransition fillTransition(Circle circle) {
        Color color = Color.valueOf(circle.getFill().toString());
        FillTransition ft = new FillTransition(Settings.ANIMATION_DURATION, circle, color, Settings.getBrighter(color));
        ft.setAutoReverse(Settings.ALLOW_REVERSE);
        ft.setCycleCount(Settings.CYCLE_COUNT);
        allAnimations.add(ft);
        return ft;
    }

    private static Transition commitTreeTransition(Circle node) {
        ScaleTransition scaleTransition = new ScaleTransition(Settings.ANIMATION_DURATION, node);
        scaleTransition.setByX(Settings.SCALE_NODE);
        scaleTransition.setByY(Settings.SCALE_NODE);
        scaleTransition.setCycleCount(Settings.CYCLE_COUNT);
        scaleTransition.setAutoReverse(Settings.ALLOW_REVERSE);
        allAnimations.add(scaleTransition);
        return scaleTransition;
    }

    private static Transition commitTreeTransition(CubicCurve line) {
        Color color = Color.valueOf(line.getStroke().toString());
        StrokeTransition strokeTransition = new StrokeTransition(Settings.ANIMATION_DURATION, line, color, Color.valueOf(Settings.LINE_BRIGHTER_COLOR));
        strokeTransition.setAutoReverse(Settings.ALLOW_REVERSE);
        strokeTransition.setCycleCount(Settings.CYCLE_COUNT);
        allAnimations.add(strokeTransition);
        return strokeTransition;
    }

    public static Transition commitTreeTransition(Node node) {
        if (node instanceof Circle) {
            node.toFront();
            return commitTreeTransition((Circle) node);
        }
        if (node instanceof CubicCurve) {
            node.toBack();
            return commitTreeTransition((CubicCurve) node);
        }

        return new StrokeTransition(Duration.ZERO);
    }

    public static Transition snoozeNode(Node node) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(100), node);
        translateTransition.setByX(5);
        translateTransition.setCycleCount(4);
        translateTransition.setAutoReverse(true);
        allAnimations.add(translateTransition);
        return translateTransition;
    }

    public static void playAnimation(Transition animation) {
        animation.play();
    }

    public static void playSeriesAnimations(List<Node> nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                CustomAnimations.playAnimation(CustomAnimations.commitTreeTransition(node));
            }
        }
    }
}
