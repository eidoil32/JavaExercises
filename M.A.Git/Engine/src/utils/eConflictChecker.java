package utils;

import java.util.Arrays;
import java.util.Optional;

public enum eConflictChecker {
    CONFLICT_1(false,false,false,false,false,false) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_2(false,false,false,false,false,true) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_3(false,false,false,false,true,false) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_4(false,false,false,false,true,true) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_5(false,false,false,true,false,false) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_6(false,false,false,true,false,true) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_7(false,false,false,true,true,false) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_8(false,false,false,true,true,true) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_9(false,false,true,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_10(false,false,true,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_11(false,false,true,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_12(false,false,true,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_13(false,false,true,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_14(false,false,true,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_15(false,false,true,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_16(false,false,true,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_17(false,true,false,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_18(false,true,false,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_19(false,true,false,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_20(false,true,false,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_21(false,true,false,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_22(false,true,false,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_23(false,true,false,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_24(false,true,false,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_25(false,true,true,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_26(false,true,true,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_27(false,true,true,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_28(false,true,true,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_29(false,true,true,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_30(false,true,true,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_31(false,true,true,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_32(false,true,true,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_33(true,false,false,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return true; } },
    CONFLICT_34(true,false,false,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_35(true,false,false,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_36(true,false,false,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_37(true,false,false,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return true; } },
    CONFLICT_38(true,false,false,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_39(true,false,false,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return true; } },
    CONFLICT_40(true,false,false,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_41(true,false,true,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_42(true,false,true,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_43(true,false,true,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_44(true,false,true,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_45(true,false,true,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_46(true,false,true,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_47(true,false,true,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return true; } },
    CONFLICT_48(true,false,true,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_49(true,true,false,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_50(true,true,false,false,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_51(true,true,false,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_52(true,true,false,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_53(true,true,false,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return true; } },
    CONFLICT_54(true,true,false,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_55(true,true,false,true,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return true; } },
    CONFLICT_56(true,true,false,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return true; } },
    CONFLICT_57(true,true,true,false,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_58(true,true,true,false,false,true) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_59(true,true,true,false,true,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_60(true,true,true,false,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_61(true,true,true,true,false,false) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 2; }  @Override public boolean notTake() { return false; } },
    CONFLICT_62(true,true,true,true,false,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } },
    CONFLICT_63(true,true,true,true,true,false) { @Override public boolean isConflict() { return false; } @Override public boolean take() { return false; } @Override public int whatToTake() { return 0; }  @Override public boolean notTake() { return false; } },
    CONFLICT_64(true,true,true,true,true,true) { @Override public boolean isConflict() { return true; } @Override public boolean take() { return true; } @Override public int whatToTake() { return 1; }  @Override public boolean notTake() { return false; } };
    private boolean isCondition_0, isCondition_1, isCondition_2,
            isCondition_3, isCondition_4, isCondition_5;

    eConflictChecker(boolean... conditions) {
        this.isCondition_0 = conditions[0];
        this.isCondition_1 = conditions[1];
        this.isCondition_2 = conditions[2];
        this.isCondition_3 = conditions[3];
        this.isCondition_4 = conditions[4];
        this.isCondition_5 = conditions[5];
    }

    public static Optional<eConflictChecker> getItem(boolean ... values) {
        return Arrays.stream(eConflictChecker.values())
                .filter(e -> values[0] == e.isCondition_0)
                .filter(e -> values[1] == e.isCondition_1)
                .filter(e -> values[2] == e.isCondition_2)
                .filter(e -> values[3] == e.isCondition_3)
                .filter(e -> values[4] == e.isCondition_4)
                .filter(e -> values[5] == e.isCondition_5)
                .findFirst();
    }

    public abstract int whatToTake();
    public abstract boolean isConflict();
    public abstract boolean take();
    public abstract boolean notTake();
}