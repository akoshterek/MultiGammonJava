package org.akoshterek.backgammon.nn

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.{read, writePretty}

import java.io.FileWriter
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import scala.io.Source
import scala.util.Using

case class Hyperparameters(
  alpha: Float,
  lambda: Float,
  gamma: Float
)

case class NetworkArchitecture(
  inputSize: Int,
  hiddenSize: Int,
  outputSize: Int,
  hiddenActivation: String,
  outputActivation: String
)

case class Performance(
  vsRandom: Float,
  vsHeuristic: Float
)

case class NetworkWeights(
  wInputHidden: Array[Array[Float]],
  wHiddenOutput: Array[Array[Float]],
  bHidden: Array[Float],
  bOutput: Array[Float]
)

case class CheckpointMetadata(
  formatVersion: String,
  timestamp: String,
  gamesPlayed: Int,
  experimentTag: String,
  hyperparameters: Hyperparameters,
  networkArchitecture: NetworkArchitecture,
  randomSeed: Long,
  performance: Option[Performance]
)

case class Checkpoint(
  metadata: CheckpointMetadata,
  weights: NetworkWeights
)

object CheckpointManager {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)

  /**
   * Save checkpoint to file atomically
   */
  def save(checkpoint: Checkpoint, path: Path): Unit = {
    val tempFile = Paths.get(path.toString + ".tmp")

    try {
      // Write to temp file with pretty formatting
      val json = writePretty(checkpoint)
      Using(new FileWriter(tempFile.toFile)) { writer =>
        writer.write(json)
      }.get

      // Atomic rename
      Files.move(tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
      println(s"Checkpoint saved: $path")
    } catch {
      case e: Exception =>
        // Clean up temp file on error
        Files.deleteIfExists(tempFile)
        throw new RuntimeException(s"Failed to save checkpoint to $path", e)
    }
  }

  /**
   * Load checkpoint from file
   */
  def load(path: Path): Checkpoint = {
    try {
      val json = Using(Source.fromFile(path.toFile)) { source =>
        source.mkString
      }.get

      val checkpoint = read[Checkpoint](json)
      println(s"Checkpoint loaded: $path")
      checkpoint
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Failed to parse checkpoint JSON from $path", e)
    }
  }

  /**
   * Find latest checkpoint in experiment directory
   */
  def findLatest(experimentPath: Path, experimentTag: String): Option[Path] = {
    val checkpointDir = experimentPath.resolve("checkpoints")

    if (!Files.exists(checkpointDir)) {
      return None
    }

    val pattern = s"${experimentTag}_checkpoint_(\\d{8})\\.json".r

    try {
      val checkpoints = Files.list(checkpointDir)
        .toArray
        .map(_.asInstanceOf[Path])
        .filter(_.getFileName.toString.endsWith(".json"))
        .flatMap { path =>
          val filename = path.getFileName.toString
          pattern.findFirstMatchIn(filename).map { m =>
            val gamesPlayed = m.group(1).toInt
            (gamesPlayed, path)
          }
        }

      if (checkpoints.isEmpty) {
        None
      } else {
        val (maxGames, latestPath) = checkpoints.maxBy(_._1)
        println(s"Found latest checkpoint: ${latestPath.getFileName} ($maxGames games)")
        Some(latestPath)
      }
    } catch {
      case e: Exception =>
        println(s"Warning: Error scanning for checkpoints: ${e.getMessage}")
        None
    }
  }

  /**
   * Validate checkpoint against current configuration
   */
  def validate(checkpoint: Checkpoint, currentArch: NetworkArchitecture,
               currentHyperparams: Hyperparameters): Unit = {
    val meta = checkpoint.metadata
    val ckptArch = meta.networkArchitecture
    val ckptHyperparams = meta.hyperparameters

    // Validate architecture
    if (ckptArch.inputSize != currentArch.inputSize ||
        ckptArch.hiddenSize != currentArch.hiddenSize ||
        ckptArch.outputSize != currentArch.outputSize) {
      throw new RuntimeException(
        s"Architecture mismatch:\n" +
        s"  Checkpoint: ${ckptArch.inputSize}→${ckptArch.hiddenSize}→${ckptArch.outputSize}\n" +
        s"  Current: ${currentArch.inputSize}→${currentArch.hiddenSize}→${currentArch.outputSize}"
      )
    }

    if (ckptArch.hiddenActivation != currentArch.hiddenActivation) {
      throw new RuntimeException(
        s"Activation function mismatch:\n" +
        s"  Checkpoint: ${ckptArch.hiddenActivation}\n" +
        s"  Current: ${currentArch.hiddenActivation}\n" +
        s"Action: Change TdNeuralNetwork.scala line 10 to match checkpoint, or delete checkpoint"
      )
    }

    // Validate lambda and gamma (must match)
    if (math.abs(ckptHyperparams.lambda - currentHyperparams.lambda) > 0.001f) {
      throw new RuntimeException(
        s"Lambda mismatch: checkpoint=${ckptHyperparams.lambda}, current=${currentHyperparams.lambda}\n" +
        s"Cannot change lambda mid-training. Use same lambda value, or delete checkpoint to start fresh."
      )
    }

    if (math.abs(ckptHyperparams.gamma - currentHyperparams.gamma) > 0.001f) {
      throw new RuntimeException(
        s"Gamma mismatch: checkpoint=${ckptHyperparams.gamma}, current=${currentHyperparams.gamma}\n" +
        s"Cannot change gamma mid-training. Use same gamma value, or delete checkpoint to start fresh."
      )
    }

    // Alpha can differ (override allowed)
    if (math.abs(ckptHyperparams.alpha - currentHyperparams.alpha) > 0.0001f) {
      println(s"⚠️  Alpha override: checkpoint=${ckptHyperparams.alpha}, using ${currentHyperparams.alpha}")
    }
  }

  /**
   * Generate checkpoint filename
   */
  def getCheckpointPath(experimentPath: Path, experimentTag: String, gamesPlayed: Int): Path = {
    val checkpointDir = experimentPath.resolve("checkpoints")
    Files.createDirectories(checkpointDir)
    checkpointDir.resolve(f"${experimentTag}_checkpoint_${gamesPlayed}%08d.json")
  }
}
