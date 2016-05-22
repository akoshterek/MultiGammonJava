package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.move.Move;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public interface Agent {
    String getFullName();
    Path getPath();
    int getPlayedGames();

    void startGame();
    void endGame();
    void doMove(Move move);

    default Reward evaluatePosition(final Board board, PositionClass pc) {
        if(PositionClass.isBearoff(pc) && !supportsBearoff()) {
            pc = PositionClass.CLASS_RACE;
        }

        switch (pc) {
            case CLASS_OVER:
                return evalOver(board);
            case CLASS_RACE:
                return evalRace(board);
            case CLASS_CRASHED:
                return evalCrashed(board);
            case CLASS_CONTACT:
                return evalContact(board);
            case CLASS_BEAROFF1:
                return Evaluator.getInstance().evalBearoff1(board);
            case CLASS_BEAROFF2:
                return Evaluator.getInstance().evalBearoff2(board);
            default:
                throw new RuntimeException("Unknown class. How did we get here?");
        }
    }

    void scoreMoves(Move[] moves, int count);
    Reward scoreMove(Move pm);

    default Reward evalOver(final Board board) {
        return Evaluator.getInstance().evalOver(board);
    }
    default Reward evalRace(final Board board) {
        return evalContact(board);
    }
    default Reward evalCrashed(final Board board) {
        return evalContact(board);
    }

    Reward evalContact(Board board);

    boolean isLearnMode();
    void setLearnMode(boolean learn);
    //fixed means it's unable to learn
    boolean isFixed();
    boolean needsInvertedEval();
    void setNeedsInvertedEval(boolean needsInvertedEval);
    boolean supportsSanityCheck();
    void setSanityCheck(boolean sc);
    boolean supportsBearoff();
    void setCurrentBoard(Board board);

    default void load() {}
    default void save() {}
}
