package org.akoshterek.backgammon.board;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public enum PositionClass {
    CLASS_OVER(0),         /* Game already finished */
    CLASS_BEAROFF2(1),     /* Two-sided bearoff database (in memory) */
    //CLASS_BEAROFF_TS(2),   /* Two-sided bearoff database (on disk) */
    CLASS_BEAROFF1(2),     /* One-sided bearoff database (in memory) */
    //CLASS_BEAROFF_OS(4),   /* One-sided bearoff database (on disk) */

    CLASS_RACE(3),         /* Race neural network */
    CLASS_CRASHED(4),      /* Contact, one side has less than 7 active checkers */
    CLASS_CONTACT(5);      /* Contact neural network */

    private final int value;

    PositionClass(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static boolean isExact(PositionClass pc) {
        return pc != CLASS_CONTACT && pc != CLASS_CRASHED && pc != CLASS_RACE;
    }

    public static boolean isBearoff(PositionClass pc) {
        switch (pc) {
            case CLASS_BEAROFF2:
            //case CLASS_BEAROFF_TS:
            case CLASS_BEAROFF1:
            //case CLASS_BEAROFF_OS:
                return true;
            default:
                return false;
        }
    }
}
