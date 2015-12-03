package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.agent.fa.SimpleEncogFA;
import org.akoshterek.backgammon.agent.inputrepresentation.GnuBgCodec;
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro89Codec;
import org.akoshterek.backgammon.agent.raw.RawRepresentation;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.util.Normalizer;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 29.11.2015.
 */
public class RawBatch40 extends AbsFlexAgent {
    public RawBatch40(Path path) {
        super(path);
        fullName = "RawBatch40";

        contactRepresentation = new RawRepresentation(new Tesauro89Codec());
        raceRepresentation = new RawRepresentation(new GnuBgCodec());
        crashedRepresentation = new RawRepresentation(new GnuBgCodec());

        contactFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-Tesauro89-40-contact.eg"));
        raceFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-GnuBg-40-race.eg"));
        crashedFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-GnuBg-40-crashed.eg"));
    }

    @Override
    public Reward evalContact(Board board) {
        Reward reward = super.evalContact(board);
        Normalizer.fromSmallerSigmoid(reward.data, Constants.NUM_OUTPUTS);
        return reward;
    }

    @Override
    public Reward evalRace(Board board) {
        Reward reward = super.evalRace(board);
        Normalizer.fromSmallerSigmoid(reward.data, Constants.NUM_OUTPUTS);
        return reward;
    }

    @Override
    public Reward evalCrashed(Board board) {
        Reward reward = super.evalCrashed(board);
        Normalizer.fromSmallerSigmoid(reward.data, Constants.NUM_OUTPUTS);
        return reward;
    }
}
