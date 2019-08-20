package utils;

import java.util.Arrays;
import java.util.Optional;

public enum eUserMergeChoice {
    ANCESTOR(0), ACTIVE(1), TARGET(3), OTHER(4);

    private int num;

    eUserMergeChoice(int num) {
        this.num = num;
    }

    public int getNumber() {
        return num;
    }

    public static Optional<eUserMergeChoice> getItem(int index) {
        return Arrays.stream(eUserMergeChoice.values()).filter(e -> index == e.getNumber()).findFirst();
    }
}
