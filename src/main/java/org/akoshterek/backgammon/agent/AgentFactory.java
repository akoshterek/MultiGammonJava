package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.eval.Evaluator;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public class AgentFactory {
    public static IAgent createAgent(String fullName) {
        String fullNameLower = fullName.toLowerCase();
        //String[] tokens = fullNameLower.split("-");

        IAgent agent;
        switch (fullNameLower) {
            case "random":
                agent = new RandomAgent(Evaluator.getInstance().getBasePath());
                break;
            case "heuristic":
                agent = new HeuristicAgent(Evaluator.getInstance().getBasePath());
                break;
            default:
                throw new IllegalArgumentException("Unknown agent name " + fullName);
        }

        return agent;
    }
}
