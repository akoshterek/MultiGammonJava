package org.akoshterek.backgammon.bearoff;

import com.google.common.primitives.Ints;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionId;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date 01.09.2015.
 */
public class Bearoff {
    public static boolean isBearoff(BearoffContext pbc, Board board) {
        int i, nOppBack, nBack;
        int n = 0, nOpp = 0;

        int[][] anBoard = board.anBoard();
        for (nOppBack = 24; nOppBack > 0; nOppBack--) {
            if (anBoard[0][nOppBack] != 0)
                break;
        }
        for (nBack = 24; nBack > 0; nBack--) {
            if (anBoard[1][nBack] != 0)
                break;
        }
        if (anBoard[0][nOppBack] == 0 || anBoard[1][nBack] == 0) {
            // the game is over
            return false;
        }

        if ((nBack + nOppBack > 22)) {
            // contact position
            return false;
        }

        for (i = 0; i <= nOppBack; ++i) {
            nOpp += anBoard[0][i];
        }

        for (i = 0; i <= nBack; ++i) {
            n += anBoard[1][i];
        }

        return n <= pbc.getnChequers() && nOpp <= pbc.getnChequers()
                && nBack < pbc.getnPoints() && nOppBack < pbc.getnPoints();
    }

    public static Reward bearoffEval(BearoffContext pbc, Board anBoard) {
        if(pbc.isTwoSided()) {
            return bearoffEvalTwoSided(pbc, anBoard);
        } else {
            return bearoffEvalOneSided(pbc, anBoard);
        }
    }

    private static Reward bearoffEvalTwoSided (BearoffContext pbc, Board anBoard) {
        int nUs = PositionId.positionBearoff(anBoard.anBoard()[1], pbc.getnPoints(), pbc.getnChequers());
        int nThem = PositionId.positionBearoff(anBoard.anBoard()[0], pbc.getnPoints(), pbc.getnChequers());
        int n = PositionId.combination(pbc.getnPoints() + pbc.getnChequers(), pbc.getnPoints());
        int iPos = nUs * n + nThem;
        float[] ar = new float[4];

        ReadTwoSidedBearoff(pbc, iPos, ar);
        double reward[] = Reward.rewardArray();
        reward[OUTPUT_WIN() ] = ar[ 0 ] / 2.0 + 0.5;
        return new Reward(reward);
    }

    /**
     * BEAROFF_GNUBG: read two sided bearoff database
     * */
    private static void ReadTwoSidedBearoff(BearoffContext pbc, int iPos, float[] ar) {
        int i, k = (pbc.isfCubeful()) ? 4 : 1;
        byte[] ac = new byte[8];
        int us;

        pbc.readBearoffData(40 + 2 * iPos * k, ac, k * 2);
        // add to cache

        for (i = 0; i < k; ++i) {
            us = ac[2 * i] | (ac[2 * i + 1]) << 8;
            ar[i] = us / 32767.5f - 1.0f;
        }
    }

    private static Reward bearoffEvalOneSided(BearoffContext pbc, Board anBoard) {
        int i, j;
        float[][] aarProb = new float[2][32];
        float[][] aarGammonProb = new float[2][32];
        float r;
        int[] anOn = new int[2];
        int[] an = new int[2];
        float[][] ar = new float[2][4];

        // get bearoff probabilities

        for (i = 0; i < 2; ++i) {
            an[i] = PositionId.positionBearoff(anBoard.anBoard()[i], pbc.getnPoints(), pbc.getnChequers());
            bearoffDist(pbc, an[i], aarProb[i], aarGammonProb[i], ar[i], null);
        }

        // calculate winning chance
        r = 0;
        for (i = 0; i < 32; ++i) {
            for (j = i; j < 32; ++j) {
                r += aarProb[1][i] * aarProb[0][j];
            }
        }

        double arOutput[] = Reward.rewardArray();
        arOutput[OUTPUT_WIN()] = r;

        // calculate gammon chances
        for (i = 0; i < 2; ++i) {
            for (j = 0, anOn[i] = 0; j < 25; ++j) {
                anOn[i] += anBoard.anBoard()[i][j];
            }
        }

        if (anOn[0] == 15 || anOn[1] == 15) {
            if (pbc.isfGammon()) {
                // my gammon chance: I'm out in i rolls and my opponent isn't inside
                //home quadrant in less than i rolls
                r = 0;
                for (i = 0; i < 32; i++) {
                    for (j = i; j < 32; j++) {
                        r += aarProb[1][i] * aarGammonProb[0][j];
                    }
                }

                arOutput[OUTPUT_WINGAMMON()] = r;

                // opp gammon chance
                r = 0;
                for (i = 0; i < 32; i++) {
                    for (j = i + 1; j < 32; j++) {
                        r += aarProb[0][i] * aarGammonProb[1][j];
                    }
                }

                arOutput[OUTPUT_LOSEGAMMON()] = r;
            } else {
                throw new IllegalArgumentException("Invalid bearoff database");
            }
        } else {
            // no gammons possible
            arOutput[OUTPUT_WINGAMMON()] = 0;
            arOutput[OUTPUT_LOSEGAMMON()] = 0;
        }

        // no backgammons possible
        arOutput[OUTPUT_LOSEBACKGAMMON()] = 0;
        arOutput[OUTPUT_WINBACKGAMMON()] = 0;
        return new Reward(arOutput);
    }

    public static void bearoffDist(BearoffContext pbc, int nPosID,
                             float[] arProb, float[] arGammonProb,
                             float[] ar,
                             short[] ausProb) {
        if (pbc.isTwoSided()) {
            throw new IllegalArgumentException("Invalid bearoff database");
        }

        if (pbc.isfND()) {
            readBearoffOneSidedND(pbc, nPosID, arProb, arGammonProb, ar, ausProb);
        } else {
            readBearoffOneSidedExact(pbc, nPosID, arProb, arGammonProb, ar, ausProb);
        }
    }

    private  static float fnd (float x, float mu, float sigma  ) {
        float epsilon = 1.0e-7f;
        if ( sigma <= epsilon ) {
            // dirac delta function
            return (Math.abs(mu - x) < epsilon) ? 1.0f : 0.0f;
        } else {
            float xm = ( x - mu ) / sigma;
            return 1.0f / (( sigma * (float)Math.sqrt( 2.0 * Math.PI ) ) * ((float)(Math.exp( - xm * xm / 2.0 ))));
        }
    }

    private static void readBearoffOneSidedND(BearoffContext pbc,
                                              int nPosID,
                                              float[] arProb, float[] arGammonProb,
                                              float[] ar,
                                              short[] ausProb) {

        byte[] ac = new byte[16];
        int i;
        float r;

        pbc.readBearoffData(40 + nPosID * 16, ac, 16);
        ByteBuffer bb = ByteBuffer.wrap(ac);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        float[] arx = bb.asFloatBuffer().array();

        if ( arProb != null || ausProb != null) {
            for (i = 0; i < 32; ++i) {
                r = fnd(1.0f * i, arx[0], arx[1]);
                if(arProb != null) {
                    arProb[i] = r;
                }
                if(ausProb != null) {
                    ausProb[i] = (short)((int)(r * 65535.0f) & 0xffff);
                }
            }
        }

        if ( arGammonProb != null) {
            for (i = 0; i < 32; ++i) {
                r = fnd(1.0f * i, arx[2], arx[3]);
                arGammonProb[i] = r;
            }
        }

        if ( ar != null) {
            System.arraycopy(arx, 0, ar, 0, 4);
        }
    }

    private static void readBearoffOneSidedExact(BearoffContext pbc,
                                                 int nPosID,
                                                 float[] arProb, float[] arGammonProb,
                                                 float[] ar,
                                                 short[] ausProb) {
        short[] aus = new short[64];
    	// get distribution
        if (pbc.isfCompressed()) {
            getDistCompressed(aus, pbc, nPosID);
        } else {
            getDistUncompressed(aus, pbc, nPosID);
        }

        assignOneSided(arProb, arGammonProb, ar, ausProb, aus, 32);
    }

    private static void assignOneSided(float[] arProb, float[] arGammonProb,
                                       float[] ar,
                                       short[] ausProb,
                                       short[] ausProbx,
                                       int ausGammonProbxOffset) {

        float[] arx = new float[64];

        if ( ausProb != null)
            System.arraycopy(ausProbx, 0, ausProb, 0, 32);

        if (ar != null || arProb != null || arGammonProb != null) {
            for (int i = 0; i < 32; ++i)
                arx[i] = ausProbx[i] / 65535.0f;

            for (int i = 0; i < 32; ++i)
                arx[32 + i] = ausProbx[ausGammonProbxOffset + i] / 65535.0f;

            if (arProb != null) {
                System.arraycopy(arx, 0, arProb, 0, 32);
            }
            if (arGammonProb != null) {
                System.arraycopy(arx, 32, arGammonProb, 0, 32);
            }
            if (ar != null) {
                averageRolls(arx, 0, ar, 0);
                averageRolls(arx, 32, ar, 2);
            }
        }
    }

    private static void averageRolls(float[] arProb, int probOffset, float[] ar, int arrOffset) {
        float sx = 0, sx2 = 0;
        for (int i = 1; i < 32; i++) {
            float p = i * arProb[probOffset + i];
            sx += p;
            sx2 += i * p;
        }

        ar[arrOffset] = sx;
        ar[arrOffset + 1] = (float) Math.sqrt(sx2 - sx * sx);
    }

    private static void getDistCompressed(short[] aus, BearoffContext pbc, int nPosID) {
        byte[] ac = new byte[128];
        int iOffset;
        int nBytes;
        int ioff, nz, ioffg = 0, nzg = 0;
        int nPos = PositionId.combination(pbc.getnPoints() + pbc.getnChequers(), pbc.getnPoints());
        int index_entry_size = pbc.isfGammon() ? 8 : 6;

        // find offsets and no. of non-zero elements

        pbc.readBearoffData(40 + nPosID * index_entry_size, ac, index_entry_size);

        // find offset (LE byte order)
        iOffset = Ints.fromBytes(ac[3], ac[2], ac[1], ac[0]);

        nz = ac[4];
        ioff = ac[5];
        if (pbc.isfGammon()) {
            nzg = ac[6];
            ioffg = ac[7];
        }
        // Sanity checks

        if ((iOffset > 64 * nPos && 64 * nPos > 0) ||
                nz > 32 || ioff > 32 ||
                nzg > 32 || ioffg > 32) {
            throw new IllegalArgumentException("The bearoff file '" + pbc.getSzFilename() + "' is likely to be corrupted.");
        }

        // read prob + gammon probs

        iOffset = 40     /* the header */
                + nPos * index_entry_size     /* the offset data */
                + 2 * iOffset; /* offset to current position */

        // read values

        nBytes = 2 * (nz + nzg);

        // get distribution
        pbc.readBearoffData(iOffset, ac, nBytes);
        copyBytes(aus, ac, nz, ioff, nzg, ioffg);
    }

    private static void getDistUncompressed (short[] aus, BearoffContext pbc, int nPosID) {
        byte[] ac = new byte[128];
        int iOffset;

        // read from file
        iOffset = 40 + 64 * nPosID * (pbc.isfGammon() ? 2 : 1);

        pbc.readBearoffData(iOffset, ac, pbc.isfGammon() ? 128 : 64);
        copyBytes ( aus, ac, 32, 0, 32, 0 );
    }

    private static void copyBytes(short[] aus, byte[] ac, int nz, int ioff, int nzg, int ioffg) {
        int i = 0;
        Arrays.fill(aus, 0, 64, (short) 0);
        for (int j = 0; j < nz; ++j, i += 2) {
            aus[ioff + j] = (short) (ac[i] | ac[i + 1] << 8);
        }

        for (int j = 0; j < nzg; ++j, i += 2) {
            aus[32 + ioffg + j] = (short) (ac[i] | ac[i + 1] << 8);
        }
    }
}
