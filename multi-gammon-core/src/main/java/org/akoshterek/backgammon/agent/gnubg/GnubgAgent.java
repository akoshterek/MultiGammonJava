package org.akoshterek.backgammon.agent.gnubg;

import org.akoshterek.backgammon.agent.AbsAgent;
import org.akoshterek.backgammon.agent.gnubg.nn.GnuNeuralNets;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.file.Path;

import static org.akoshterek.backgammon.Constants.*;
import static org.akoshterek.backgammon.eval.Gammons.*;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public class GnubgAgent extends AbsAgent {
    private GnuBgRepresentation representation = new GnuBgRepresentation();

    public GnubgAgent(final Path path) {
        super("Gnubg", path);

        supportsSanityCheck_$eq(true);
        needsInvertedEval_$eq(true);
    }

    @Override
    public Reward evalRace(final Board board) {
        double[] inputs = representation.calculateRaceInputs(board);
        double reward[] = Reward.rewardArray();
        GnuNeuralNets.nnRace().evaluate(inputs, reward);

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
            any |= OG_POSSIBLE();

        if (totMen0 == 15)
            any |= G_POSSIBLE();

        any = calculateBackgammonPossibility(board, any);
        evaluatePossibleBackgammon(board, any, reward);
        // sanity check will take care of rest
        return new Reward(reward);
    }

    private static int calculateBackgammonPossibility(final Board board, final int gammonsFlag) {
        int any = gammonsFlag;
        int i;

        if ((any & OG_POSSIBLE()) != 0) {
            for (i = 23; i >= 18; --i) {
                if (board.anBoard()[Board.SELF()][i] > 0) {
                    break;
                }
            }

            if (i >= 18)
                any |= OBG_POSSIBLE();
        }

        if ((any & G_POSSIBLE()) != 0) {
            for (i = 23; i >= 18; --i) {
                if (board.anBoard()[Board.OPPONENT()][i] > 0)
                    break;
            }

            if (i >= 18)
                any |= BG_POSSIBLE();
        }

        return any;
    }

    private static void evaluatePossibleBackgammon(final Board board, final int gammonsFlag, final double reward[]) {
        if ((gammonsFlag & (BG_POSSIBLE() | OBG_POSSIBLE())) != 0) {
        /* side that can have the backgammon */
            int side = (gammonsFlag & BG_POSSIBLE()) != 0 ? 1 : 0;
            float pr = Evaluator.getInstance().raceBGprob(board, side);

            if (pr > 0.0) {
                if (side == 1) {
                    reward[OUTPUT_WINBACKGAMMON()] = pr;

                    if (reward[OUTPUT_WINGAMMON()] < reward[OUTPUT_WINBACKGAMMON()])
                        reward[OUTPUT_WINGAMMON()] = reward[OUTPUT_WINBACKGAMMON()];
                } else {
                    reward[OUTPUT_LOSEBACKGAMMON()] = pr;

                    if (reward[OUTPUT_LOSEGAMMON()] < reward[OUTPUT_LOSEBACKGAMMON()])
                        reward[OUTPUT_LOSEGAMMON()] = reward[OUTPUT_LOSEBACKGAMMON()];
                }
            } else {
                if (side == 1)
                    reward[OUTPUT_WINBACKGAMMON()] = 0.0;
                else
                    reward[OUTPUT_LOSEBACKGAMMON()] = 0.0;
            }
        }
    }

    @Override
    public Reward evalCrashed(final Board board) {
        double[] inputs = representation.calculateCrashedInputs(board);
        double[] reward = Reward.rewardArray();
        GnuNeuralNets.nnCrashed().evaluate(inputs, reward);
        return new Reward(reward);
    }

    @Override
    public Reward evalContact(final Board board) {
        double[] inputs = representation.calculateContactInputs(board);
        double[] reward = Reward.rewardArray();
        GnuNeuralNets.nnContact().evaluate(inputs, reward);
        return new Reward(reward);
    }
}
