package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path
import java.util.Random

class RandomAgent(override val path: Path) extends AbsAgent("Random", path) {
    final private val random: Random = new Random

    def evalContact(board: Board): Reward = {
        val reward = Reward.rewardArray
        reward(Constants.OUTPUT_WIN) = random.nextDouble
        new Reward(reward)
    }

    override def evalRace(board: Board): Reward = evalContact(board)

    override def evalCrashed(board: Board): Reward = evalContact(board)
}