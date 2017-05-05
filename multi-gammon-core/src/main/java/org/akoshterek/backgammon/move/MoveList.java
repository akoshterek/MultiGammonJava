package org.akoshterek.backgammon.move;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public class MoveList {
	/* A trivial upper bound on the number of (complete or incomplete)
	 * legal moves of a single roll: if all 15 chequers are spread out,
	 * then there are 18 C 4 + 17 C 3 + 16 C 2 + 15 C 1 = 3875
	 * combinations in which a roll of 11 could be played (up to 4 choices from
	 * 15 chequers, and a chequer may be chosen more than once).  The true
	 * bound will be lower than this (because there are only 26 points,
	 * some plays of 15 chequers must "overlap" and map to the same
	 * resulting position), but that would be more difficult to
	 * compute. */
    public static final int MAX_INCOMPLETE_MOVES = 3875;
    public static final int MAX_MOVES = 3060;

    public int cMoves = 0; /* and current move when building list */
    public int cMaxMoves, cMaxPips;
    public Move[] amMoves = null;

    MoveList() {}

    public MoveList(MoveList src) {
        if (this == src) {
            return;
        }

        if (src.cMoves != 0) {
            if (src.cMoves > cMoves) {
                deleteMoves();
                amMoves = new Move[src.cMoves];
                for(int i = 0; i < src.cMoves; i++) {
                    amMoves[i] = new Move();
                }
            }
            System.arraycopy(src.amMoves, 0, amMoves, 0, src.cMoves);
        } else {
            deleteMoves();
        }

        cMoves = src.cMoves;
        cMaxMoves = src.cMaxMoves;
        cMaxPips = src.cMaxPips;
    }

    public void deleteMoves() {
        amMoves = null;
        cMoves = 0;
    }
}
