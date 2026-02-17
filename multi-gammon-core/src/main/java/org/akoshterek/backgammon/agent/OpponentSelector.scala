package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent

import java.nio.file.Path
import scala.util.Random

case class TrainingOpponentSpec(name: String, percentage: Int) {
  require(percentage >= 0 && percentage <= 100, "Percentage must be 0-100")
}

case class OpponentConfig(
  trainingOpponents: List[TrainingOpponentSpec],
  benchmarkOpponents: List[String]
) {
  require(trainingOpponents.map(_.percentage).sum == 100,
    "Training opponent percentages must sum to 100")

  lazy val cumulativeProbs: List[(String, Float)] = {
    var cumulative = 0.0f
    trainingOpponents.map { spec =>
      cumulative += spec.percentage / 100.0f
      (spec.name, cumulative)
    }
  }
}

object OpponentConfig {
  def parse(trainingStr: String, benchmarkStr: String): OpponentConfig = {
    val trainingSpecs = trainingStr.split(",").map { spec =>
      val parts = spec.trim.split(":")
      require(parts.length == 2, s"Invalid training opponent spec: $spec")
      TrainingOpponentSpec(parts(0).trim, parts(1).trim.toInt)
    }.toList

    val benchmarkNames = benchmarkStr.split(",").map(_.trim).toList

    OpponentConfig(trainingSpecs, benchmarkNames)
  }
}

class OpponentSelector(
  val mainAgent: CopyableAgent[Agent],
  val config: OpponentConfig,
  val basePath: Path,
  val seed: Long
) {
  private val random = new Random(seed)
  private val opponentCache = scala.collection.mutable.Map[String, Agent]()

  def selectTrainingOpponent(): Agent = {
    val roll = random.nextFloat()
    
    config.cumulativeProbs.find { case (_, cumProb) => roll < cumProb }
      .map { case (name, _) => getOrCreateOpponent(name) }
      .getOrElse(mainAgent.copyAgent())
  }

  private def getOrCreateOpponent(name: String): Agent = {
    opponentCache.getOrElseUpdate(name, createOpponent(name))
  }

  private def createOpponent(name: String): Agent = {
    name match {
      case "self" =>
        val copy = mainAgent.copyAgent()
        copy.isLearnMode = true
        copy
      case "Random" => new RandomAgent(basePath)
      case "SimpleHeuristic" => new SimpleHeuristicAgent(basePath)
      case "Heuristic" => new HeuristicAgent(basePath)
      case "PubEval" => PubEvalAgent(basePath)
      case _ => throw new IllegalArgumentException(s"Unknown opponent: $name")
    }
  }
  
  def getBenchmarkOpponents(): List[Agent] = {
    config.benchmarkOpponents.map(getOrCreateOpponent)
  }
}
