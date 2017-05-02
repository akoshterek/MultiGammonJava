package org.akoshterek.backgammon.agent.gnubg

import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.eval.Reward
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.nio.file.Path
import java.nio.file.Paths

/**
  * @author Alex
  *         date 18.09.2015.
  */
object TestGnuBg {
  @BeforeClass def init(): Unit = {
    val currentPath = Paths.get("").toAbsolutePath.normalize
    Evaluator.getInstance.setSeed(16000000L)
    Evaluator.getInstance.load(currentPath)
  }
}

class TestGnuBg {
  @Test def testRace(): Unit = {
    val agent = new GnubgAgent(Evaluator.getInstance.getBasePath)
    val positionId = "4PPBQRCw58gBMA"
    System.out.println(positionId)
    val board = Board.positionFromID(positionId)
    val pc = Evaluator.getInstance.classifyPosition(board)
    Assert.assertTrue(pc eq PositionClass.CLASS_CONTACT)
    val reward = agent.evaluatePosition(board, pc)
    System.out.println("Reward: " + reward.toString)
  }
}