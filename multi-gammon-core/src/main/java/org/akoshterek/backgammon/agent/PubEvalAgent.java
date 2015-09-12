package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 31.08.2015.
 */
public class PubEvalAgent extends AbsAgent {
    private final PubEval eval = new PubEval();

    public PubEvalAgent(Path path) {
        super(path);
        fullName = "PubEval";
    }

    @Override
    public Reward evalContact(Board board) {
        Reward reward = new Reward();
        int [] pos = new int[28];
        preparePos(board, pos);

        int race = curPC.getValue() <= PositionClass.CLASS_RACE.getValue() ? 1 : 0;
        reward.data[Constants.OUTPUT_WIN] = eval.pubeval(race, pos);
        return reward;
    }

    private void preparePos(Board board, int[] pos)  {
        Board tmpBoard = new Board(board);
        tmpBoard.swapSides();

        int[] men = new int[2];
        tmpBoard.chequersCount(men);

        for(int i = 0; i < 24; i++) {
            pos[i+1] = tmpBoard.anBoard[Board.SELF][i];
            if(tmpBoard.anBoard[Board.OPPONENT][23 - i] != 0) {
                pos[i + 1] = -tmpBoard.anBoard[Board.OPPONENT][23 - i];
            }
        }

        pos[25] = tmpBoard.anBoard[Board.SELF][Board.BAR];
        pos[0] = -tmpBoard.anBoard[Board.OPPONENT][Board.BAR];
        pos[26] = 15 - men[Board.SELF];
        pos[27] = -(15 - men[Board.OPPONENT]);
    }
}
