package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.gnubg.GnubgAgent
import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent
import org.akoshterek.backgammon.agent.raw.RawBatch
import org.akoshterek.backgammon.agent.raw.RawRl40
import org.akoshterek.backgammon.eval.Evaluator

object AgentFactory {
    def createAgent(fullName: String): Agent = {
        val fullNameLower: String = fullName.toLowerCase
        //String[] tokens = fullNameLower.split("-");

        var agent: Agent = null
        fullNameLower match {
            case "random" =>
                agent = new RandomAgent(Evaluator.getInstance.getBasePath)
            case "heuristic" =>
                agent = new HeuristicAgent(Evaluator.getInstance.getBasePath)
            case "pubeval" =>
                agent =  PubEvalAgent.buildDefault(Evaluator.getInstance.getBasePath)
            case "gnubg" =>
                agent = new GnubgAgent(Evaluator.getInstance.getBasePath)
            case "rawbatch40" =>
                agent = new RawBatch(Evaluator.getInstance.getBasePath)
            case "rawrl40" =>
                agent = new RawRl40(Evaluator.getInstance.getBasePath)
                agent.load()
            case _ =>
                throw new IllegalArgumentException("Unknown agent name " + fullName)
        }
        agent
    }
}