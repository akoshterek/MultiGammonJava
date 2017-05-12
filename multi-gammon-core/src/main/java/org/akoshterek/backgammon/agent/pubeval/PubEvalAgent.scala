package org.akoshterek.backgammon.agent.pubeval

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path

object PubEvalAgent {
  def buildDefault(path: Path): PubEvalAgent = {
    new PubEvalAgent(path, PubEvalDefaultWeights.contactWeights, PubEvalDefaultWeights.raceWeights)
  }
}

class PubEvalAgent(override val path: Path, val contactWeights: Array[Double], val raceWeights: Array[Double])
  extends AbsAgent("PubEval", path) with Cloneable {

  private val eval: PubEval = new PubEval(contactWeights, raceWeights)

  def evalContact(board: Board): Reward = evaluate(board)

  private def evaluate(board: Board): Reward = {
    val reward = Reward.rewardArray
    val pos: Array[Int] = new Array[Int](28)
    preparePos(board, pos)

    val race: Int = if (curPC.getValue <= PositionClass.CLASS_RACE.getValue) 1 else 0
    reward(Constants.OUTPUT_WIN) = eval.evaluate(race, pos)
    new Reward(reward)
  }

  private def preparePos(board: Board, pos: Array[Int]) {
    val tmpBoard: Board = board.clone()//.swapSides
    val (opponent, self) = tmpBoard.chequersCount

    for (i <- 0 until Board.HALF_BOARD_SIZE - 1) {
      pos(i + 1) = tmpBoard.anBoard(Board.SELF)(i)
      if (tmpBoard.anBoard(Board.OPPONENT)(23 - i) != 0) {
        pos(i + 1) = -tmpBoard.anBoard(Board.OPPONENT)(23 - i)
      }
    }

    pos(25) = tmpBoard.anBoard(Board.SELF)(Board.BAR)
    pos(0) = -tmpBoard.anBoard(Board.OPPONENT)(Board.BAR)
    pos(26) = 15 - self
    pos(27) = -(15 - opponent)
  }

  override def clone: PubEvalAgent = {
    new PubEvalAgent(path, eval.contactWeights, eval.raceWeights)
  }
}