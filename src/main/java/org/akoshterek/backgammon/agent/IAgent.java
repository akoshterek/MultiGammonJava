package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.move.Move;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public interface IAgent {
    String getFullName();
    Path getPath();
    int getPlayedGames();

    void startGame();
    void endGame();
    void doMove(Move move);

    default Reward evaluatePosition(Board board, PositionClass pc) {
        switch(pc)
        {
            case CLASS_OVER:
                return evalOver(board);
            case CLASS_RACE:
                return evalRace(board);
            case CLASS_CRASHED:
                return evalCrashed(board);
            case CLASS_CONTACT:
                return evalContact(board);
            default:
                throw new RuntimeException("Unknown class. How did we get here?");
        }
    }

    Reward evalOver(Board board);
    default Reward evalRace(Board board) {
        return evalContact(board);
    }
    default Reward evalCrashed(Board board) {
        return evalContact(board);
    }

    Reward evalContact(Board board);

    boolean isLearnMode();
    void setLearnMode(boolean learn);
    //fixed means it's unable to learn
    boolean isFixed();
    boolean needsInvertedEval();
    boolean supportsSanityCheck();
    void setSanityCheck(boolean sc);

    default void load() {};
    default void save() {};
}
