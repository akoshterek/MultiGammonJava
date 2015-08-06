package org.akoshterek.backgammon.match;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Evaluator;

/**
 * @author Alex
 *         date 05.08.2015.
 */
public class MatchState {
    public Board board;
    public int[] anDice = new int[]{0, 0};    // (0,0) for unrolled anDice
    public int fTurn = 0;        // who makes the next decision
    public int fDoubled = 0;
    public int fMove = 0;        // player on roll
    public int[] anScore = new int[]{0, 0};
    public GameState gs = GameState.GAME_NONE;

    public void rollDice() {
        anDice[0] = Evaluator.getInstance().nextDice();
        anDice[1] = Evaluator.getInstance().nextDice();
    }
}
