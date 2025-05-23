package org.akoshterek.backgammon.matchstate

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.dice.{DiceRoller, PseudoRandomDiceRoller}
import org.akoshterek.backgammon.eval.Evaluator

/**
  * Represents the current state of a backgammon match.
  *
  * This class encapsulates all the information needed to describe a backgammon match,
  * including the board position, dice values, player turns, scores, and game state.
  * It serves as the central data structure for tracking match progress.
  */
class MatchState {
  /** Current board position */
  var board: Board = new Board

  /**
    * Current dice values as a tuple.
    * (0,0) represents unrolled dice
    */
  var anDice: (Int, Int) = (0, 0)

  /**
    * Player who makes the next decision.
    * -1 represents an uninitialized state
    */
  var fTurn: Int = -1

  /**
    * Player on roll.
    * -1 represents an uninitialized state
    */
  var fMove: Int = -1

  /** Array containing the score for each player */
  var anScore: Array[Int] = Array[Int](0, 0)

  /** Current state of the game */
  var gs: GameState = GameState.GAME_NONE

  /**
    * Rolls the dice and updates the dice values in the match state.
    *
    * Uses the dice roller provided by the Evaluator to generate
    * random dice values for the next move.
    */
  def rollDice(): Unit = {
    anDice = Evaluator.diceRoller.roll()
  }
}
