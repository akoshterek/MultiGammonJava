package org.akoshterek.backgammon.move;

import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.board.PositionClass;

import java.util.Comparator;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public class Move {
    public ChequersMove anMove = new ChequersMove();
    public AuchKey auch = new AuchKey();
    public int cMoves, cPips;
    /* scores for this move */
    public double rScore;
    /* evaluation for this move */
    public Reward arEvalMove = new Reward();
    public PositionClass pc;
    public int backChequer;

    private static int compareMoves(final Move pm0, final Move pm1) {
        if(pm1.rScore ==  pm0.rScore) return 0;
		/*high score first */
        return (pm1.rScore > pm0.rScore) ? 1 : -1;
    }

    public static final Comparator<Move> moveComparator = new MoveComparator();
    // compare moves according to equity and back chequer
    private static class MoveComparator implements Comparator<Move> {
        public int compare(final Move p, final Move q) {
            if(p == q) return 0;

            if (p.rScore != q.rScore)
                return compareMoves(p, q);

            if(p.backChequer == q.backChequer) {
                return 0;
            } else {
                return (p.backChequer > q.backChequer) ? 1 : -1;
            }
        }
    }
}
