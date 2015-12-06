package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.agent.gnubg.GnubgAgent;
import org.akoshterek.backgammon.agent.pubeval.PubEvalAgent;
import org.akoshterek.backgammon.agent.raw.RawBatch40;
import org.akoshterek.backgammon.agent.raw.RawRl40;
import org.akoshterek.backgammon.eval.Evaluator;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public class AgentFactory {
    public static Agent createAgent(String fullName) {
        String fullNameLower = fullName.toLowerCase();
        //String[] tokens = fullNameLower.split("-");

        Agent agent;
        switch (fullNameLower) {
            case "random":
                agent = new RandomAgent(Evaluator.getInstance().getBasePath());
                break;
            case "heuristic":
                agent = new HeuristicAgent(Evaluator.getInstance().getBasePath());
                break;
            case "pubeval":
                agent = new PubEvalAgent(Evaluator.getInstance().getBasePath());
                break;
            case "gnubg":
                agent = new GnubgAgent(Evaluator.getInstance().getBasePath());
                break;
            case "rawbatch40":
                agent = new RawBatch40(Evaluator.getInstance().getBasePath());
                break;
            case "rawrl40":
                agent = new RawRl40(Evaluator.getInstance().getBasePath());
                agent.load();
                break;
            default:
                throw new IllegalArgumentException("Unknown agent name " + fullName);
        }

        return agent;
    }
}
