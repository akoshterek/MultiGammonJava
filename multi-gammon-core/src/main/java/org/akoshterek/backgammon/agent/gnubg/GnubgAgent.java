package org.akoshterek.backgammon.agent.gnubg;

import com.google.common.io.LittleEndianDataInputStream;
import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.gnubg.nn.NeuralNet;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;

import java.io.DataInput;
import java.io.IOException;
import java.nio.file.Path;

import static org.akoshterek.backgammon.Constants.*;
import static org.akoshterek.backgammon.eval.Gammons.*;

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
        double[] inputs = representation.calculateRaceInputs(board);
        Reward reward = new Reward();
        nnRace.evaluate(inputs, reward.data);

        // anBoard[1] is on roll
        // total men for side not on roll
        int totMen0 = 0;

        // total men for side on roll
        int totMen1 = 0;

        // a set flag for every possible outcome
        int any = 0, i;

        for (i = 23; i >= 0; --i) {
            totMen0 += board.anBoard[0][i];
            totMen1 += board.anBoard[1][i];
        }

        if (totMen1 == 15)
            any |= OG_POSSIBLE;

        if (totMen0 == 15)
            any |= G_POSSIBLE;

        if (any != 0) {
            if ((any & OG_POSSIBLE) != 0) {
                for (i = 23; i >= 18; --i) {
                    if (board.anBoard[1][i] > 0) {
                        break;
                    }
                }

                if (i >= 18)
                    any |= OBG_POSSIBLE;
            }

            if ((any & G_POSSIBLE) != 0) {
                for (i = 23; i >= 18; --i) {
                    if (board.anBoard[0][i] > 0)
                        break;
                }

                if (i >= 18)
                    any |= BG_POSSIBLE;
            }
        }

        if ((any & (BG_POSSIBLE | OBG_POSSIBLE)) != 0) {
        /* side that can have the backgammon */
            int side = (any & BG_POSSIBLE) != 0 ? 1 : 0;
            float pr = Evaluator.getInstance().raceBGprob(board, side);

            if (pr > 0.0) {
                if (side == 1) {
                    reward.data[OUTPUT_WINBACKGAMMON] = pr;

                    if (reward.data[OUTPUT_WINGAMMON] < reward.data[OUTPUT_WINBACKGAMMON])
                        reward.data[OUTPUT_WINGAMMON] = reward.data[OUTPUT_WINBACKGAMMON];
                } else {
                    reward.data[OUTPUT_LOSEBACKGAMMON] = pr;

                    if (reward.data[OUTPUT_LOSEGAMMON] < reward.data[OUTPUT_LOSEBACKGAMMON])
                        reward.data[OUTPUT_LOSEGAMMON] = reward.data[OUTPUT_LOSEBACKGAMMON];
                }
            } else {
                if (side == 1)
                    reward.data[OUTPUT_WINBACKGAMMON] = 0.0;
                else
                    reward.data[OUTPUT_LOSEBACKGAMMON] = 0.0;
            }
        }

        // sanity check will take care of rest
        return reward;
    }

    @Override
    public Reward evalCrashed(Board board) {
        double[] inputs = representation.calculateCrashedInputs(board);
        Reward reward = new Reward();
        nnCrashed.evaluate(inputs, reward.data);
        return reward;
    }

    @Override
    public Reward evalContact(Board board) {
        double[] inputs = representation.calculateContactInputs(board);
        Reward reward = new Reward();
        nnContact.evaluate(inputs, reward.data);
        return reward;
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
