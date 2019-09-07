package magit.utils;

import javafx.animation.FillTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import settings.Settings;

public class CustomAnimations {
    public static FillTransition fillTransition(Circle circle) {
        Color color = Color.valueOf(circle.getFill().toString());
        FillTransition ft = new FillTransition(Settings.ANIMATION_DURATION, circle, color, Settings.getBrighter(color));
        ft.setAutoReverse(true);
        ft.setCycleCount(4);
        return ft;
    }
}
