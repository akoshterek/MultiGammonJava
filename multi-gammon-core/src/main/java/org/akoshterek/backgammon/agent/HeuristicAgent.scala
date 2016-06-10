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
        val reward: Reward = new Reward
        var value: Double = 0
        val tmpBoard: Board = new Board(board)
        tmpBoard.swapSides()
        val points: Array[Int] = tmpBoard.anBoard(Board.SELF)

        val (_, self) = tmpBoard.chequersCount
        val atHome: Int = 15 - self

        // 1/15th of a point per man home
        value += atHome / 15.0

        // -1/5th of a point per man on the bar
        value -= points(Board.BAR) / 5.0

        for (i <- 0 until 24) {
            // -1/10th of a point for each blot
            // +1/20th for contiguous points
            if (points(i) == 1) {
                value -= 0.10
            }
            else if (i > 0 && points(i) >= 2 && points(i - 1) >= 2) {
                value += 0.05
            }

            val dist: Int = 25 - i

            // value based on closeness to home
            value += (12.5 - dist) * points(i).toDouble / 225.0
        }
        reward.data(Constants.OUTPUT_WIN) = value
        reward
    }
}