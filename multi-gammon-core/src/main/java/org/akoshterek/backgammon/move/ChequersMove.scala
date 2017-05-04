package org.akoshterek.backgammon.move

class ChequersMove {
  var move: Array[ChequerMove] = Array[ChequerMove](
    new ChequerMove,
    new ChequerMove,
    new ChequerMove,
    new ChequerMove
  )

  def copyFrom(o: ChequersMove): Unit = {
    move = for (e <- o.move) yield e.clone()
  }

  override def toString: String = move.filter(m => m.from >= 0).sorted.mkString(", ")
}