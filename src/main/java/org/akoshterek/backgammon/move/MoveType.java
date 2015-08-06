package org.akoshterek.backgammon.move;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public enum MoveType {
    MOVE_INVALID (-1),
    MOVE_GAMEINFO(0),
    MOVE_NORMAL(1),
    MOVE_SETDICE(7);

    private final int value;

    private MoveType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
