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
    private static final double MIN_ETRACE = 0.000001;
    private InputRepresentation representation = new RawRepresentation(Tesauro89Codec.self());
    private NeuralNetworkFA fa;
    private Map<String, ETraceEntry> eligibilityTraces;
    //private int step = 0;
    //private ETraceEntry prevEntry;
//    private Move prevMove;
    private static final float gamma = 1.0f;
    private static final float lambda = 0.7f;

    public RawRl40(Path path) {
        super("RawRl40", path);
        BasicNetwork network = SimpleEncogFA.createNN(representation.contactInputsCount(), 40);
        fa = new SimpleEncogFA(network);
        eligibilityTraces = new HashMap<>();
    }

    @Override
    public Reward evalContact(Board board) {
        float[] inputs = representation.calculateContactInputs(board);
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

    /*
    @Override
    public void startGame() {
        super.startGame();
        step = 0;
    }
    */

    @Override
    public void doMove(Move move) {
        super.doMove(move);
        if(!isLearnMode())
            return;

        //if(step == 0) {
        //    prepareStep0();
        //}

        Reward deltaReward = calcDeltaReward(move);
        //Board board = Board.positionFromKey(move.auch());
        ETraceEntry entry = new ETraceEntry(representation.calculateContactInputs(currentBoard()),
                /*move.pc()*/ curPC(),
                /*move.auch()*/currentBoard().calcPositionKey());
        eligibilityTraces.clear();
        eligibilityTraces.put(PositionId.positionIDFromKey(entry.auch), entry);
        updateETrace(deltaReward);

        //step++;
        //prevEntry = entry;
        //prevMove = move;
    }

    /*
    public void endGame() {
        super.endGame();
        //doMove(prevMove);
        step = 0;
    }
    */

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
                .sorted((o1, o2) -> Float.valueOf(o2.eTrace).compareTo(o1.eTrace))
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
        Board prevBoard = currentBoard();
        float prevQValue[] = evaluatePosition(prevBoard, Evaluator.getInstance().classifyPosition(prevBoard)).data();

        //Predicted greedy reward
        float predictedGreedyReward[] = move.arEvalMove().data();
        float deltaReward[] = new float[Constants.NUM_OUTPUTS()];
        for(int i = 0; i < deltaReward.length; i++) {
            deltaReward[i] = predictedGreedyReward[i] + predictedGreedyReward[i] * gamma - prevQValue[i];
        }

        return new Reward(deltaReward);
    }

    /*
    private void prepareStep0() {
        Board initBoard = Board.initialPosition();
        prevEntry = new ETraceEntry(representation.calculateContactInputs(initBoard),
                Evaluator.getInstance().classifyPosition(initBoard),
                initBoard.calcPositionKey());
    }
    */
}
