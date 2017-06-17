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
        new RandomAgent(Evaluator.getInstance.getBasePath)
      case "heuristic" =>
        new HeuristicAgent(Evaluator.getInstance.getBasePath)
      case "pubeval" =>
        PubEvalAgent(Evaluator.getInstance.getBasePath)
      case "gnubg" =>
        new GnubgAgent(Evaluator.getInstance.getBasePath)
      case "raw" =>
        RawBatch(Evaluator.getInstance.getBasePath, fullNameLower)
      case "rawrl40" =>
        val agent = new RawRl40(Evaluator.getInstance.getBasePath)
        agent.load()
        agent
      case "rawtd40" =>
        new RawTd40(Evaluator.getInstance.getBasePath)
      case _ =>
        throw new IllegalArgumentException("Unknown agent name " + fullName)
    }
  }
}