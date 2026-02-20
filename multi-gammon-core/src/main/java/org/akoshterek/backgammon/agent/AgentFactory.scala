package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.gnubg.GnubgAgent
import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent
import org.akoshterek.backgammon.agent.raw.RawTd40
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.genetic.GeneticAgent
import org.akoshterek.backgammon.util.OptionsBean

import java.nio.file.Paths

object AgentFactory {
  def createAgent(fullName: String, options: OptionsBean): Agent = {
    val fullNameLower: String = fullName.toLowerCase
    val tokens = fullNameLower.split("-")

    tokens(0) match {
      case "random" =>
        new RandomAgent(Evaluator.basePath)
      case "heuristic" =>
        new HeuristicAgent(Evaluator.basePath)
      case "simpleheuristic" =>
        new SimpleHeuristicAgent(Evaluator.basePath)
      case "pubeval" =>
        PubEvalAgent(Evaluator.basePath)
      case "gnubg" =>
        new GnubgAgent(Evaluator.basePath)
      case "rawtd40" =>
        new RawTd40(Evaluator.basePath, options.alpha, options.lambda, options.gamma, options.experimentRunTag, isCopy = false, originalSeed = 16000000L,
                   useOutputBias = options.useOutputBias)

      case "geneticagent" =>
        GeneticAgent.fromPath(Evaluator.basePath, 6);


      case "geneticagent5" =>
        val path = Paths.get("experiments_ga/005_pubeval_5out")
        GeneticAgent.fromPath(path, 5);
      case "geneticagent6" =>
        val path = Paths.get("experiments_ga/006_pubeval_5out")
        GeneticAgent.fromPath(path, 6);

      case _ =>
        throw new IllegalArgumentException("Unknown agent name " + fullName)
    }
  }
}