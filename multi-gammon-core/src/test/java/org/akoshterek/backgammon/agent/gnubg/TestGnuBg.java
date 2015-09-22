package org.akoshterek.backgammon.agent.gnubg;

import org.akoshterek.backgammon.agent.Agent;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Alex
 *         date 18.09.2015.
 */
public class TestGnuBg {
    @Test
    public void testRace() {
        Agent agent = new GnubgAgent(Evaluator.getInstance().getBasePath());
        String positionId = "4PPBQRCw58gBMA";
        System.out.println(positionId);
        Board board = Board.positionFromID(positionId);
        PositionClass pc = Evaluator.getInstance().classifyPosition(board);
        Assert.assertTrue(pc == PositionClass.CLASS_CONTACT);
        Reward reward = agent.evaluatePosition(board, pc);
        System.out.println("Reward: " + reward.toString());
    }

    @BeforeClass
    public static void init() {
        Path currentPath = Paths.get("").toAbsolutePath().normalize();
        Evaluator.getInstance().setSeed(16000000L);
        Evaluator.getInstance().load(currentPath);
    }
}
