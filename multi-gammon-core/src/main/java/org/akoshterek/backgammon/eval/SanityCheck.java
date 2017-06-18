package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.bearoff.Bearoff;
import org.akoshterek.backgammon.bearoff.BearoffContext;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionId;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date: 18-06-17.
 */
class SanityCheck {
    static Reward apply(final Board board, final Reward reward, final BearoffContext pbc1) {
        int i, j, nciq;
        int[] ac = new int[2];
        int[] anBack = new int[2];
        int[] anCross = new int[2];
        int[] anGammonCross = new int[2];
        int[] anBackgammonCross = new int[2];
        int[] anMaxTurns = new int[2];
        boolean fContact;
        float data[] = reward.data().clone();

        if (data[OUTPUT_WIN()] < 0.0f) {
            data[OUTPUT_WIN()] = 0.0f;
        } else if (data[OUTPUT_WIN()] > 1.0f) {
            data[OUTPUT_WIN()] = 1.0f;
        }

        ac[0] = ac[1] = anBack[0] = anBack[1] = anCross[0] =
                anCross[1] = anBackgammonCross[0] = anBackgammonCross[1] = 0;
        anGammonCross[0] = anGammonCross[1] = 1;

        for (j = 0; j < 2; j++) {
            for (i = 0, nciq = 0; i < 6; i++)
                if (board.anBoard()[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard()[j][i];
                }
            ac[j] = anCross[j] = nciq;

            for (i = 6, nciq = 0; i < 12; i++)
                if (board.anBoard()[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard()[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 2 * nciq;
            anGammonCross[j] += nciq;

            for (i = 12, nciq = 0; i < 18; i++)
                if (board.anBoard()[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard()[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 3 * nciq;
            anGammonCross[j] += 2 * nciq;

            for (i = 18, nciq = 0; i < 24; i++)
                if (board.anBoard()[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard()[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 4 * nciq;
            anGammonCross[j] += 3 * nciq;
            anBackgammonCross[j] = nciq;

            if (board.anBoard()[j][24] != 0) {
                anBack[j] = 24;
                ac[j] += board.anBoard()[j][24];
                anCross[j] += 5 * board.anBoard()[j][24];
                anGammonCross[j] += 4 * board.anBoard()[j][24];
                anBackgammonCross[j] += 2 * board.anBoard()[j][24];
            }
        }

        fContact = anBack[0] + anBack[1] >= 24;

        if (!fContact) {
            for (i = 0; i < 2; i++) {
                if (anBack[i] < 6) {
                    anMaxTurns[i] = maxTurns(
                            PositionId.positionBearoff(board.anBoard()[i], pbc1.points(), pbc1.chequers()),
                            pbc1);
                } else {
                    anMaxTurns[i] = anCross[i] * 2;
                }
            }

            if (anMaxTurns[1] == 0) {
                anMaxTurns[1] = 1;
            }
        }

        if (!fContact && anCross[0] > 4 * (anMaxTurns[1] - 1)) {
            // Certain win
            data[OUTPUT_WIN()] = 1.0f;
        }

        if (ac[0] < 15) {
            // Opponent has borne off; no gammons or backgammons possible
            data[OUTPUT_WINGAMMON()] = data[OUTPUT_WINBACKGAMMON()] = 0.0f;
        } else if (!fContact) {
            if (anCross[1] > 8 * anGammonCross[0]) {
                // Gammon impossible
                data[OUTPUT_WINGAMMON()] = 0.0f;
            } else if (anGammonCross[0] > 4 * (anMaxTurns[1] - 1)) {
                // Certain gammon
                data[OUTPUT_WINGAMMON()] = 1.0f;
            }

            if (anCross[1] > 8 * anBackgammonCross[0]) {
                // Backgammon impossible
                data[OUTPUT_WINBACKGAMMON()] = 0.0f;
            } else if (anBackgammonCross[0] > 4 * (anMaxTurns[1] - 1)) {
                // Certain backgammon
                data[OUTPUT_WINGAMMON()] = data[OUTPUT_WINBACKGAMMON()] = 1.0f;
            }
        }

        if (!fContact && anCross[1] > 4 * anMaxTurns[0]) {
            // Certain loss
            data[OUTPUT_WIN()] = 0.0f;
        }

        if (ac[1] < 15) {
            // Player has borne off; no gammon or backgammon losses possible
            data[OUTPUT_LOSEGAMMON()] = data[OUTPUT_LOSEBACKGAMMON()] = 0.0f;
        } else if (!fContact) {
            if (anCross[0] > 8 * anGammonCross[1] - 4) {
                // Gammon loss impossible
                data[OUTPUT_LOSEGAMMON()] = 0.0f;
            } else if (anGammonCross[1] > 4 * anMaxTurns[0]) {
                // Certain gammon loss
                data[OUTPUT_LOSEGAMMON()] = 1.0f;
            }

            if (anCross[0] > 8 * anBackgammonCross[1] - 4) {
                // Backgammon loss impossible
                data[OUTPUT_LOSEBACKGAMMON()] = 0.0f;
            } else if (anBackgammonCross[1] > 4 * anMaxTurns[0]) {
                // Certain backgammon loss
                data[OUTPUT_LOSEGAMMON()] = data[OUTPUT_LOSEBACKGAMMON()] = 1.0f;
            }
        }

        // gammons must be less than wins
        if (data[OUTPUT_WINGAMMON()] > data[OUTPUT_WIN()])
            data[OUTPUT_WINGAMMON()] = data[OUTPUT_WIN()];

        float lose = 1.0f - data[OUTPUT_WIN()];
        if (data[OUTPUT_LOSEGAMMON()] > lose)
            data[OUTPUT_LOSEGAMMON()] = lose;

        // Backgammons cannot exceed gammons
        if (data[OUTPUT_WINBACKGAMMON()] > data[OUTPUT_WINGAMMON()])
            data[OUTPUT_WINBACKGAMMON()] = data[OUTPUT_WINGAMMON()];

        if (data[OUTPUT_LOSEBACKGAMMON()] > data[OUTPUT_LOSEGAMMON()])
            data[OUTPUT_LOSEBACKGAMMON()] = data[OUTPUT_LOSEGAMMON()];

        final double noise = 1 / 10000.0f;
        for (i = OUTPUT_WINGAMMON(); i < NUM_OUTPUTS(); i++) {
            if (data[i] < noise) {
                data[i] = 0;
            }
        }

        return  new Reward(data);
    }

    private static int maxTurns(final int id, final BearoffContext pbc1) {
        short[] aus = new short[32];

        Bearoff.bearoffDist(pbc1, id, null, null, null, aus);
        for (int i = 31; i >= 0; i--) {
            if (aus[i] != 0)
                return i;
        }

        return -1;
    }
}
