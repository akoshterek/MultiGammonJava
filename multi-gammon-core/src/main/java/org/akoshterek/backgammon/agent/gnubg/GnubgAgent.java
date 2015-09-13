package org.akoshterek.backgammon.agent.gnubg;

import com.google.common.io.LittleEndianDataInputStream;
import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.gnubg.nn.NeuralNet;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;

import java.io.DataInput;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public class GnubgAgent extends AbsAgent {
    private NeuralNet nnContact, nnRace, nnCrashed;
    private GnuBgRepresentation representation = new GnuBgRepresentation();

    public GnubgAgent(Path path) {
        super(path);
        fullName = "Gnubg";

        setSanityCheck(true);
        setNeedsInvertedEval(true);
        load();
    }

    @Override
    public Reward evalRace(Board board) {
        return null;
    }

    @Override
    public Reward evalCrashed(Board board) {
        return null;
    }

    @Override
    public Reward evalContact(Board board) {
        return null;
    }

    @Override
    public void load() {
        try (LittleEndianDataInputStream is = new LittleEndianDataInputStream(GnubgAgent.class.getResourceAsStream("/org/akoshterek/backgammon/gnu/gnubg.wd"))) {
            checkBinaryWeights(is);
            nnContact = NeuralNet.loadBinary(is);
            nnRace = NeuralNet.loadBinary(is);
            nnCrashed = NeuralNet.loadBinary(is);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkBinaryWeights(DataInput is) throws IOException {
        float magic = is.readFloat();
        float version = is.readFloat();

        if(magic != 472.3782f || version != 1.0f) {
            throw new IllegalArgumentException("Invalid weights file");
        }
    }
}
