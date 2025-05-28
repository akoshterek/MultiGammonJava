package org.akoshterek.backgammon.agent

import java.nio.file.Path
import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

import scala.util.Random

class RandomAgent(override val path: Path) extends AbsAgent("Random", path) {
    final private val random: Random = new Random

    override def copyAgent(): RandomAgent = {
        new RandomAgent(path)
    }

    def evalContact(board: Board): Reward = {
        val reward = Reward.rewardArray[Float]
        reward(Constants.OUTPUT_WIN) = random.nextFloat()
        new Reward(reward)
    }

    override def evalRace(board: Board): Reward = evalContact(board)

    override def evalCrashed(board: Board): Reward = evalContact(board)
}