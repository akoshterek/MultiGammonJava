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

    public GnubgAgent(final Path path) {
        super(path);
        fullName = "Gnubg";

        setSanityCheck(true);
        setNeedsInvertedEval(true);
        load();
    }

    @Override
    public Reward evalRace(final Board board) {
        double[] inputs = representation.calculateRaceInputs(board);
        Reward reward = new Reward();
        nnRace.evaluate(inputs, reward.data());

        // anBoard[1] is on roll
        // total men for side not on roll
        int totMen0;
        // total men for side on roll
        int totMen1;

        // a set flag for every possible outcome
        int any = 0;

        scala.Tuple2 mens = board.chequersCount();
        totMen0 = (Integer)mens._1();
        totMen1 = (Integer)mens._2();

        if (totMen1 == 15)
            any |= OG_POSSIBLE;

        if (totMen0 == 15)
            any |= G_POSSIBLE;

        any = calculateBackgammonPossibility(board, any);
        evaluatePossibleBackgammon(board, any, reward);
        // sanity check will take care of rest
        return reward;
    }

    private static int calculateBackgammonPossibility(final Board board, final int gammonsFlag) {
        int any = gammonsFlag;
        int i;

        if ((any & OG_POSSIBLE) != 0) {
            for (i = 23; i >= 18; --i) {
                if (board.anBoard()[Board.SELF()][i] > 0) {
                    break;
                }
            }

            if (i >= 18)
                any |= OBG_POSSIBLE;
        }

        if ((any & G_POSSIBLE) != 0) {
            for (i = 23; i >= 18; --i) {
                if (board.anBoard()[Board.OPPONENT()][i] > 0)
                    break;
            }

            if (i >= 18)
                any |= BG_POSSIBLE;
        }

        return any;
    }

    private static void evaluatePossibleBackgammon(final Board board, final int gammonsFlag, final Reward reward) {
        if ((gammonsFlag & (BG_POSSIBLE | OBG_POSSIBLE)) != 0) {
        /* side that can have the backgammon */
            int side = (gammonsFlag & BG_POSSIBLE) != 0 ? 1 : 0;
            float pr = Evaluator.getInstance().raceBGprob(board, side);

            if (pr > 0.0) {
                if (side == 1) {
                    reward.data()[OUTPUT_WINBACKGAMMON] = pr;

                    if (reward.data()[OUTPUT_WINGAMMON] < reward.data()[OUTPUT_WINBACKGAMMON])
                        reward.data()[OUTPUT_WINGAMMON] = reward.data()[OUTPUT_WINBACKGAMMON];
                } else {
                    reward.data()[OUTPUT_LOSEBACKGAMMON] = pr;

                    if (reward.data()[OUTPUT_LOSEGAMMON] < reward.data()[OUTPUT_LOSEBACKGAMMON])
                        reward.data()[OUTPUT_LOSEGAMMON] = reward.data()[OUTPUT_LOSEBACKGAMMON];
                }
            } else {
                if (side == 1)
                    reward.data()[OUTPUT_WINBACKGAMMON] = 0.0;
                else
                    reward.data()[OUTPUT_LOSEBACKGAMMON] = 0.0;
            }
        }
    }

    @Override
    public Reward evalCrashed(final Board board) {
        double[] inputs = representation.calculateCrashedInputs(board);
        Reward reward = new Reward();
        nnCrashed.evaluate(inputs, reward.data());
        return reward;
    }

    @Override
    public Reward evalContact(final Board board) {
        double[] inputs = representation.calculateContactInputs(board);
        Reward reward = new Reward();
        nnContact.evaluate(inputs, reward.data());
        return reward;
    }

    @Override
    public void load() {
        try (LittleEndianDataInputStream is = new LittleEndianDataInputStream(
            GnubgAgent.class.getResourceAsStream("/org/akoshterek/backgammon/gnu/gnubg.wd"))) {
            checkBinaryWeights(is);
            nnContact = NeuralNet.loadBinary(is);
            nnRace = NeuralNet.loadBinary(is);
            nnCrashed = NeuralNet.loadBinary(is);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkBinaryWeights(final DataInput is) throws IOException {
        float magic = is.readFloat();
        float version = is.readFloat();

        if(magic != 472.3782f || version != 1.0f) {
            throw new IllegalArgumentException("Invalid weights file");
        }
    }
}
