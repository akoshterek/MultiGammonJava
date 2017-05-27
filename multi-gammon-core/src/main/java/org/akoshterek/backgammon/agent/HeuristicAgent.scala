package org.akoshterek.backgammon.agent

import java.nio.file.Path

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

/**
  * @author Alex
  *         date 03.08.2015.
  */
class HeuristicAgent(override val path: Path) extends AbsAgent("Heuristic", path) {

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

  private def evaluate(board: Board): Float = {
    val points: Array[Int] = board(Board.SELF)
    var equity: Float = 0.0f
    val atHome: Int = 15 - board.chequersCount(Board.SELF)

    // 1/15th of a point per man home
    equity += atHome / 15.0f

    // -1/5th of a point per man on the bar
    equity -= points(Board.BAR) / 5.0f

    for (i <- 0 until Board.HALF_BOARD_SIZE - 1) {
      // -1/10th of a point for each blot
      if (points(i) == 1) {
        equity -= 0.10f
      }
      // +1/20th for contiguous points
      else if (i > 0 && points(i) >= 2 && points(i - 1) >= 2) {
        equity += 0.05f
      }

      val dist: Int = 25 - i

      // value based on closeness to home
      equity += (12.5f - dist) * points(i).toFloat / 225.0f
    }

    equity
  }
}