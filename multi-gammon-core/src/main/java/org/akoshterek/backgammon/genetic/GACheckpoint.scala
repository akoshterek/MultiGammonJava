package org.akoshterek.backgammon.genetic

import java.nio.file.{Files, Path, Paths}
import java.io.{PrintWriter, FileWriter}
import scala.io.Source
import org.akoshterek.backgammon.nn.NetworkWeights

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
  
  /**
   * Save checkpoint to JSON file
   */
  def save(checkpoint: GACheckpoint, path: Path): Unit = {
    val json = s"""{
  "generation": ${checkpoint.generation},
  "fitness": ${checkpoint.fitness},
  "population": ${checkpoint.population},
  "eliteCount": ${checkpoint.eliteCount},
  "mutationRate": ${checkpoint.mutationRate},
  "mutationStrength": ${checkpoint.mutationStrength},
  "inputSize": ${checkpoint.inputSize},
  "hiddenSize": ${checkpoint.hiddenSize},
  "outputSize": ${checkpoint.outputSize},
  "timestamp": "${checkpoint.timestamp}",
  "weights": {
    "wInputHidden": [${checkpoint.weights.wInputHidden.map(row => "[" + row.mkString(",") + "]").mkString(",")}],
    "wHiddenOutput": [${checkpoint.weights.wHiddenOutput.map(row => "[" + row.mkString(",") + "]").mkString(",")}],
    "bHidden": [${checkpoint.weights.bHidden.mkString(",")}],
    "bOutput": [${checkpoint.weights.bOutput.mkString(",")}]
  }
}"""
    
    Files.createDirectories(path.getParent)
    val writer = new PrintWriter(path.toFile)
    try {
      writer.write(json)
    } finally {
      writer.close()
    }
  }
  
  /**
   * Load checkpoint from JSON file
   */
  def load(path: Path): GACheckpoint = {
    val json = Source.fromFile(path.toFile).mkString
    // Simple JSON parsing (for production, use a proper JSON library)
    val generation = extractInt(json, "generation")
    val fitness = extractDouble(json, "fitness")
    val population = extractInt(json, "population")
    val eliteCount = extractInt(json, "eliteCount")
    val mutationRate = extractFloat(json, "mutationRate")
    val mutationStrength = extractFloat(json, "mutationStrength")
    val inputSize = extractInt(json, "inputSize")
    val hiddenSize = extractInt(json, "hiddenSize")
    val outputSize = extractInt(json, "outputSize")
    val timestamp = extractString(json, "timestamp")
    
    // Extract weights
    val weightsJson = json.substring(json.indexOf("\"weights\""))
    val wInputHidden = extractMatrix(weightsJson, "wInputHidden", hiddenSize, inputSize)
    val wHiddenOutput = extractMatrix(weightsJson, "wHiddenOutput", outputSize, hiddenSize)
    val bHidden = extractArray(weightsJson, "bHidden", hiddenSize)
    val bOutput = extractArray(weightsJson, "bOutput", outputSize)
    
    val weights = NetworkWeights(wInputHidden, wHiddenOutput, bHidden, bOutput)
    
    GACheckpoint(generation, fitness, population, eliteCount, mutationRate, mutationStrength,
                 inputSize, hiddenSize, outputSize, weights, timestamp)
  }
  
  private def extractInt(json: String, key: String): Int = {
    val pattern = s""""$key":\\s*(\\d+)""".r
    pattern.findFirstMatchIn(json).get.group(1).toInt
  }
  
  private def extractDouble(json: String, key: String): Double = {
    val pattern = s""""$key":\\s*([\\d.]+)""".r
    pattern.findFirstMatchIn(json).get.group(1).toDouble
  }
  
  private def extractFloat(json: String, key: String): Float = {
    val pattern = s""""$key":\\s*([\\d.]+)""".r
    pattern.findFirstMatchIn(json).get.group(1).toFloat
  }
  
  private def extractString(json: String, key: String): String = {
    val pattern = s""""$key":\\s*"([^"]+)"""".r
    pattern.findFirstMatchIn(json).get.group(1)
  }
  
  private def extractArray(json: String, key: String, size: Int): Array[Float] = {
    val pattern = s""""$key":\\s*\\[([^\\]]+)\\]""".r
    val values = pattern.findFirstMatchIn(json).get.group(1)
    values.split(",").map(_.trim.toFloat)
  }
  
  private def extractMatrix(json: String, key: String, rows: Int, cols: Int): Array[Array[Float]] = {
    val startPattern = s""""$key":\\s*\\[""".r
    val startMatch = startPattern.findFirstMatchIn(json).get
    val startIdx = startMatch.end
    
    // Find matching closing bracket using bracket counting
    var bracketCount = 1
    var idx = startIdx
    while (bracketCount > 0 && idx < json.length) {
      json(idx) match {
        case '[' => bracketCount += 1
        case ']' => bracketCount -= 1
        case _ =>
      }
      idx += 1
    }
    
    val content = json.substring(startIdx, idx - 1)
    
    val rowPattern = "\\[([^\\]]+)\\]".r
    val rowMatches = rowPattern.findAllMatchIn(content).toArray
    
    rowMatches.map { m =>
      m.group(1).split(",").map(_.trim.toFloat)
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
      val lines = Source.fromFile(bestPointerFile.toFile).getLines().toList
      val filename = lines.last.trim
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
      val lines = Source.fromFile(bestPointerFile.toFile).getLines().toList
      val fitnessLine = lines.find(_.startsWith("# Fitness:")).get
      fitnessLine.split(":")(1).trim.toDouble
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
