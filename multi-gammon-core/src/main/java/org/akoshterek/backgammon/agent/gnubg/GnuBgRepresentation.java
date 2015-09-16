package org.akoshterek.backgammon.agent.gnubg;

import org.akoshterek.backgammon.agent.fa.InputRepresentation;
import org.akoshterek.backgammon.board.Board;

import java.util.Arrays;

import static org.akoshterek.backgammon.agent.fa.InputConstants.*;
import static org.akoshterek.backgammon.agent.fa.InputConstants.ContactInputs.*;
import static org.akoshterek.backgammon.agent.fa.InputConstants.RaceInputs.*;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public class GnuBgRepresentation implements InputRepresentation {
    private static int[] anEscapes = new int[0x1000];
    private static int[] anEscapes1 = new int[0x1000];

	/* aanCombination[n] -
     How many ways to hit from a distance of n pips.
     Each number is an index into aIntermediate below.
	*/
    private static int[][] aanCombination = new int[][]
    {
        {  0, -1, -1, -1, -1 }, /*  1 */
        {  1,  2, -1, -1, -1 }, /*  2 */
        {  3,  4,  5, -1, -1 }, /*  3 */
        {  6,  7,  8,  9, -1 }, /*  4 */
        { 10, 11, 12, -1, -1 }, /*  5 */
        { 13, 14, 15, 16, 17 }, /*  6 */
        { 18, 19, 20, -1, -1 }, /*  7 */
        { 21, 22, 23, 24, -1 }, /*  8 */
        { 25, 26, 27, -1, -1 }, /*  9 */
        { 28, 29, -1, -1, -1 }, /* 10 */
        { 30, -1, -1, -1, -1 }, /* 11 */
        { 31, 32, 33, -1, -1 }, /* 12 */
        { -1, -1, -1, -1, -1 }, /* 13 */
        { -1, -1, -1, -1, -1 }, /* 14 */
        { 34, -1, -1, -1, -1 }, /* 15 */
        { 35, -1, -1, -1, -1 }, /* 16 */
        { -1, -1, -1, -1, -1 }, /* 17 */
        { 36, -1, -1, -1, -1 }, /* 18 */
        { -1, -1, -1, -1, -1 }, /* 19 */
        { 37, -1, -1, -1, -1 }, /* 20 */
        { -1, -1, -1, -1, -1 }, /* 21 */
        { -1, -1, -1, -1, -1 }, /* 22 */
        { -1, -1, -1, -1, -1 }, /* 23 */
        { 38, -1, -1, -1, -1 }  /* 24 */
    };

    /* One way to hit */
    private static class Inter
    {
		/* if true, all intermediate points (if any) are required;
		   if false, one of two intermediate points are required.
		   Set to true for a direct hit, but that can be checked with
		   nFaces == 1,
		*/
        public final int fAll;

		/* Intermediate points required */
        public final int[] anIntermediate;

		/* Number of faces used in hit (1 to 4) */
        public final int nFaces;

		/* Number of pips used to hit */
        public final int nPips;

        public Inter(int fAll, int[] anIntermediate, int nFaces, int nPips) {
            this.fAll = fAll;
            this.anIntermediate = anIntermediate;
            this.nFaces = nFaces;
            this.nPips = nPips;
        }
    }

      /* All ways to hit */
    private static Inter[] aIntermediate = new Inter[]
    {
        new Inter(1, new int[]{ 0, 0, 0 }, 1, 1 ), /*  0: 1x hits 1 */
        new Inter( 1, new int[]{ 0, 0, 0 }, 1, 2 ), /*  1: 2x hits 2 */
        new Inter( 1, new int[]{ 1, 0, 0 }, 2, 2 ), /*  2: 11 hits 2 */
        new Inter( 1, new int[]{ 0, 0, 0 }, 1, 3 ), /*  3: 3x hits 3 */
        new Inter( 0, new int[]{ 1, 2, 0 }, 2, 3 ), /*  4: 21 hits 3 */
        new Inter( 1, new int[]{ 1, 2, 0 }, 3, 3 ), /*  5: 11 hits 3 */
        new Inter( 1, new int[]{ 0, 0, 0 }, 1, 4 ), /*  6: 4x hits 4 */
        new Inter( 0, new int[]{ 1, 3, 0 }, 2, 4 ), /*  7: 31 hits 4 */
        new Inter( 1, new int[]{ 2, 0, 0 }, 2, 4 ), /*  8: 22 hits 4 */
        new Inter( 1, new int[]{ 1, 2, 3 }, 4, 4 ), /*  9: 11 hits 4 */
        new Inter( 1, new int[]{ 0, 0, 0 }, 1, 5 ), /* 10: 5x hits 5 */
        new Inter( 0, new int[]{ 1, 4, 0 }, 2, 5 ), /* 11: 41 hits 5 */
        new Inter( 0, new int[]{ 2, 3, 0 }, 2, 5 ), /* 12: 32 hits 5 */
        new Inter( 1, new int[]{ 0, 0, 0 }, 1, 6 ), /* 13: 6x hits 6 */
        new Inter( 0, new int[]{ 1, 5, 0 }, 2, 6 ), /* 14: 51 hits 6 */
        new Inter( 0, new int[]{ 2, 4, 0 }, 2, 6 ), /* 15: 42 hits 6 */
        new Inter( 1, new int[]{ 3, 0, 0 }, 2, 6 ), /* 16: 33 hits 6 */
        new Inter( 1, new int[]{ 2, 4, 0 }, 3, 6 ), /* 17: 22 hits 6 */
        new Inter( 0, new int[]{ 1, 6, 0 }, 2, 7 ), /* 18: 61 hits 7 */
        new Inter( 0, new int[]{ 2, 5, 0 }, 2, 7 ), /* 19: 52 hits 7 */
        new Inter( 0, new int[]{ 3, 4, 0 }, 2, 7 ), /* 20: 43 hits 7 */
        new Inter( 0, new int[]{ 2, 6, 0 }, 2, 8 ), /* 21: 62 hits 8 */
        new Inter( 0, new int[]{ 3, 5, 0 }, 2, 8 ), /* 22: 53 hits 8 */
        new Inter( 1, new int[]{ 4, 0, 0 }, 2, 8 ), /* 23: 44 hits 8 */
        new Inter( 1, new int[]{ 2, 4, 6 }, 4, 8 ), /* 24: 22 hits 8 */
        new Inter( 0, new int[]{ 3, 6, 0 }, 2, 9 ), /* 25: 63 hits 9 */
        new Inter( 0, new int[]{ 4, 5, 0 }, 2, 9 ), /* 26: 54 hits 9 */
        new Inter( 1, new int[]{ 3, 6, 0 }, 3, 9 ), /* 27: 33 hits 9 */
        new Inter( 0, new int[]{ 4, 6, 0 }, 2, 10 ), /* 28: 64 hits 10 */
        new Inter( 1, new int[]{ 5, 0, 0 }, 2, 10 ), /* 29: 55 hits 10 */
        new Inter( 0, new int[]{ 5, 6, 0 }, 2, 11 ), /* 30: 65 hits 11 */
        new Inter( 1, new int[]{ 6, 0, 0 }, 2, 12 ), /* 31: 66 hits 12 */
        new Inter( 1, new int[]{ 4, 8, 0 }, 3, 12 ), /* 32: 44 hits 12 */
        new Inter( 1, new int[]{ 3, 6, 9 }, 4, 12 ), /* 33: 33 hits 12 */
        new Inter( 1, new int[]{ 5, 10, 0 }, 3, 15 ), /* 34: 55 hits 15 */
        new Inter( 1, new int[]{ 4, 8, 12 }, 4, 16 ), /* 35: 44 hits 16 */
        new Inter( 1, new int[]{ 6, 12, 0 }, 3, 18 ), /* 36: 66 hits 18 */
        new Inter( 1, new int[]{ 5, 10, 15 }, 4, 20 ), /* 37: 55 hits 20 */
        new Inter( 1, new int[]{ 6, 12, 18 }, 4, 24 )  /* 38: 66 hits 24 */
    };

	/** aaRoll[n] - All ways to hit with the n'th roll
      *Each entry is an index into aIntermediate above.
	  */
    static int[][] aaRoll = new int [][] {
        {  0,  2,  5,  9 }, /* 11 */
        {  0,  1,  4, -1 }, /* 21 */
        {  1,  8, 17, 24 }, /* 22 */
        {  0,  3,  7, -1 }, /* 31 */
        {  1,  3, 12, -1 }, /* 32 */
        {  3, 16, 27, 33 }, /* 33 */
        {  0,  6, 11, -1 }, /* 41 */
        {  1,  6, 15, -1 }, /* 42 */
        {  3,  6, 20, -1 }, /* 43 */
        {  6, 23, 32, 35 }, /* 44 */
        {  0, 10, 14, -1 }, /* 51 */
        {  1, 10, 19, -1 }, /* 52 */
        {  3, 10, 22, -1 }, /* 53 */
        {  6, 10, 26, -1 }, /* 54 */
        { 10, 29, 34, 37 }, /* 55 */
        {  0, 13, 18, -1 }, /* 61 */
        {  1, 13, 21, -1 }, /* 62 */
        {  3, 13, 25, -1 }, /* 63 */
        {  6, 13, 28, -1 }, /* 64 */
        { 10, 13, 30, -1 }, /* 65 */
        { 13, 31, 36, 38 }  /* 66 */
    };

	/* One roll stat */
    static class OneRollStat
    {
		/* count of pips this roll hits */
        public int nPips = 0;

		/* number of chequers this roll hits */
        public int nChequers = 0;
    }

    static {
        computeTable0();
        computeTable1();
    }

    @Override
    public double[] calculateRaceInputs(Board anBoard) {
        double[] inputs = new double[NUM_RACE_INPUTS];
        for (int side = 0; side < 2; ++side) {
            byte[] board = anBoard.anBoard[side];
            int afInputIndex = side * HALF_RACE_INPUTS;

            int menOff = 15;
            assert (board[23] == 0 && board[24] == 0);

            // Points
            for (int i = 0; i < 23; ++i) {
                int nc = board[i];
                int k = i * 4;
                menOff -= nc;
                inputs[afInputIndex + k++] = (nc == 1) ? 1.0f : 0.0f;
                inputs[afInputIndex + k++] = (nc == 2) ? 1.0f : 0.0f;
                inputs[afInputIndex + k++] = (nc >= 3) ? 1.0f : 0.0f;
                inputs[afInputIndex + k] = nc > 3 ? (nc - 3) / 2.0f : 0.0f;
            }

            // Men off
            for (int k = 0; k < 14; ++k) {
                inputs[afInputIndex + k + RI_OFF] = (menOff == (k + 1)) ? 1.0f : 0.0f;
            }

            int nCross = 0;

            for (int k = 1; k < 4; ++k) {
                for (int i = 6 * k; i < 6 * k + 6; ++i) {
                    int nc = board[i];
                    if (nc != 0)
                        nCross += nc * k;
                }
            }

            inputs[afInputIndex + RI_NCROSS] = nCross / 10.0f;
        }

        return inputs;
    }

    @Override
    public double[] calculateCrashedInputs(Board anBoard) {
        double[] inputs = new double[NUM_INPUTS];
        baseInputs(anBoard, inputs);

        int index = 4 * 25 * 2;
        menOffAll(anBoard.anBoard[1], inputs, index + I_OFF1);
        calculateHalfInputs(anBoard.anBoard[1], anBoard.anBoard[0], inputs, index);

        index = 4 * 25 * 2 + MORE_INPUTS;
        menOffAll(anBoard.anBoard[0], inputs, index + I_OFF1);
        calculateHalfInputs(anBoard.anBoard[0], anBoard.anBoard[1], inputs, index);

        return inputs;
    }

    @Override
    public double[] calculateContactInputs(Board anBoard) {
        double[] inputs = new double[NUM_INPUTS];
        baseInputs(anBoard, inputs);

        int index = 4 * 25 * 2;
        // I accidentally switched sides (0 and 1) when I trained the net
        menOffNonCrashed(anBoard.anBoard[0], inputs, index + I_OFF1);
        calculateHalfInputs(anBoard.anBoard[1], anBoard.anBoard[0], inputs, index);

        index = (4 * 25 * 2 + MORE_INPUTS);
        menOffNonCrashed(anBoard.anBoard[1], inputs, index + I_OFF1);
        calculateHalfInputs(anBoard.anBoard[0], anBoard.anBoard[1], inputs, index);

        return inputs;
    }

    private static void calculateHalfInputs(byte[] anBoard, byte[] anBoardOpp, double[] input, int index) {
        int i, j, k, l, nOppBack, nBoard;
        int[] aHit = new int[39];

        Inter pi;

        for (nOppBack = 24; nOppBack >= 0; --nOppBack) {
            if (anBoardOpp[nOppBack] != 0)
                break;
        }

        nOppBack = 23 - nOppBack;

        {
            int n = 0;
            for (i = nOppBack + 1; i < 25; i++) {
                if (anBoard[i] != 0) {
                    n += (i + 1 - nOppBack) * anBoard[i];
                }
            }
            assert (n != 0);
            input[index + I_BREAK_CONTACT] = n / (15 + 152.0f);
        }

        {
            int p = 0;

            for (i = 0; i < nOppBack; i++) {
                if (anBoard[i] != 0)
                    p += (i + 1) * anBoard[i];
            }

            input[index + I_FREEPIP] = p / 100.0f;
        }

        {
            int t = 0;
            int no = 0;

            t += 24 * anBoard[24];
            no += anBoard[24];

            for (i = 23; i >= 12 && i > nOppBack; --i) {
                if (anBoard[i] != 0 && anBoard[i] != 2) {
                    int n = ((anBoard[i] > 2) ? (anBoard[i] - 2) : 1);
                    no += n;
                    t += i * n;
                }
            }

            for (; i >= 6; --i) {
                if (anBoard[i] != 0) {
                    int n = anBoard[i];
                    no += n;
                    t += i * n;
                }
            }

            for (i = 5; i >= 0; --i) {
                if (anBoard[i] > 2) {
                    t += i * (anBoard[i] - 2);
                    no += (anBoard[i] - 2);
                } else if (anBoard[i] < 2) {
                    int n = (2 - anBoard[i]);

                    if (no >= n) {
                        t -= i * n;
                        no -= n;
                    }
                }
            }

            if (t < 0)
                t = 0;

            input[index + I_TIMING] = t / 100.0f;
        }

	/* Back chequer */
        {
            int nBack;

            for (nBack = 24; nBack >= 0; --nBack) {
                if (anBoard[nBack] != 0)
                    break;
            }

            input[index + I_BACK_CHEQUER] = nBack / 24.0f;

		/* Back anchor */
            for (i = nBack == 24 ? 23 : nBack; i >= 0; --i) {
                if (anBoard[i] >= 2)
                    break;
            }

            input[index + I_BACK_ANCHOR] = i / 24.0f;

		/* Forward anchor */
            int n = 0;
            for (j = 18; j <= i; ++j) {
                if (anBoard[j] >= 2) {
                    n = 24 - j;
                    break;
                }
            }

            if (n == 0) {
                for (j = 17; j >= 12; --j) {
                    if (anBoard[j] >= 2) {
                        n = 24 - j;
                        break;
                    }
                }
            }

            input[index + I_FORWARD_ANCHOR] = n == 0 ? 2.0f : n / 6.0f;
        }

	/* Piploss */
        nBoard = 0;
        for (i = 0; i < 6; i++)
            if (anBoard[i] != 0)
                nBoard++;

        Arrays.fill(aHit, 0);

    /* for every point we'd consider hitting a blot on, */
        for (i = (nBoard > 2) ? 23 : 21; i >= 0; i--) {
        /* if there's a blot there, then */

            if (anBoardOpp[i] == 1) {
			/* for every point beyond */

                for (j = 24 - i; j < 25; j++) {
				/* if we have a hitter and are willing to hit */

                    if (anBoard[j] != 0 && !(j < 6 && anBoard[j] == 2)) {
					/* for every roll that can hit from that point */

                        for (int n = 0; n < 5; n++) {
                            if (aanCombination[j - 24 + i][n] == -1)
                                break;

						/* find the intermediate points required to play */

                            pi = aIntermediate[aanCombination[j - 24 + i][n]];

                            boolean cannot_hit = false;
                            if (pi.fAll != 0) {
							/* if nFaces is 1, there are no intermediate points */

                                if (pi.nFaces > 1) {
								/* all the intermediate points are required */

                                    for (k = 0; k < 3 && pi.anIntermediate[k] > 0; k++)
                                        if (anBoardOpp[i - pi.anIntermediate[k]] > 1) {
										/* point is blocked; look for other hits */
                                            cannot_hit = true;
                                        }
                                }
                            } else {
							/* either of two points are required */

                                if (anBoardOpp[i - pi.anIntermediate[0]] > 1
                                        && anBoardOpp[i - pi.anIntermediate[1]] > 1) {
								/* both are blocked; look for other hits */
                                    cannot_hit = true;
                                }
                            }

						/* enter this shot as available */
                            if (!cannot_hit) {
                                aHit[aanCombination[j - 24 + i][n]] |= 1 << j;
                            }
                        }
                    }
                }
            }
        }

        OneRollStat[] aRoll = new OneRollStat[21];
        for (i = 0; i < aRoll.length; i++) {
            aRoll[i] = new OneRollStat();
        }

        if (anBoard[24] == 0) {
		/* we're not on the bar; for each roll, */

            for (i = 0; i < 21; i++) {
                int n = -1; /* (hitter used) */

			/* for each way that roll hits, */
                for (j = 0; j < 4; j++) {
                    int r = aaRoll[i][j];
                    if (r < 0)
                        break;

                    if (aHit[r] == 0)
                        continue;

                    pi = aIntermediate[r];

                    if (pi.nFaces == 1) {
					/* direct shot */
                        for (k = 23; k > 0; k--) {
                            if ((aHit[r] & (1 << k)) != 0) {
							/* select the most advanced blot; if we still have
							a chequer that can hit there */

                                if (n != k || anBoard[k] > 1)
                                    aRoll[i].nChequers++;

                                n = k;

                                if (k - pi.nPips + 1 > aRoll[i].nPips)
                                    aRoll[i].nPips = k - pi.nPips + 1;

							/* if rolling doubles, check for multiple
							direct shots */

                                if (aaRoll[i][3] >= 0 && (aHit[r] & ~(1 << k)) != 0)
                                    aRoll[i].nChequers++;

                                break;
                            }
                        }
                    } else {
					/* indirect shot */
                        if (aRoll[i].nChequers == 0)
                            aRoll[i].nChequers = 1;

					/* find the most advanced hitter */
                        for (k = 23; k >= 0; k--)
                            if ((aHit[r] & (1 << k)) != 0)
                                break;

                        if (k - pi.nPips + 1 > aRoll[i].nPips)
                            aRoll[i].nPips = k - pi.nPips + 1;

					/* check for blots hit on intermediate points */

                        for (l = 0; l < 3 && pi.anIntermediate[l] > 0; l++) {
                            if (anBoardOpp[23 - k + pi.anIntermediate[l]] == 1) {
                                aRoll[i].nChequers++;
                                break;
                            }
                        }
                    }
                }
            }
        } else if (anBoard[24] == 1) {
		/* we have one on the bar; for each roll, */

            for (i = 0; i < 21; i++) {
                int n = 0; /* (free to use either die to enter) */

                for (j = 0; j < 4; j++) {
                    int r = aaRoll[i][j];
                    if (r < 0)
                        break;

                    if (aHit[r] == 0)
                        continue;

                    pi = aIntermediate[r];

                    if (pi.nFaces == 1) {
					/* direct shot */

                        for (k = 24; k > 0; k--) {
                            if ((aHit[r] & (1 << k)) != 0) {
							/* if we need this die to enter, we can't hit elsewhere */

                                if (n != 0 && k != 24)
                                    break;

							/* if this isn't a shot from the bar, the
							other die must be used to enter */

                                if (k != 24) {
                                    int npip = aIntermediate[aaRoll[i][1 - j]].nPips;

                                    if (anBoardOpp[npip - 1] > 1)
                                        break;

                                    n = 1;
                                }

                                aRoll[i].nChequers++;

                                if (k - pi.nPips + 1 > aRoll[i].nPips)
                                    aRoll[i].nPips = k - pi.nPips + 1;
                            }
                        }
                    } else {
					/* indirect shot -- consider from the bar only */
                        if ((aHit[r] & (1 << 24)) == 0)
                            continue;

                        if (aRoll[i].nChequers == 0)
                            aRoll[i].nChequers = 1;

                        if (25 - pi.nPips > aRoll[i].nPips)
                            aRoll[i].nPips = 25 - pi.nPips;

					/* check for blots hit on intermediate points */
                        for (k = 0; k < 3 && pi.anIntermediate[k] > 0; k++) {
                            if (anBoardOpp[pi.anIntermediate[k] + 1] == 1) {
                                aRoll[i].nChequers++;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
		/* we have more than one on the bar --
		   count only direct shots from point 24 */

            for (i = 0; i < 21; i++) {
		  /* for the first two ways that hit from the bar */

                for (j = 0; j < 2; j++) {
                    int r = aaRoll[i][j];
                    if ((aHit[r] & (1 << 24)) == 0)
                        continue;

                    pi = aIntermediate[r];

				/* only consider direct shots */
                    if (pi.nFaces != 1)
                        continue;

                    aRoll[i].nChequers++;

                    if (25 - pi.nPips > aRoll[i].nPips)
                        aRoll[i].nPips = 25 - pi.nPips;
                }
            }
        }

        {
            int np = 0;
            int n1 = 0;
            int n2 = 0;

            for (i = 0; i < 21; i++) {
                int w = aaRoll[i][3] > 0 ? 1 : 2;
                int nc = aRoll[i].nChequers;
                np += aRoll[i].nPips * w;

                if (nc > 0) {
                    n1 += w;
                    if (nc > 1)
                        n2 += w;
                }
            }

            input[index + I_PIPLOSS] = np / (12.0f * 36.0f);
            input[index + I_P1] = n1 / 36.0f;
            input[index + I_P2] = n2 / 36.0f;
        }

        input[index + I_BACKESCAPES] = escapes(anBoard, 23 - nOppBack) / 36.0f;
        input[index + I_BACKRESCAPES] = escapes1(anBoard, 23 - nOppBack) / 36.0f;

        {
            i = 15;
            int n;
            for (n = 36; i < 24 - nOppBack; i++)
                if ((j = escapes(anBoard, i)) < n)
                    n = j;

            input[index + I_ACONTAIN] = (36 - n) / 36.0f;
            input[index + I_ACONTAIN2] = input[index + I_ACONTAIN] * input[index + I_ACONTAIN];

            if (nOppBack < 0) {
		/* restart loop, point 24 should not be included */
                i = 15;
                n = 36;
            }

            for (; i < 24; i++)
                if ((j = escapes(anBoard, i)) < n)
                    n = j;


            input[index + I_CONTAIN] = (36 - n) / 36.0f;
            input[index + I_CONTAIN2] = input[index + I_CONTAIN] * input[index + I_CONTAIN];

            for (n = 0, i = 6; i < 25; i++)
                if (anBoard[i] != 0)
                    n += (i - 5) * anBoard[i] * escapes(anBoardOpp, i);

            input[index + I_MOBILITY] = n / 3600.0f;

            j = 0;
            n = 0;
            for (i = 0; i < 25; i++) {
                int ni = anBoard[i];

                if (ni != 0) {
                    j += ni;
                    n += i * ni;
                }
            }

            if (j != 0)
                n = (n + j - 1) / j;

            j = 0;
            for (k = 0, i = n + 1; i < 25; i++) {
                int ni = anBoard[i];

                if (ni != 0) {
                    j += ni;
                    k += ni * (i - n) * (i - n);
                }
            }

            if (j != 0)
                k = (k + j - 1) / j;

            input[index + I_MOMENT2] = k / 400.0f;
        }

        if (anBoard[24] > 0) {
            int loss = 0;
            boolean two = anBoard[24] > 1;

            for (i = 0; i < 6; ++i) {
                if (anBoardOpp[i] > 1) {
				/* any double loses */
                    loss += 4 * (i + 1);

                    for (j = i + 1; j < 6; ++j) {
                        if (anBoardOpp[j] > 1) {
                            loss += 2 * (i + j + 2);
                        } else {
                            if (two) {
                                loss += 2 * (i + 1);
                            }
                        }
                    }
                } else {
                    if (two) {
                        for (j = i + 1; j < 6; ++j) {
                            if (anBoardOpp[j] > 1)
                                loss += 2 * (j + 1);
                        }
                    }
                }
            }

            input[index + I_ENTER] = loss / (36.0f * (49.0f / 6.0f));
        } else {
            input[index + I_ENTER] = 0.0f;
        }

        {
            int n = 0;
            for (i = 0; i < 6; i++)
                n += (anBoardOpp[i] > 1) ? 1 : 0;

            input[index + I_ENTER2] = (36 - (n - 6) * (n - 6)) / 36.0f;
        }

        {
            int pa = -1;
            int w = 0;
            int tot = 0;
            int np;

            for (np = 23; np > 0; --np) {
                if (anBoard[np] >= 2) {
                    if (pa == -1) {
                        pa = np;
                        continue;
                    }

                    {
                        int d = pa - np;
                        int c = 0;

                        if (d <= 6)
                            c = 11;
                        else if (d <= 11)
                            c = 13 - d;

                        w += c * anBoard[pa];
                        tot += anBoard[pa];
                    }
                }
            }

            input[index + I_BACKBONE] = tot != 0 ? 1 - (w / (tot * 11.0f)) : 0;
        }

        {
            int nAc = 0;

            for (i = 18; i < 24; ++i) {
                if (anBoard[i] > 1)
                    ++nAc;
            }

            input[index + I_BACKG] = 0.0;
            input[index + I_BACKG1] = 0.0;

            if (nAc >= 1) {
                int tot = 0;
                for (i = 18; i < 25; ++i) {
                    tot += anBoard[i];
                }

                if (nAc > 1) {
				/* g_assert( tot >= 4 ); */
                    input[index + I_BACKG] = (tot - 3) / 4.0f;
                } else if (nAc == 1) {
                    input[index + I_BACKG1] = tot / 8.0f;
                }
            }
        }
    }

    private static void menOffAll(byte[] anBoard, double[] afInput, int index) {
        //* Men off
        int menOff = 15;

        for (int i = 0; i < 25; i++)
            menOff -= anBoard[i];

        if (menOff > 10) {
            afInput[index + I_OFF1] = 1.0;
            afInput[index + I_OFF2 - I_OFF1] = 1.0;
            afInput[index + I_OFF3 - I_OFF1] = (menOff - 10) / 5.0f;
        } else if (menOff > 5) {
            afInput[index+ I_OFF1] = 1.0f;
            afInput[index + I_OFF2 - I_OFF1] = (menOff - 5) / 5.0f;
            afInput[index + I_OFF3 - I_OFF1] = 0.0f;
        } else {
            afInput[index+ I_OFF1] = menOff != 0 ? menOff / 5.0f : 0.0f;
            afInput[index + I_OFF2 - I_OFF1] = 0.0f;
            afInput[index + I_OFF3 - I_OFF1] = 0.0f;
        }
    }

    private static void menOffNonCrashed(byte[] anBoard, double[] afInput, int index) {
        int menOff = 15;
        int i;

        for (i = 0; i < 25; ++i)
            menOff -= anBoard[i];

        assert (menOff <= 8);

        if (menOff > 5) {
            afInput[index] = 1.0f;
            afInput[index + 1] = 1.0f;
            afInput[index + 2] = (menOff - 6) / 3.0f;
        } else if (menOff > 2) {
            afInput[index] = 1.0f;
            afInput[index + 1] = (menOff - 3) / 3.0f;
            afInput[index + 2] = 0.0f;
        } else {
            afInput[index] = menOff != 0 ? menOff / 3.0f : 0.0f;
            afInput[index + 1] = 0.0f;
            afInput[index + 2] = 0.0f;
        }
    }

    private static void baseInputs(Board anBoard, double[] arInput) {
        for (int j = 0; j < 2; ++j) {
            int afInput = j * 25 * 4;
            byte[] board = anBoard.anBoard[j];

            //* Points
            for (int i = 0; i < 24; i++) {
                int nc = board[i];

                arInput[afInput + i * 4] = (nc == 1) ? 1.0f : 0.0f;
                arInput[afInput + i * 4 + 1] = (nc == 2) ? 1.0f : 0.0f;
                arInput[afInput + i * 4 + 2] = (nc >= 3) ? 1.0f : 0.0f;
                arInput[afInput + i * 4 + 3] = nc > 3 ? (nc - 3) / 2.0f : 0.0f;
            }

            //* Bar
            int nc = board[24];
            arInput[afInput + 24 * 4] = (nc >= 1) ? 1.0f : 0.0f;
            arInput[afInput + 24 * 4 + 1] = (nc >= 2) ? 1.0f : 0.0f;
            arInput[afInput + 24 * 4 + 2] = (nc >= 3) ? 1.0f : 0.0f;
            arInput[afInput + 24 * 4 + 3] = nc > 3 ? (nc - 3) / 2.0f : 0.0f;
        }
    }

    private static void computeTable0() {
        int i, c, n0, n1;
        for (i = 0; i < 0x1000; i++) {
            c = 0;
            for (n0 = 0; n0 <= 5; n0++) {
                for (n1 = 0; n1 <= n0; n1++) {
                    if ((i & (1 << (n0 + n1 + 1))) == 0 &&
                            ((i & (1 << n0)) == 0 && (i & (1 << n1)) != 0)) {
                        c += (n0 == n1) ? 1 : 2;
                    }
                }
            }

            anEscapes[i] = c;
        }
    }

    private static void computeTable1() {
        int i, c, n0, n1, low;
        for (i = 1; i < 0x1000; i++) {
            c = 0;
            low = 0;
            while ((i & (1 << low)) == 0) {
                ++low;
            }

            for (n0 = 0; n0 <= 5; n0++) {
                for (n1 = 0; n1 <= n0; n1++) {
                    if ((n0 + n1 + 1 > low) &&
                            (i & (1 << (n0 + n1 + 1))) == 0 &&
                            ((i & (1 << n0)) == 0 && (i & (1 << n1)) != 0)) {
                        c += (n0 == n1) ? 1 : 2;
                    }
                }
            }

            anEscapes1[i] = c;
        }
    }

    private static int escapes(byte[] anBoard, int n) {
        int i, af = 0, m;
        m = (n < 12) ? n : 12;

        for (i = 0; i < m; i++)
            if (anBoard[24 + i - n] > 1)
                af |= (1 << i);

        return anEscapes[af];
    }

    private static int escapes1(byte[] anBoard, int n) {
        int i, af = 0, m;
        m = (n < 12) ? n : 12;

        for( i = 0; i < m; i++ )
            if( anBoard[ 24 + i - n ] > 1 )
                af |= ( 1 << i );

        return anEscapes1[ af ];
    }
}
