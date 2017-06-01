package org.akoshterek.backgammon.agent

import java.nio.file.Paths

import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent
import org.akoshterek.backgammon.board.Board
import org.junit.{Assert, Test}

/**
  * @author Alex
  *         date 03.08.2015.
  */
class TestHeuristicAgent {
  @Test def testAgent(): Unit = {
    val agent = PubEvalAgent(Paths.get("."))
    checkPosition(agent, "4HPwATDYZvDBAA")
  }

  private def checkPosition(agent: Agent, positionId: String) = {
    var board = Board.positionFromID(positionId)
    val backPosId = board.positionID
    //System.out.println(BoardFormatter.drawBoard(board, 0, new String[7], ""));
    val reward = agent.evalContact(board)
    board = board.swapSides
    val reward2 = agent.evalContact(board)
    Assert.assertEquals("Mismatch in board conversion", positionId, backPosId)
    Assert.assertTrue("Wrong calculation for " + positionId, reward.equity < reward2.equity)
  }
}