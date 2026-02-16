package org.akoshterek.backgammon.agent.raw

import java.nio.file.{Files, Path}
import java.time.Instant
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro92Codec
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.{Evaluator, Reward}
import org.akoshterek.backgammon.move.Move
import org.akoshterek.backgammon.nn._
import org.akoshterek.backgammon.dice.PseudoRandomDiceRoller

import scala.util.Using


class RawTd40(override val path: Path,
              var alpha: Float = 0.01f,
              val lambda: Float = 0.7f,
              val gamma: Float = 1.0f,
              val experimentTag: String = "",
              val isCopy: Boolean = false,
              val originalSeed: Long = 16000000L
             ) extends AbsAgent("RawTd40", path) {
  private val representation = new RawRepresentation(Tesauro92Codec)
  // shared NN
  private var tdNN = new TdNeuralNetwork(representation.contactInputsCount, 40, 1, alpha, lambda, gamma)
  private var eligibilityTrace: EligibilityTrace2D = _
  private var weights: Weights2D = _

  // Try to load checkpoint on startup (only for non-copy instances)
  if (!isCopy && experimentTag.nonEmpty) {
    tryLoadCheckpoint()
  }

  private val filePath: Path = path.resolve(s"${if (experimentTag.nonEmpty) experimentTag + "_" else ""}${fullName}_td_metrics.csv")
  Files.createDirectories(filePath.getParent) // ensures path exists
  private val metricsFile = filePath.toFile

  if (!metricsFile.exists()) {
    val writer = new java.io.PrintWriter(metricsFile)
    writer.println("gamesPlayed,averageTDError,weightDelta,weightMean,weightStdDev,weightMaxAbs,weightNearZero,weightLarge")
    writer.close()
  }

  private def tryLoadCheckpoint(): Unit = {
    CheckpointManager.findLatest(path, experimentTag) match {
      case Some(checkpointPath) =>
        try {
          val checkpoint = CheckpointManager.load(checkpointPath)

          // Create current configuration for validation
          val currentArch = NetworkArchitecture(
            inputSize = representation.contactInputsCount,
            hiddenSize = 40,
            outputSize = 1,
            hiddenActivation = "LeakyReLU",  // Should match TdNeuralNetwork.scala:10
            outputActivation = "Sigmoid"
          )

          val currentHyperparams = Hyperparameters(alpha, lambda, gamma)

          // Validate checkpoint
          CheckpointManager.validate(checkpoint, currentArch, currentHyperparams)

          // Load weights
          tdNN.loadWeights(checkpoint.weights)

          // Restore game counter
          setPlayedGames(checkpoint.metadata.gamesPlayed)

          // Update random seed (deterministic offset)
          val newSeed = checkpoint.metadata.randomSeed + checkpoint.metadata.gamesPlayed
          Evaluator.diceRoller = PseudoRandomDiceRoller(newSeed)

          // Alpha might have been overridden - use command line value
          alpha = currentHyperparams.alpha

          println(s"✅ Resumed from checkpoint: ${checkpoint.metadata.gamesPlayed} games")
          println(s"   Dice seed updated: $newSeed")
        } catch {
          case e: Exception =>
            System.err.println(s"❌ Failed to load checkpoint: ${e.getMessage}")
            throw e
        }

      case None =>
        println(s"No checkpoint found for tag '$experimentTag', starting fresh training")
    }
  }

  override def copyAgent(): RawTd40 = {
        val other: RawTd40 = new RawTd40(path, alpha, lambda, gamma, experimentTag, isCopy = true, originalSeed)
        other.tdNN = tdNN
        other.setPlayedGames(this.playedGames)
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
      val weightStats = tdNN.analyzeWeights()

      // Append metrics to CSV
      Using(new java.io.PrintWriter(new java.io.FileOutputStream(metricsFile, true))) { writer =>
        writer.println(s"$playedGames, $averageTdError, $delta, ${weightStats.mean}, ${weightStats.stdDev}, ${weightStats.maxAbs}, ${weightStats.nearZeroCount}, ${weightStats.largeCount}")
      }

      weights = snapshot

      // Console output every 50K games (less verbose)
      if (playedGames % 50000 == 0) {
        println(s"\n[$playedGames games] ${weightStats.prettyPrint}")
        weightStats.healthWarnings.foreach(println)
        println()

        // Save checkpoint every 50K games
        if (experimentTag.nonEmpty) {
          saveCheckpoint(playedGames)
        }
      }
    }

    playedGames
  }

  private def saveCheckpoint(gamesPlayed: Int): Unit = {
    try {
      val checkpointPath = CheckpointManager.getCheckpointPath(path, experimentTag, gamesPlayed)

      val metadata = CheckpointMetadata(
        formatVersion = "1.0",
        timestamp = Instant.now().toString,
        gamesPlayed = gamesPlayed,
        experimentTag = experimentTag,
        hyperparameters = Hyperparameters(alpha, lambda, gamma),
        networkArchitecture = NetworkArchitecture(
          inputSize = representation.contactInputsCount,
          hiddenSize = 40,
          outputSize = 1,
          hiddenActivation = "LeakyReLU",
          outputActivation = "Sigmoid"
        ),
        randomSeed = originalSeed,
        performance = None  // Could add benchmark results here if available
      )

      val checkpoint = Checkpoint(
        metadata = metadata,
        weights = tdNN.saveWeights()
      )

      CheckpointManager.save(checkpoint, checkpointPath)
    } catch {
      case e: Exception =>
        System.err.println(s"Warning: Failed to save checkpoint: ${e.getMessage}")
        // Don't crash training on checkpoint save failure
    }
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
