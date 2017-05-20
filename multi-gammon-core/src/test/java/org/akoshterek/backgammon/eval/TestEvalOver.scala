package org.akoshterek.backgammon.eval

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.matchstate.GameResult
import org.junit.Assert.assertEquals
import org.junit.Test

/**
  * Created by Alex on 07.05.2017.
  */
class TestEvalOver {
  @Test
  def testEvalOverWinSingle(): Unit = {
    val board = new Board
    board(Board.OPPONENT)(1) = 1
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.SINGLE, board.gameResult)
    assertEquals(Reward(Array[Double](1, 0, 0, 0, 0)), reward)
  }

  @Test
  def testEvalOverLoseSingle(): Unit = {
    val board = new Board
    board(Board.SELF)(1) = 1
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.SINGLE, board.gameResult)
    assertEquals(Reward(Array[Double](0, 0, 0, 0, 0)), reward)
  }

  @Test
  def testEvalWinGammon(): Unit = {
    val board = new Board
    board(Board.OPPONENT)(1) = 15
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.GAMMON, board.gameResult)
    assertEquals(Reward(Array[Double](1, 1, 0, 0, 0)), reward)
  }

  @Test
  def testEvalLoseGammon(): Unit = {
    val board = new Board
    board(Board.SELF)(1) = 15
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.GAMMON, board.gameResult)
    assertEquals(Reward(Array[Double](0, 0, 0, 1, 0)), reward)
  }

  @Test
  def testEvalWinBackgammon(): Unit = {
    val board = new Board
    board(Board.OPPONENT)(20) = 15
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.BACKGAMMON, board.gameResult)
    assertEquals(Reward(Array[Double](1, 1, 1, 0, 0)), reward)
  }

  @Test
  def testEvalLoseBackgammon(): Unit = {
    val board = new Board
    board(Board.SELF)(20) = 15
    val reward: Reward = Evaluator.getInstance().evalOver(board)
    assertEquals(GameResult.BACKGAMMON, board.gameResult)
    assertEquals(Reward(Array[Double](0, 0, 0, 1, 1)), reward)
  }
}
