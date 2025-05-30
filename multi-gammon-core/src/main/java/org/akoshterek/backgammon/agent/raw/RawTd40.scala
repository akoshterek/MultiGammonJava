package org.akoshterek.backgammon.agent.raw

import java.nio.file.{Files, Path, Paths}
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro92Codec
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import org.akoshterek.backgammon.nn.{EligibilityTrace2D, TdNeuralNetwork, Weights2D}

import scala.util.Using


class RawTd40(override val path: Path, val isCopy: Boolean = false) extends AbsAgent("RawTd40", path) {
  private val representation = new RawRepresentation(Tesauro92Codec)
  // shared NN
  private var tdNN = new TdNeuralNetwork(representation.contactInputsCount, 40, 1)
  private var eligibilityTrace: EligibilityTrace2D = _
  private var weights: Weights2D = _

  private val filePath: Path = path.resolve(s"log/${fullName}_td_metrics.csv")
  Files.createDirectories(filePath.getParent) // ensures ./log exists
  private val metricsFile = filePath.toFile


  if (!metricsFile.exists()) {
    val writer = new java.io.PrintWriter(metricsFile)
    writer.println("gamesPlayed, averageTDError, weightDelta")
    writer.close()
  }

  override def copyAgent(): RawTd40 = {
        val other: RawTd40 = new RawTd40(path, true)
        other.tdNN = tdNN
        other
  }

  override def evaluatePosition(board: Board, pc: PositionClass): Reward = {
    pc match {
      case PositionClass.CLASS_OVER => evalOver(board)
      case _ => evalContact(board)
    }
  }

  override def evalContact(board: Board): Reward = {
    val output = Reward.rewardArray[Float]
    tdNN.forward(representation.calculateContactInputs(board), output)
    Reward(output)
  }

  override def evalRace(board: Board): Reward = evalContact(board)

  override def evalCrashed(board: Board): Reward = evalContact(board)

  override def startGame(): Unit = {
    eligibilityTrace = tdNN.createEligibilityTrace()

    if (isLearnMode && playedGames % 1000 == 0) {
      weights = tdNN.weightsCopy
    }
  }

  override def endGame(): Unit = {
    super.endGame()

    if (isLearnMode && !isCopy && playedGames % 1000 == 0) {
      // calculate metrics
      val snapshot = tdNN.weightsCopy
      val averageTdError = tdNN.getAverageTDError(reset = true)
      val delta = calculateWeightDelta(weights, snapshot)

      // Append metrics to CSV
      Using(new java.io.PrintWriter(new java.io.FileOutputStream(metricsFile, true))) { writer =>
        writer.println(s"$playedGames, $averageTdError, $delta")
      }

      weights = snapshot
    }

    playedGames
  }

  override def doMove(move: Move): Unit = {
    super.doMove(move)
    if (isLearnMode) {
      doLearnMove(move)
    }
  }


  private def doLearnMove(move: Move): Unit = {
    val boardAfterMove = Board.positionFromKey(move.auch)
    val afterMoveOutput = evaluatePosition(boardAfterMove, move.pc).data
    val boardBeforeMove = currentBoard

    // to call forward()
    val input = representation.calculateContactInputs(boardBeforeMove)
    val currentOutput = Reward.rewardArray[Float]
    tdNN.forward(input, currentOutput)

    tdNN.train(afterMoveOutput, eligibilityTrace)
  }

  def calculateWeightDelta(w1: Weights2D, w2: Weights2D): Float = {
    var sumSq = 0f

    for (h <- w1.inputHiddenWeights.indices; i <- w1.inputHiddenWeights(h).indices) {
      val diff = w1.inputHiddenWeights(h)(i) - w2.inputHiddenWeights(h)(i)
      sumSq += diff * diff
    }

    for (o <- w1.hiddenOutputWeights.indices; h <- w1.hiddenOutputWeights(o).indices) {
      val diff = w1.hiddenOutputWeights(o)(h) - w2.hiddenOutputWeights(o)(h)
      sumSq += diff * diff
    }

    math.sqrt(sumSq).toFloat
  }
}
