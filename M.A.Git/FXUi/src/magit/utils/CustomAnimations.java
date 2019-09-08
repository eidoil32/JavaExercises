package magit.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.CubicCurve;
import javafx.util.Duration;
import settings.Settings;

public class CustomAnimations {
    public static FillTransition fillTransition(Circle circle) {
        Color color = Color.valueOf(circle.getFill().toString());
        FillTransition ft = new FillTransition(Settings.ANIMATION_DURATION, circle, color, Settings.getBrighter(color));
        ft.setAutoReverse(Settings.ALLOW_REVERSE);
        ft.setCycleCount(Settings.CYCLE_COUNT);
        return ft;
    }

    private static Transition commitTreeTransition(Circle node) {
        ScaleTransition st = new ScaleTransition(Settings.ANIMATION_DURATION, node);
        st.setByX(Settings.SCALE_NODE);
        st.setByY(Settings.SCALE_NODE);
        st.setCycleCount(Settings.CYCLE_COUNT);
        st.setAutoReverse(Settings.ALLOW_REVERSE);

        return st;
    }

    private static Transition commitTreeTransition(CubicCurve line) {
        Color color = Color.valueOf(line.getStroke().toString());
        StrokeTransition ft = new StrokeTransition(Settings.ANIMATION_DURATION, line, color, Color.valueOf(Settings.LINE_BRIGHTER_COLOR));
        ft.setAutoReverse(Settings.ALLOW_REVERSE);
        ft.setCycleCount(Settings.CYCLE_COUNT);
        return ft;
    }

    public static Transition commitTreeTransition(Node node) {
        if (node instanceof Circle)
            return commitTreeTransition((Circle)node);
        if (node instanceof CubicCurve)
            return commitTreeTransition((CubicCurve)node);

        return new StrokeTransition(Duration.ZERO);
    }
}
