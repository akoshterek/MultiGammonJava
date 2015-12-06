package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Paths;

/**
 * @author Alex
 *         date 03.08.2015.
 */
public class TestHeuristicAgent {

    @Test
    public void testAgent() {
        Agent agent = new HeuristicAgent(Paths.get("."));
        checkPosition(agent, "4HPwATDYZvDBAA");
    }

    private void checkPosition(Agent agent, String positionId) {
        Board board = Board.positionFromID(positionId);
//        System.out.println(BoardFormatter.drawBoard(board, 0, new String[7], ""));
        Reward reward = agent.evalContact(board);
        board.swapSides();
        Reward reward2 = agent.evalContact(board);
        Assert.assertTrue("Wrong calculation for " + positionId, reward.equity() > reward2.equity());
    }
}
