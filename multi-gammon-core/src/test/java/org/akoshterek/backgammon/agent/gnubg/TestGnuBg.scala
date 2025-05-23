package org.akoshterek.backgammon.agent.gnubg

import java.nio.file.Paths
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.dice.PseudoRandomDiceRoller
import org.akoshterek.backgammon.eval.Evaluator
import org.junit.{Assert, BeforeClass, Test}

/**
  * @author Alex
  *         date 18.09.2015.
  */
object TestGnuBg {
  @BeforeClass def init(): Unit = {
    val currentPath = Paths.get("").toAbsolutePath.normalize
    Evaluator.diceRoller = PseudoRandomDiceRoller()
    Evaluator.basePath = currentPath
  }
}

class TestGnuBg {
  @Test def testRace(): Unit = {
    val agent = new GnubgAgent(Evaluator.basePath)
    val positionId = "4PPBQRCw58gBMA"
    val board = Board.positionFromID(positionId)
    val pc = Evaluator.classifyPosition(board)
    Assert.assertTrue(pc == PositionClass.CLASS_CONTACT)
    agent.evaluatePosition(board, pc)
  }
}