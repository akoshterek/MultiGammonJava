package org.akoshterek.backgammon.agent.raw

import java.nio.file.Path

import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.agent.inputrepresentation.GnuBgCodec
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import org.akoshterek.backgammon.nn.{Linear, NeuralNetwork, Sigmoid}

/**
  * Created by Alex on 17-06-17.
  */
class RawTd40(override val path: Path) extends AbsAgent("RawTd40", path) with Cloneable {
  private val representation = new RawRepresentation(GnuBgCodec)
  private var nn = new NeuralNetwork(representation.contactInputsCount, 40, 5, Sigmoid, Linear)

  override def evaluatePosition(board: Board, pc: PositionClass): Reward = {
    pc match {
      case PositionClass.CLASS_OVER => evalOver(board)
      case _ => evalContact(board)
    }
  }

  override def evalContact(board: Board): Reward = {
    Reward(nn.calculate(representation.calculateContactInputs(board)))
  }

  override def evalRace(board: Board): Reward = evalContact(board)

  override def evalCrashed(board: Board): Reward = evalContact(board)

  override def clone: RawTd40 = {
    val other: RawTd40 = super.clone.asInstanceOf[RawTd40]
    other.nn = nn
    other
  }


  override def doMove(move: Move): Unit = {
    super.doMove(move)
    if (isLearnMode) {
      doLearnMove(move)
    }
  }

  private def doLearnMove(move: Move): Unit = {

  }
}
