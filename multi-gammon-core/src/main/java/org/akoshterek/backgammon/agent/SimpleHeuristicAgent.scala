package org.akoshterek.backgammon.agent

import java.nio.file.Path
import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

/**
  * Simple heuristic agent - stronger than Random but weaker than full Heuristic.
  * Uses only basic positional evaluation without tactical awareness (no blot detection, no prime building).
  * Good intermediate benchmark for tracking training progress.
  *
  * @author Alex
  */
class SimpleHeuristicAgent(override val path: Path) extends AbsAgent("SimpleHeuristic", path) {

  override def copyAgent(): SimpleHeuristicAgent = {
    new SimpleHeuristicAgent(path)
  }

  override def evalRace(board: Board): Reward = {
    evalContact(board)
  }

  override def evalCrashed(board: Board): Reward = {
    evalContact(board)
  }

  def evalContact(board: Board): Reward = {
    val reward = Reward.rewardArray[Float]
    reward(Constants.OUTPUT_WIN) = evaluate(board)
    new Reward(reward)
  }

  /**
    * Simple evaluation based only on:
    * 1. Men borne off (positive)
    * 2. Men on bar (negative)
    * 3. Average pip count (distance to home)
    *
    * Does NOT consider:
    * - Blots (exposed checkers)
    * - Primes (contiguous points)
    * - Blocking positions
    */
  private def evaluate(board: Board): Float = {
    val points: Array[Int] = board(Board.SELF)
    var equity: Float = 0.0f
    val atHome: Int = 15 - board.chequersCount(Board.SELF)

    // 1/15th of a point per man home (same as full Heuristic)
    equity += atHome / 15.0f

    // -1/5th of a point per man on the bar (same as full Heuristic)
    equity -= points(Board.BAR) / 5.0f

    // Simple pip count evaluation - sum of (pip distance * checker count)
    // Normalized by maximum possible pip count (375 = 15 checkers at point 25)
    var totalPips: Float = 0.0f
    for (i <- 0 until Board.HALF_BOARD_SIZE - 1) {
      val dist: Int = 25 - i
      totalPips += dist * points(i)
    }
    // Men on bar count as distance 25
    totalPips += 25 * points(Board.BAR)

    // Normalize and invert (lower pip count = better position)
    equity -= totalPips / 375.0f

    equity
  }
}
