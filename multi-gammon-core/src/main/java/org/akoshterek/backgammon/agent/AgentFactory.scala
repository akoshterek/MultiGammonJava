package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.gnubg.GnubgAgent
import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent
import org.akoshterek.backgammon.agent.raw.{RawBatch, RawRl40, RawTd40}
import org.akoshterek.backgammon.eval.Evaluator

object AgentFactory {
  def createAgent(fullName: String): Agent = {
    val fullNameLower: String = fullName.toLowerCase
    val tokens = fullNameLower.split("-")

    tokens(0) match {
      case "random" =>
        new RandomAgent(Evaluator.basePath)
      case "heuristic" =>
        new HeuristicAgent(Evaluator.basePath)
      case "pubeval" =>
        PubEvalAgent(Evaluator.basePath)
      case "gnubg" =>
        new GnubgAgent(Evaluator.basePath)
      case "raw" =>
        RawBatch(Evaluator.basePath, fullNameLower)
      case "rawrl40" =>
        val agent = new RawRl40(Evaluator.basePath)
        agent.load()
        agent
      case "rawtd40" =>
        new RawTd40(Evaluator.basePath)
      case _ =>
        throw new IllegalArgumentException("Unknown agent name " + fullName)
    }
  }
}