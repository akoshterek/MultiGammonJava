package org.akoshterek.backgammon.matchstate;

/**
 * @author Alex
 *         date 06.08.2015.
 */
public enum GameState {
    GAME_NONE(0),
    GAME_PLAYING(1),
    GAME_OVER(2);

    private final int value;

    GameState(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
