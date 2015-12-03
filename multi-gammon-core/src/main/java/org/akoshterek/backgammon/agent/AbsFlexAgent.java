package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.agent.fa.FunctionApproximator;
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 29.11.2015.
 */
public abstract class AbsFlexAgent extends AbsAgent {
    protected InputRepresentation contactRepresentation;
    protected InputRepresentation raceRepresentation;
    protected InputRepresentation crashedRepresentation;

    protected FunctionApproximator contactFa;
    protected FunctionApproximator raceFa;
    protected FunctionApproximator crashedFa;

    public AbsFlexAgent(Path path) {
        super(path);
    }

    @Override
    public Reward evalContact(Board board) {
        double[] inputs = contactRepresentation.calculateContactInputs(board);
        return contactFa.calculateReward(inputs);
    }

    @Override
    public Reward evalRace(Board board) {
        double[] inputs = raceRepresentation.calculateRaceInputs(board);
        return raceFa.calculateReward(inputs);
    }

    @Override
    public Reward evalCrashed(Board board) {
        double[] inputs = crashedRepresentation.calculateCrashedInputs(board);
        return crashedFa.calculateReward(inputs);
    }
}
