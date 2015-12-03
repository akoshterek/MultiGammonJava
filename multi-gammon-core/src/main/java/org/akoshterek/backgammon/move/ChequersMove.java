package org.akoshterek.backgammon.move;

import org.akoshterek.backgammon.board.BoardFormatter;

import java.util.Arrays;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public class ChequersMove {
    public class ChequerMove {
        public int from = -1;
        public int to = -1;
    }

    public ChequerMove[] move = new ChequerMove[]{
            new ChequerMove(),
            new ChequerMove(),
            new ChequerMove(),
            new ChequerMove()
    };

    public void reset() {
        for (ChequerMove aMove : move) {
            aMove.from = -1;
            aMove.to = -1;
        }
    }

    public void copyFrom(ChequersMove o) {
        for(int i = 0; i < move.length; i++) {
            move[i].from = o.move[i].from;
            move[i].to = o.move[i].to;
        }
    }

    public void sort() {
        Arrays.sort(move, (o2, o1) -> {
            if(o1.from > o2.from)
                return 1;
            else if (o1.from < o2.from)
                return -1;
            else
                return Integer.valueOf(o1.to).compareTo(o2.to);
        });
    }

    public String toString() {
        sort();
        StringBuilder builder = new StringBuilder();
        for (ChequerMove aMove : move) {
            if (aMove.from >= 0) {
                builder.append(String.format("%s/%s ",
                        BoardFormatter.formatPointPlain(aMove.from + 1),
                        BoardFormatter.formatPointPlain(aMove.to + 1)));
            }
        }

        return builder.toString();
    }
}
