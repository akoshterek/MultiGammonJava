package org.akoshterek.backgammon.genetic

import java.nio.file.{Files, Path}
import java.io.PrintWriter
import scala.io.Source
import scala.util.Using
import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import org.akoshterek.backgammon.nn.NetworkWeights
import org.akoshterek.backgammon.util.JsonUtils

/**
 * Simple checkpoint format for GA agents
 */
case class GACheckpoint(
  generation: Int,
  fitness: Double,
  population: Int,
  eliteCount: Int,
  mutationRate: Float,
  mutationStrength: Float,
  inputSize: Int,
  hiddenSize: Int,
  outputSize: Int,
  weights: NetworkWeights,
  timestamp: String
)

object GACheckpoint {
  implicit val formats: Formats = Serialization.formats(NoTypeHints)
  
  /**
   * Save checkpoint to JSON file with pretty formatting
   */
  def save(checkpoint: GACheckpoint, path: Path): Unit = {
    JsonUtils.saveJsonPretty(checkpoint, path)
  }
  
  /**
   * Load checkpoint from JSON file
   */
  def load(path: Path): GACheckpoint = {
    try {
      val json = Using(Source.fromFile(path.toFile)) { source =>
        source.mkString
      }.get
      
      read[GACheckpoint](json)
    } catch {
      case e: Exception =>
        throw new RuntimeException(s"Failed to parse GA checkpoint JSON from $path", e)
    }
  }
}

/**
 * Tracks best checkpoint across all generations
 */
class BestCheckpointTracker(val basePath: Path) {
  private val bestPointerFile = basePath.resolve("best_ga_checkpoint.txt")
  
  /**
   * Update best checkpoint if new fitness is better
   */
  def updateIfBetter(checkpoint: GACheckpoint, checkpointPath: Path): Unit = {
    val currentBest = getBestFitness()
    
    if (checkpoint.fitness > currentBest) {
      // Save pointer to new best
      val writer = new PrintWriter(bestPointerFile.toFile)
      try {
        writer.println(s"# Best GA Checkpoint")
        writer.println(s"# Fitness: ${checkpoint.fitness}")
        writer.println(s"# Generation: ${checkpoint.generation}")
        writer.println(s"# Timestamp: ${checkpoint.timestamp}")
        writer.println(checkpointPath.getFileName.toString)
      } finally {
        writer.close()
      }
      
      println(s"  ✓ New best checkpoint saved: ${checkpointPath.getFileName} (fitness: ${checkpoint.fitness})")
    }
  }
  
  /**
   * Get path to best checkpoint
   */
  def getBestCheckpointPath(): Option[Path] = {
    if (Files.exists(bestPointerFile)) {
      val filename = Using(Source.fromFile(bestPointerFile.toFile)) { source =>
        source.getLines().toList.last.trim
      }.get
      Some(basePath.resolve(filename))
    } else {
      None
    }
  }
  
  /**
   * Get best fitness seen so far
   */
  def getBestFitness(): Double = {
    if (Files.exists(bestPointerFile)) {
      Using(Source.fromFile(bestPointerFile.toFile)) { source =>
        val fitnessLine = source.getLines().find(_.startsWith("# Fitness:")).get
        fitnessLine.split(":")(1).trim.toDouble
      }.get
    } else {
      Double.MinValue
    }
  }
  
  /**
   * Load best checkpoint
   */
  def loadBestCheckpoint(): Option[GACheckpoint] = {
    getBestCheckpointPath().map(GACheckpoint.load)
  }
}
