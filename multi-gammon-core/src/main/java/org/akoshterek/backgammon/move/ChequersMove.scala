package org.akoshterek.backgammon.move

import org.akoshterek.backgammon.board.BoardFormatter

class ChequersMove {

    class ChequerMove extends Ordered[ChequerMove] {
        var from: Int = -1
        var to: Int = -1

        override def compare(that: ChequerMove): Int = {
            val o1 = that
            val o2 = this

            if (o1.from > o2.from) {
                1
            }
            else if (o1.from < o2.from) {
                -1
            }
            else {
                Integer.valueOf(o1.to).compareTo(o2.to)
            }
        }
    }

    var move: Array[ChequerMove] = Array[ChequerMove](
        new ChequerMove,
        new ChequerMove,
        new ChequerMove,
        new ChequerMove
    )

    def copyFrom(o: ChequersMove) {
        for (i <- move.indices) {
            move(i).from = o.move(i).from
            move(i).to = o.move(i).to
        }
    }

    private def sort() {
        scala.util.Sorting.quickSort(move)
    }

    override def toString: String = {
        sort()
        val builder: StringBuilder = new StringBuilder
        for (aMove <- move) {
            if (aMove.from >= 0) {
                builder.append(String.format("%s/%s ", BoardFormatter.formatPointPlain(aMove.from + 1), BoardFormatter.formatPointPlain(aMove.to + 1)))
            }
        }
        builder.toString
    }
}