package org.akoshterek.backgammon.board;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public enum PositionClass {
    CLASS_OVER(0),         /* Game already finished */
    CLASS_RACE(1),         /* Race neural network */
    CLASS_CRASHED(2),      /* Contact, one side has less than 7 active checkers */
    CLASS_CONTACT(3);      /* Contact neural network */

    //public static final int CLASS_PERFECT = CLASS_BEAROFF_TS.getValue();
    public static final int N_CLASSES = (CLASS_CONTACT.getValue() + 1);

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

}
