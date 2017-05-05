package org.akoshterek.backgammon.matchstate

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Evaluator

/**
  * @author Alex
  *         date 05.08.2015.
  */
class MatchState {
  var board: Board = new Board
  var anDice: (Int, Int) = (0, 0) // (0,0) for unrolled anDice
  var fTurn: Int = -1 // who makes the next decision
  var fMove: Int = -1 // player on roll
  var anScore: Array[Int] = Array[Int](0, 0)
  var gs: GameState = GameState.GAME_NONE

  def rollDice() {
    anDice = (Evaluator.getInstance.nextDice, Evaluator.getInstance.nextDice)
  }
}