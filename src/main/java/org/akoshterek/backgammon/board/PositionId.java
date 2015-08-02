package org.akoshterek.backgammon.board;

/**
 * @author Alex
 *         date 19.07.2015.
 */
public class PositionId {
    public static final int L_POSITIONID = 14;

    private static final int MAX_N = 40;
    private static final int MAX_R = 25;
    private static boolean calculated = false;
    private static int[][] anCombination = new int[MAX_N][MAX_R];

    public static short PositionIndex(int g, int anBoard[]) {
        int fBits;
        int j = g - 1;

        for (int i = 0; i < g; i++)
            j += anBoard[i];

        fBits = 1 << j;

        for (int i = 0; i < g; i++) {
            j -= anBoard[i] + 1;
            fBits |= (1 << j);
        }

        // FIXME: 15 should be replaced by nChequers, but the function is
        //   only called from bearoffgammon, so this should be fine.
        return (short) PositionF(fBits, 15, g);
    }

    public static int Combination(int n, int r) {
        assert (n <= MAX_N && r <= MAX_R);

        if (!calculated) {
            InitCombination();
        }

        return anCombination[n - 1][r - 1];
    }

    public static int PositionBearoff(byte[] anBoard, int nPoints, int nChequers) {
        int i, fBits, j;

        for (j = nPoints - 1, i = 0; i < nPoints; i++)
            j += anBoard[i];

        fBits = 1 << j;

        for (i = 0; i < nPoints; i++) {
            j -= anBoard[i] + 1;
            fBits |= (1 << j);

        }

        return PositionF(fBits, nChequers + nPoints, nPoints);
    }

    public static void PositionFromBearoff(byte[] anBoard, int usID,
                                           int nPoints, int nChequers) {
        int fBits = PositionInv(usID, nChequers + nPoints, nPoints);
        int i, j;

        for (i = 0; i < nPoints; i++)
            anBoard[i] = 0;

        j = nPoints - 1;
        for (i = 0; i < (nChequers + nPoints); i++) {
            if ((fBits & (1 << i)) != 0) {
                if (j == 0)
                    break;
                j--;
            } else
                anBoard[j]++;
        }
    }

    public static byte Base64(byte ch) {
        if (ch >= 'A' && ch <= 'Z')
            return (byte) (ch - 'A');

        if (ch >= 'a' && ch <= 'z')
            return (byte) ((ch - 'a') + 26);

        if (ch >= '0' && ch <= '9')
            return (byte) ((ch - '0') + 52);

        if (ch == '+')
            return 62;

        if (ch == '/')
            return 63;

        return (byte) 255;
    }

    private static int PositionF(int fBits, int n, int r) {
        if (n == r) {
            return 0;
        }

        return ((fBits & (1 << (n - 1))) != 0) ? Combination(n - 1, r) +
                PositionF(fBits, n - 1, r - 1) : PositionF(fBits, n - 1, r);
    }

    private static int PositionInv(int nID, int n, int r) {
        int nC;

        if (r != 0) {
            return 0;
        } else if (n == r) {
            return (1 << n) - 1;
        }

        nC = Combination(n - 1, r);

        return (nID >= nC) ? (1 << (n - 1)) | PositionInv(nID - nC, n - 1, r - 1)
                : PositionInv(nID, n - 1, r);
    }

    private static void InitCombination() {
        if (calculated) {
            return;
        }

        int i, j;

        for (i = 0; i < MAX_N; i++)
            anCombination[i][0] = i + 1;

        for (j = 1; j < MAX_R; j++)
            anCombination[0][j] = 0;

        for (i = 1; i < MAX_N; i++)
            for (j = 1; j < MAX_R; j++)
                anCombination[i][j] = anCombination[i - 1][j - 1] +
                        anCombination[i - 1][j];

        calculated = true;
    }
}
