package org.akoshterek.backgammon.agent.util;

import org.akoshterek.backgammon.agent.Agent;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.move.Move;

import java.util.concurrent.Callable;

/**
 * @author Alex
 *         date: 04.01.2016
 */
public class ScoreMoveCallable implements Callable<Reward> {
    private final Agent agent;
    private final Move move;

    public ScoreMoveCallable(Agent agent, Move move) {
        this.agent = agent;
        this.move = move;
    }

    @Override
    public Reward call() throws Exception {
        return agent.scoreMove(move);
    }
}
