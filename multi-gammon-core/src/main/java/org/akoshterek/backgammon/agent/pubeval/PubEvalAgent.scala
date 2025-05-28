package org.akoshterek.backgammon.agent.pubeval

import java.nio.file.Path

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.Reward

object PubEvalAgent {
  def apply(path: Path): PubEvalAgent = {
    new PubEvalAgent(path, PubEvalDefaultWeights.contactWeights, PubEvalDefaultWeights.raceWeights)
  }
}

class PubEvalAgent(override val path: Path, val contactWeights: Array[Float], val raceWeights: Array[Float])
  extends AbsAgent("PubEval", path) {

  private val eval: PubEval = new PubEval(contactWeights, raceWeights)

  def evalContact(board: Board): Reward = evaluate(board)

  override def copyAgent(): PubEvalAgent = {
    new PubEvalAgent(path, contactWeights, raceWeights)
  }

  private def evaluate(board: Board): Reward = {
    val reward = Reward.rewardArray[Float]
    val pos: Array[Int] = new Array[Int](28)
    preparePos(board, pos)

    val race: Int = if (curPC.getValue <= PositionClass.CLASS_RACE.getValue) 1 else 0
    reward(Constants.OUTPUT_WIN) = eval.evaluate(race, pos).toFloat
    new Reward(reward)
  }

  private def preparePos(board: Board, pos: Array[Int]): Unit = {
    val (opponent, self) = board.chequersCount

    for (i <- 0 until Board.HALF_BOARD_SIZE - 1) {
      pos(i + 1) = board.anBoard(Board.SELF)(i)
      if (board.anBoard(Board.OPPONENT)(23 - i) != 0) {
        pos(i + 1) = -board.anBoard(Board.OPPONENT)(23 - i)
      }
    }

    pos(25) = board.anBoard(Board.SELF)(Board.BAR)
    pos(0) = -board.anBoard(Board.OPPONENT)(Board.BAR)
    pos(26) = 15 - self
    pos(27) = -(15 - opponent)
  }

  override def clone: PubEvalAgent = {
    new PubEvalAgent(path, eval.contactWeights, eval.raceWeights)
  }
}