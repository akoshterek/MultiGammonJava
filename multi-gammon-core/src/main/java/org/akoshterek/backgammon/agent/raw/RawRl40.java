package org.akoshterek.backgammon.agent.raw;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.ETraceEntry;
import org.akoshterek.backgammon.agent.fa.NeuralNetworkFA;
import org.akoshterek.backgammon.agent.fa.SimpleEncogFA;
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro89Codec;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.board.PositionId;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.move.Move;
import org.encog.neural.networks.BasicNetwork;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex
 *         date: 05.12.2015
 */
public class RawRl40 extends AbsAgent implements Cloneable {
    private static final double MIN_ETRACE = 0.00000001;
    private InputRepresentation representation = new RawRepresentation(new Tesauro89Codec());
    private NeuralNetworkFA fa;
    private Map<String, ETraceEntry> eligibilityTraces;
    private int step = 0;
    private ETraceEntry prevEntry;
//    private Move prevMove;
    private static final double gamma = 1.0;
    private static final double lambda = 0.7;

    public RawRl40(Path path) {
        super("RawRl40", path);
        fixed_$eq(false);
        supportsSanityCheck_$eq(false);

        BasicNetwork network = SimpleEncogFA.createNN(representation.getContactInputsCount(), 40);
        fa = new SimpleEncogFA(network);
        supportsBearoff_$eq(false);
        eligibilityTraces = new HashMap<>();
    }

    @Override
    public Reward evalContact(Board board) {
        double[] inputs = representation.calculateContactInputs(board);
        return fa.calculateReward(inputs);
    }

    @Override
    public Reward evalRace(Board board) {
        return evalContact(board);
    }

    @Override
    public Reward evalCrashed(Board board) {
        return evalContact(board);
    }

    public RawRl40 clone() {
        RawRl40 other = (RawRl40)super.clone();
        other.fa = fa;
        other.representation = representation;
        other.eligibilityTraces = new HashMap<>();
        return other;
    }

    @Override
    public void startGame() {
        super.startGame();
        step = 0;
    }

    @Override
    public void doMove(Move move) {
        super.doMove(move);
        if(!isLearnMode())
            return;

        if(step == 0) {
            prepareStep0();
        }

        Reward deltaReward = calcDeltaReward(move);
        Board board = Board.positionFromKey(move.auch);
        ETraceEntry entry = new ETraceEntry(representation.calculateContactInputs(board),
                move.pc,
                move.auch);
        eligibilityTraces.clear();
        eligibilityTraces.put(PositionId.positionIDFromKey(entry.auch), entry);
        updateETrace(deltaReward);

        step++;
        prevEntry = entry;
        //prevMove = move;
    }

    public void endGame() {
        super.endGame();
        //doMove(prevMove);
        step = 0;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {
        Path folder = path().resolve(fullName());
        folder.toFile().mkdirs();
        Path nn = folder.resolve(fullName() + ".eg");
        fa.saveNN(nn);
    }

    public Reward evaluatePosition(Board board, PositionClass pc) {
        Reward reward;
        switch (pc) {
            case CLASS_OVER:
                reward = evalOver(board);
                break;
            default:
                reward = evalContact(board);
        }

        for(int i = 1; i < Constants.NUM_OUTPUTS; i++) {
            reward.data()[i] = 0;
        }
        return reward;
    }


    private void updateETrace(Reward deltaReward) {
        eligibilityTraces
                .values()
                .stream()
                .sorted((o1, o2) -> Double.valueOf(o2.eTrace).compareTo(o1.eTrace))
                .forEach(entry -> updateTraceEntry(entry, deltaReward));
        eligibilityTraces
                .values()
                .removeIf(entry -> entry.eTrace < MIN_ETRACE);
    }

    private void updateTraceEntry(ETraceEntry entry, Reward deltaReward) {
        fa.updateAddToReward(entry.input, deltaReward.$times(entry.eTrace));
        entry.eTrace *= lambda;
    }

    /**
     * Q update rule
     */
    private Reward calcDeltaReward(final Move move) {
        //prev reward
        Board prevBoard = Board.positionFromKey(prevEntry.auch);
        Reward prevQValue = evaluatePosition(prevBoard, prevEntry.pc);

        //Predicted greedy reward
        Reward predictedGreedyReward = move.arEvalMove;
        Reward deltaReward = new Reward();
        for(int i = 0; i < 1/*Constants.NUM_OUTPUTS*/; i++) {
            deltaReward.data()[i] = /*reward.data[i] +*/ predictedGreedyReward.data()[i] * gamma - prevQValue.data()[i];
            //deltaReward.data[i] *= 0.3;
        }

        return deltaReward;
    }

    private void prepareStep0() {
        Board initBoard = new Board();
        initBoard.initBoard();
        prevEntry = new ETraceEntry(representation.calculateContactInputs(initBoard),
                Evaluator.getInstance().classifyPosition(initBoard),
                initBoard.calcPositionKey());
    }
}
