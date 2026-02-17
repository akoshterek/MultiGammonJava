package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.gnubg.GnubgAgent
import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent
import org.akoshterek.backgammon.agent.raw.RawTd40
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.util.OptionsBean

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
      case _ =>
        throw new IllegalArgumentException("Unknown agent name " + fullName)
    }
  }
}