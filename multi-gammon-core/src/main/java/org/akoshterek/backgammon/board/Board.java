package org.akoshterek.backgammon.board;

import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.move.AuchKey;
import org.akoshterek.backgammon.move.ChequerMove;
import org.akoshterek.backgammon.move.Move;
import org.akoshterek.backgammon.move.MoveList;

import java.util.Arrays;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date 19.07.2015.
 */
public class Board {
    public static final int OPPONENT = 0;
    public static final int SELF = 1;

    public static final int BAR = 24;
    public static final int TOTAL_MEN = 15;

    public byte[][] anBoard = new byte[2][25];

    public Board() {
        clearBoard();
    }

    public Board(Board board) {
        System.arraycopy(board.anBoard[0], 0, this.anBoard[0], 0, this.anBoard[0].length);
        System.arraycopy(board.anBoard[1], 0, this.anBoard[1], 0, this.anBoard[1].length);
    }

    public void swapSides() {
        for(int i = 0; i < 25; i++ )
        {
            byte n = anBoard[ OPPONENT ][ i ];
            anBoard[ OPPONENT ][ i ] = anBoard[ SELF ][ i ];
            anBoard[ SELF ][ i ] = n;
        }
    }

    public int gameStatus() {
        if (Evaluator.getInstance().classifyPosition(this) != PositionClass.CLASS_OVER) {
            return 0;
        }

        Reward ar = Evaluator.getInstance().evalOver(this);
        if (ar.data[OUTPUT_WINBACKGAMMON] != 0 || ar.data[OUTPUT_LOSEBACKGAMMON] != 0) {
            return 3;
        } else if (ar.data[OUTPUT_WINGAMMON] != 0 || ar.data[OUTPUT_LOSEGAMMON] != 0) {
            return 2;
        } else {
            return 1;
        }
    }

    public void initBoard() {
        clearBoard();

        anBoard[0][5]  = anBoard[1][5]  = anBoard[0][12] = anBoard[1][12] = 5;
        anBoard[0][7]  = anBoard[1][7]  = 3;
        anBoard[0][23] = anBoard[1][23] = 2;
    }

    public void chequersCount(int[] anChequers) {
        anChequers[OPPONENT] = 0;
        anChequers[SELF] = 0;

        for (int i = 0; i < 25; i++) {
            anChequers[OPPONENT] += anBoard[OPPONENT][i];
            anChequers[SELF] += anBoard[SELF][i];
        }
    }

    public int calcBackChequer() {
        int back = -1;
        for (int b = 24; b > -1; b--) {
            if (anBoard[SELF][b] > 0) {
                back = b;
                break;
            }
        }

        return back;
    }

    public AuchKey PositionKey() {
        int i, iBit = 0;
        AuchKey auchKey = new AuchKey();

        for(i = 0; i < 2; i++) {
            for(int j = 0; j < 25; ++j) {
                int nc = anBoard[i][j];

                if( nc != 0) {
                    addBits(auchKey, iBit, nc);
                    iBit += nc + 1;
                } else {
                    ++iBit;
                }
            }
        }

        return auchKey;
    }

    public String positionID() {
        AuchKey auch = PositionKey();
        return PositionId.positionIDFromKey(auch);
    }

    public static Board positionFromKey(AuchKey auch) {
        int i = 0, j = 0;
        Board newBoard = new Board();

        for (int a = 0; a < auch.key.length; a++) {
            byte cur = auch.key[a];

            for (int k = 0; k < 8; ++k) {
                if ((cur & 0x1) != 0) {
                    if (i >= 2 || j >= 25) {
                        throw new RuntimeException("Invalid key");
                    }
                    ++newBoard.anBoard[i][j];
                } else {
                    if (++j == 25) {
                        ++i;
                        j = 0;
                    }
                }
                cur >>= 1;
            }
        }

        return newBoard;
    }

    public static Board positionFromID(String pchEnc) {
        AuchKey auchKey = new AuchKey();
        byte[] ach = new byte[PositionId.L_POSITIONID];
        int pch = 0;
        int puch = 0;
        int i;

        for (i = 0; i < PositionId.L_POSITIONID; i++)
            ach[pch + i] = PositionId.Base64((byte) pchEnc.charAt(i));

        for (i = 0; i < 3; i++) {
            auchKey.key[puch++] = (byte) ((ach[pch] << 2) | (ach[pch + 1] >> 4));
            auchKey.key[puch++] = (byte) ((ach[pch + 1] << 4) | (ach[pch + 2] >> 2));
            auchKey.key[puch++] = (byte) ((ach[pch + 2] << 6) | ach[pch + 3]);

            pch += 4;
        }

        auchKey.key[puch] = (byte) ((ach[pch] << 2) | (ach[pch + 1] >> 4));
        Board anBoard = positionFromKey(auchKey);
        if(!anBoard.checkPosition()) {
            throw new IllegalArgumentException("Invalid PositionID");
        }

        return anBoard;
    }

    public boolean applyMove(ChequerMove anMove, boolean fCheckLegal) {
        for (int i = 0; i < anMove.move.length && anMove.move[i] >= 0; i += 2)
            if (!applySubMove(anMove.move[i], anMove.move[i] - anMove.move[i + 1], fCheckLegal)) {
                return false;
            }

        return true;
    }

    public boolean applySubMove(int iSrc, int nRoll, boolean fCheckLegal) {
        int iDest = iSrc - nRoll;

        if (fCheckLegal && (nRoll < 1 || nRoll > 6)) {
            // Invalid dice roll
            return false;
        }

        if (iSrc < 0 || iSrc > 24 || iDest > 24 || anBoard[1][iSrc] < 1) {
            // Invalid point number, or source point is empty
            return false;
        }

        anBoard[SELF][iSrc]--;

        if (iDest < 0) {
            return true;
        }

        if (anBoard[0][23 - iDest] != 0) {
            if (anBoard[0][23 - iDest] > 1) {
                // Trying to move to a point already made by the opponent
                return false;
            }

            //blot hit
            anBoard[SELF][iDest] = 1;
            anBoard[OPPONENT][23 - iDest] = 0;
            //send to bar
            anBoard[OPPONENT][BAR]++;
        } else {
            anBoard[SELF][iDest]++;
        }

        return true;
    }

    public boolean isLegalMove(int iSrc, int nPips) {
        int i, nBack = 0, iDest = iSrc - nPips;

        if (iDest >= 0) {
            // Here we can do the Chris rule check
            return (anBoard[OPPONENT][23 - iDest] < 2);
        }

        // otherwise, attempting to bear off
        for (i = 1; i < 25; i++)
            if (anBoard[SELF][i] > 0)
                nBack = i;

        return (nBack <= 5 && (iSrc == nBack || iDest == -1));
    }

    public void saveMoves(MoveList pml, int cMoves, int cPip, ChequerMove anMoves, boolean fPartial) {
        int i, j;
        Move pm;

        if (fPartial) {
            //Save all moves, even incomplete ones *
            if (cMoves > pml.cMaxMoves) {
                pml.cMaxMoves = cMoves;
            }

            if (cPip > pml.cMaxPips) {
                pml.cMaxPips = cPip;
            }
        } else {
            //Save only legal moves: if the current move moves plays less
            //chequers or pips than those already found, it is illegal; if
            //it plays more, the old moves are illegal.
            if (cMoves < pml.cMaxMoves || cPip < pml.cMaxPips)
                return;

            if (cMoves > pml.cMaxMoves || cPip > pml.cMaxPips)
                pml.cMoves = 0;

            pml.cMaxMoves = cMoves;
            pml.cMaxPips = cPip;
        }

        pm = pml.amMoves[pml.cMoves];
        AuchKey auch = PositionKey();

        for (i = 0; i < pml.cMoves; i++) {
            if (auch.equals(pml.amMoves[i].auch)) {
                if (cMoves > pml.amMoves[i].cMoves ||
                        cPip > pml.amMoves[i].cPips) {
                    for (j = 0; j < cMoves * 2; j++) {
                        pml.amMoves[i].anMove.move[j] = anMoves.move[j] > -1 ? anMoves.move[j] : -1;
                    }

                    if (cMoves < 4) {
                        pml.amMoves[i].anMove.move[cMoves * 2] = -1;
                    }

                    pml.amMoves[i].cMoves = cMoves;
                    pml.amMoves[i].cPips = cPip;
                }

                return;
            }
        }

        for (i = 0; i < cMoves * 2; i++) {
            pm.anMove.move[i] = anMoves.move[i] > -1 ? anMoves.move[i] : -1;
        }

        if (cMoves < 4) {
            pm.anMove.move[cMoves * 2] = -1;
        }

        pm.auch = auch;

        pm.cMoves = cMoves;
        pm.cPips = cPip;
        pm.backChequer = calcBackChequer();
        //pm->cmark = CMARK_NONE;

        pm.arEvalMove.reset();
        pml.cMoves++;
        assert (pml.cMoves < MoveList.MAX_INCOMPLETE_MOVES);
    }

    public int locateMove(ChequerMove anMove, MoveList pml) {
        AuchKey key = calcMoveKey(anMove);

        for (int i = 0; i < pml.cMoves; ++i) {
            AuchKey auch = calcMoveKey(pml.amMoves[i].anMove);
            if (auch.equals(key)) {
                return i;
            }
        }

        return 0;
    }

    private AuchKey calcMoveKey(ChequerMove anMove) {
        Board anBoardMove = new Board(this);
        anBoardMove.applyMove(anMove, false);
        return anBoardMove.PositionKey();
    }

    private boolean checkPosition() {
        int[] ac = new int[]{0, 0};
        int i;

        // Check for a player with over 15 chequers
        for (i = 0; i < 25; i++) {
            ac[0] += anBoard[0][i];
            ac[1] += anBoard[1][i];
            if (ac[0] > 15 || ac[1] > 15) {
                return false;
            }
        }

        // Check for both players having chequers on the same point
        for (i = 0; i < 24; i++)
            if (anBoard[0][i] != 0 && anBoard[1][23 - i] != 0) {
                return false;
            }

        // Check for both players on the bar against closed boards
        for (i = 0; i < 6; i++)
            if (anBoard[0][i] < 2 || anBoard[1][i] < 2)
                return true;

        return anBoard[0][BAR] == 0 || anBoard[1][BAR] == 0;
    }

    public boolean equals(Object board) {
        if(board == null) return false;
        if(board == this) return true;

        if(getClass() != board.getClass()) return false;

        Board that = (Board)board;
        for(int i = 0; i < 2; i++) {
            if (!Arrays.equals(anBoard[i], that.anBoard[i]))
                return false;
        }

        return true;
    }

    private void addBits(AuchKey auchKey, int bitPos, int nBits)
    {
        int k = bitPos >> 3;
        int r = (bitPos & 0x7);
        long b = ((0x1 << nBits) - 1) << r;

        auchKey.key[k] |= (byte) b;

        if (k < 8) {
            auchKey.key[k + 1] |= (byte) (b >> 8);
            auchKey.key[k + 2] |= (byte) (b >> 16);
        } else if (k == 8) {
            auchKey.key[k + 1] |= (byte) (b >> 8);
        }
    }

    private void clearBoard() {
        Arrays.fill(anBoard[0], (byte)0);
        Arrays.fill(anBoard[1], (byte)0);
    }
}
