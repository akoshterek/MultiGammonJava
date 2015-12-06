package org.akoshterek.backgammon.agent.raw;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.ETraceEntry;
import org.akoshterek.backgammon.agent.fa.FunctionApproximator;
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
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Alex
 *         date: 05.12.2015
 */
public class RawRl40  extends AbsAgent {
    private InputRepresentation representation = new RawRepresentation(new Tesauro89Codec());
    private FunctionApproximator fa;
    private Map<String, ETraceEntry> eligibilityTraces = new TreeMap<>();
    private int step = 0;
    private ETraceEntry prevEntry;
    private double gamma = 0.7;

    public RawRl40(Path path) {
        super(path);
        fullName = "RawRl40";
        fixed = false;
        setSanityCheck(false);

        BasicNetwork network = SimpleEncogFA.createNN(representation.getContactInputsCount(), 40);
        fa = new SimpleEncogFA(network);
    }

    @Override
    public Reward evalContact(Board board) {
        return null;
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
        return other;
    }

    @Override
    public void startGame() {
        super.startGame();
        eligibilityTraces.clear();
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

        Reward reward = (move.pc == PositionClass.CLASS_OVER) ? move.arEvalMove : new Reward();
        Reward deltaReward = calcDeltaReward(move, reward);

        Board board = Board.positionFromKey(move.auch);
        ETraceEntry entry = new ETraceEntry(representation.calculateContactInputs(board),
                move.pc,
                move.auch);
        eligibilityTraces.put(PositionId.positionIDFromKey(entry.auch), entry);
        updateETrace(deltaReward);

        step++;
        prevEntry = entry;
    }

    public void endGame() {
        super.endGame();
        step = 0;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {
        //Path folder = getPath().resolve(fullName);
    }

    private void updateETrace(Reward deltaReward) {
        eligibilityTraces.entrySet().stream().forEach(entry -> updateTraceEntry(entry, deltaReward));
        eligibilityTraces.entrySet().removeIf(entry -> entry.getValue().eTrace < 0.0000001);
    }

    private Map.Entry<String, ETraceEntry> updateTraceEntry(Map.Entry<String, ETraceEntry> entry, Reward deltaReward) {
        ETraceEntry eTraceEntry = entry.getValue();
        fa.updateAddToReward(eTraceEntry.input, deltaReward.multiply(eTraceEntry.eTrace));
        eTraceEntry.eTrace *= gamma;
        return entry;
    }

    /**
     * Q update rule
     */
    private Reward calcDeltaReward(final Move move, final Reward reward) {
        //prev reward
        Board prevBoard = Board.positionFromKey(prevEntry.auch);
        Reward prevQValue = evaluatePosition(prevBoard, prevEntry.pc);

        //Predicted greedy reward
        Reward predictedGreedyReward = move.arEvalMove;
        Reward deltaReward = new Reward();
        for(int i = 0; i < Constants.NUM_OUTPUTS; i++) {
            deltaReward.data[i] = reward.data[i] + predictedGreedyReward.data[i] * gamma - prevQValue.data[i];
        }

        return deltaReward;
    }

    private void prepareStep0() {
        Board initBoard = new Board();
        initBoard.initBoard();
        prevEntry = new ETraceEntry(representation.calculateContactInputs(initBoard),
                Evaluator.getInstance().classifyPosition(initBoard),
                initBoard.PositionKey());
    }
}
