package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path
import java.util.Random

class RandomAgent(val path: Path) extends AbsAgent(path) {
    fullName = "Random"
    final private val random: Random = new Random

    def evalContact(board: Board): Reward = {
        val reward: Reward = new Reward
        reward.data(Constants.OUTPUT_WIN) = random.nextDouble
        reward
    }

    override def evalRace(board: Board): Reward = {
        evalContact(board)
    }

    override def evalCrashed(board: Board): Reward = {
        evalContact(board)
    }
}