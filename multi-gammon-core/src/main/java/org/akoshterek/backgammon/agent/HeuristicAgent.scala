package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path

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
    val tmpBoard: Board = board.clone().swapSides()
    val reward = Reward.rewardArray
    reward(Constants.OUTPUT_WIN) = evaluate(tmpBoard.anBoard(Board.SELF).toVector)
    new Reward(reward)
  }

  private def evaluate(points: Vector[Int]): Double = {
    var equity: Double = 0.0
    val atHome: Int = 15 - points.sum

    // 1/15th of a point per man home
    equity += atHome / 15.0

    // -1/5th of a point per man on the bar
    equity -= points(Board.BAR) / 5.0

    for (i <- 0 until Board.HALF_BOARD_SIZE - 1) {
      // -1/10th of a point for each blot
      if (points(i) == 1) {
        equity -= 0.10
      }
      // +1/20th for contiguous points
      else if (i > 0 && points(i) >= 2 && points(i - 1) >= 2) {
        equity += 0.05
      }

      val dist: Int = 25 - i

      // value based on closeness to home
      equity += (12.5 - dist) * points(i).toDouble / 225.0
    }

    equity
  }
}