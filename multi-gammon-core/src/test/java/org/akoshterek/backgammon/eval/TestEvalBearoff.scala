package org.akoshterek.backgammon.eval

import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.junit.Assert._
import org.junit.Test

/**
  * Created by Alex on 13-05-17.
  */
class TestEvalBearoff {
  @Test
  def testEvalBearoffSingle(): Unit = {
    val board = Board()
    board(Board.OPPONENT)(5) = 5
    board(Board.OPPONENT)(4) = 5
    board(Board.SELF)(1) = 1
    board(Board.SELF)(2) = 1
    assertEquals(PositionClass.CLASS_BEAROFF1, Evaluator.getInstance().classifyPosition(board))
    assertTrue(Evaluator.getInstance().evalBearoff1(board)(0) > 0)
    assertTrue("Must be all zeroes for flipped board", Evaluator.getInstance().evalBearoff1(board.swapSides)(0) == 0)
  }

  @Test
  def testEvalBearoffDouble(): Unit = {
    val board = Board()
    board(Board.OPPONENT)(5) = 3
    board(Board.OPPONENT)(4) = 3
    board(Board.SELF)(1) = 1
    board(Board.SELF)(2) = 1
    assertEquals(PositionClass.CLASS_BEAROFF2, Evaluator.getInstance().classifyPosition(board))
    val r = Evaluator.getInstance().evalBearoff2(board)
    assertEquals(1.0, Evaluator.getInstance().evalBearoff2(board)(0), 0.01)
    assertEquals(0.0, Evaluator.getInstance().evalBearoff2(board.swapSides)(0), 0.01)
  }
}
