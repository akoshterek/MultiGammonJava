package org.akoshterek.backgammon.move

import java.util

import org.akoshterek.backgammon.board.BoardFormatter
import java.util.Comparator

class ChequersMove {

  class ChequerMove {
    var from: Int = -1
    var to: Int = -1
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
    util.Arrays.sort (move, new Comparator[ChequerMove] {
      override def compare(o2: ChequerMove, o1: ChequerMove): Int = {
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
    })
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