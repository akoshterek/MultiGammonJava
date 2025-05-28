package org.akoshterek.backgammon.agent.raw

import java.nio.file.Path
import org.akoshterek.backgammon.agent.{AbsAgent, CopyableAgent}
import org.akoshterek.backgammon.agent.inputrepresentation.{GnuBgCodec, Tesauro92Codec}
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import org.akoshterek.backgammon.nn.{Elliott, Linear, NeuralNetwork}

/**
  * Created by Alex on 17-06-17.
  */
//TODO: keep eligibility trace for TD(lambda) per player
class RawTd40(override val path: Path) extends AbsAgent("RawTd40", path) {
  private val representation = new RawRepresentation(Tesauro92Codec)
  private var nn = new NeuralNetwork(representation.contactInputsCount, 40, 5, Elliott, Linear)

  override def copyAgent(): RawTd40 = {
        val other: RawTd40 = new RawTd40(path)
        other.nn = nn
        other
  }

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

  override def startGame(): Unit = {

  }

  override def endGame(): Unit = {
    super.endGame()
    playedGames
  }

  override def doMove(move: Move): Unit = {
    super.doMove(move)
    if (isLearnMode) {
      doLearnMove(move)
    }
  }

  /**
   * For clarity
   * Let's imagine board state S1 before move and S2 after move
   * currentInput = boardRepresentation(S1)
   * currentOutput = nn.forward(currentInput)
   * target =  nn.forward(boardRepresentation(S2))
   *
   * is it correct?
   *
   *
   * ChatGPT said:
   * Exactly right! That’s a solid way to frame it for TD learning with a neural net:
   *
   * currentInput = representation of the current board state S1 (before move)
   *
   * currentOutput = network’s predicted value for S1 (usually a win probability or value estimate)
   *
   * target = network’s predicted value for the next state S2 (after the move), which serves as the target for updating the prediction at S1
   *
   * Then the TD error is:
   * target - currentOutput
   *
   * This lets you train the network to better approximate the expected future reward starting from S1, bootstrapping from the prediction of S2.
   *
   * If S2 is a terminal state (win/loss), target can be 1 or 0 directly instead of a forward pass.
   *
   * So your intuition is perfect — using forward passes for both current and next states as inputs to train makes the process explicit and clearer.
   *
   */
  private def doLearnMove(move: Move): Unit = {
    val boardAfterMove = Board.positionFromKey(move.auch)
    val nextOutput = evaluatePosition(boardAfterMove, move.pc).data
    val boardBeforeMove = currentBoard
    val currentInput = representation.calculateContactInputs(boardBeforeMove)
    val currentOutput = nn.calculate(currentInput)
    nn.backpropWithEtraces(currentInput, currentOutput, nextOutput)
  }
}
