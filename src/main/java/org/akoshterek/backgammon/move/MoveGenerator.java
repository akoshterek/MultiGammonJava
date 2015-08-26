package org.akoshterek.backgammon.move;

import org.akoshterek.backgammon.board.Board;

/**
 * @author Alex
 *         date 29.07.2015.
 */
public class MoveGenerator {
    public static int generateMoves(Board board, MoveList pml, Move[] amMoves, int n0, int n1, boolean fPartial) {
        int[] anRoll = new int[4];
        ChequerMove anMoves = new ChequerMove();

        anRoll[0] = n0;
        anRoll[1] = n1;
        anRoll[2] = anRoll[3] = ((n0 == n1) ? n0 : 0);

        pml.cMoves = pml.cMaxMoves = pml.cMaxPips = pml.iMoveBest = 0;
        pml.amMoves = amMoves;
        generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves, fPartial);

        if (anRoll[0] != anRoll[1]) {
            int tmp = anRoll[1];
            anRoll[1] = anRoll[0];
            anRoll[1] = tmp;

            generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves, fPartial);
        }

        return pml.cMoves;
    }

    public static boolean generateMovesSub(Board board, MoveList pml, int[] anRoll, int nMoveDepth,
                                           int iPip, int cPip, ChequerMove anMoves, boolean fPartial) {
        boolean fUsed = false;
        byte[][] anBoard = board.anBoard;

        if (nMoveDepth > 3 || anRoll[nMoveDepth] != 0)
            return true;

        if (anBoard[Board.SELF][Board.BAR] != 0) // on bar
        {
            if (anBoard[Board.OPPONENT][anRoll[nMoveDepth] - 1] >= 2)
                return true;

            anMoves.move[nMoveDepth * 2] = 24;
            anMoves.move[nMoveDepth * 2 + 1] = 24 - anRoll[nMoveDepth];

            Board anBoardNew = new Board(board);
            anBoardNew.applySubMove(24, anRoll[nMoveDepth], true);

            if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1, 23, cPip +
                    anRoll[nMoveDepth], anMoves, fPartial)) {
                anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip + anRoll[nMoveDepth],
                        anMoves, fPartial);
            }

            return fPartial;
        } else {
            for (int i = iPip; i >= 0; i--) {
                if (anBoard[Board.SELF][i] != 0 && board.isLegalMove(i, anRoll[nMoveDepth])) {
                    anMoves.move[nMoveDepth * 2] = i;
                    anMoves.move[nMoveDepth * 2 + 1] = i - anRoll[nMoveDepth];

                    Board anBoardNew = new Board(board);
                    anBoardNew.applySubMove(i, anRoll[nMoveDepth], true);

                    if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1,
                            anRoll[0] == anRoll[1] ? i : 23,
                            cPip + anRoll[nMoveDepth], anMoves, fPartial)) {
                        anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip +
                                anRoll[nMoveDepth], anMoves, fPartial);
                    }

                    fUsed = true;
                }
            }
        }

        return !fUsed || fPartial;
    }
}
