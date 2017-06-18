package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.bearoff.Bearoff;
import org.akoshterek.backgammon.bearoff.BearoffContext;
import org.akoshterek.backgammon.bearoff.BearoffGammon;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionId;

/**
 * @author Alex
 *         date: 18-06-17.
 */
class RaceBgProb {
    static float apply(final Board anBoard, final int side, final BearoffContext pbc1) {
        int totMenHome = 0;
        int totPipsOp = 0;

        for (int i = 0; i < 6; ++i)
            totMenHome += anBoard.anBoard()[side][i];

        for (int i = 22; i >= 18; --i)
            totPipsOp += anBoard.anBoard()[1 - side][i] * (i - 17);


        if (!((totMenHome + 3) / 4 - (side == 1 ? 1 : 0) <= (totPipsOp + 2) / 3))
            return 0.0f;

        Board dummy = new Board();
        System.arraycopy(anBoard.anBoard()[side], 0, dummy.anBoard()[side], 0, 25);
        System.arraycopy(anBoard.anBoard()[1 - side], 18, dummy.anBoard()[1 - side], 0, 6);
        for (int i = 6; i < 25; ++i) {
            dummy.anBoard()[1 - side][i] = 0;
        }

        long[] bgp = BearoffGammon.getRaceBGprobs(dummy.anBoard()[1 - side]);
        if (bgp.length > 0) {
            int k = PositionId.positionBearoff(anBoard.anBoard()[side], pbc1.points(), pbc1.chequers());
            short[] aProb = new short[32];
            float p = 0.0f;
            long scale = (side == 0) ? 36 : 1;
            Bearoff.bearoffDist(pbc1, k, null, null, null, aProb);

            for (int j = 1 - side; j < BearoffGammon.RBG_NPROBS(); j++) {
                long sum = 0;
                scale *= 36;
                for (int i = 1; i <= j + side; ++i)
                    sum += aProb[i];
                p += ((float)((Long)(bgp[j]))) / scale * sum;
            }

            p /= 65535.0;
            return p;
        } else {
            Reward p;
            if (PositionId.positionBearoff(dummy.anBoard()[0], 6, 15) > 923 ||
                    PositionId.positionBearoff(dummy.anBoard()[1], 6, 15) > 923) {
                p = Evaluator.evalBearoff1(dummy);
            } else {
                p = Evaluator.evalBearoff2(dummy);
            }

            float data[] = p.data().clone();
            return (side == 1 ? data[0] : 1 - data[0]);
        }
    }
}
