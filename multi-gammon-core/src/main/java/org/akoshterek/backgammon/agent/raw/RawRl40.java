package org.akoshterek.backgammon.agent.raw;

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
    private InputRepresentation representation = new RawRepresentation(Tesauro89Codec.self());
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

        BasicNetwork network = SimpleEncogFA.createNN(representation.contactInputsCount(), 40);
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
        Board board = Board.positionFromKey(move.auch());
        ETraceEntry entry = new ETraceEntry(representation.calculateContactInputs(board),
                move.pc(),
                move.auch());
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
        if (!folder.toFile().exists()) {
            folder.toFile().mkdirs();
        }
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
        double prevQValue[] = evaluatePosition(prevBoard, prevEntry.pc).toArray();

        //Predicted greedy reward
        double predictedGreedyReward[] = move.arEvalMove().toArray();
        double deltaReward[] = Reward.rewardArray();
        for(int i = 0; i < 1/*Constants.NUM_OUTPUTS*/; i++) {
            deltaReward[i] = /*reward.data[i] +*/ predictedGreedyReward[i] * gamma - prevQValue[i];
            //deltaReward.data[i] *= 0.3;
        }

        return new Reward(deltaReward);
    }

    private void prepareStep0() {
        Board initBoard = Board.initialPosition();
        prevEntry = new ETraceEntry(representation.calculateContactInputs(initBoard),
                Evaluator.getInstance().classifyPosition(initBoard),
                initBoard.calcPositionKey());
    }
}
