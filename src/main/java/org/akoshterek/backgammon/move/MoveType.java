package org.akoshterek.backgammon.move;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public enum MoveType {
    MOVE_INVALID (-1),
    MOVE_GAMEINFO(0),
    MOVE_NORMAL(1),
    MOVE_DOUBLE(2),
    MOVE_TAKE(3),
    MOVE_DROP(4),
    MOVE_RESIGN(5),
    MOVE_SETBOARD(6),
    MOVE_SETDICE(7),
    MOVE_SETCUBEVAL(8),
    MOVE_SETCUBEPOS(9);

    private final int value;

    private MoveType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
