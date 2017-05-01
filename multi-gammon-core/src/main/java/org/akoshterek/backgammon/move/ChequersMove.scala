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

  override def toString: String = {
    val builder: StringBuilder = new StringBuilder
    move.sorted.foreach(aMove => builder.append(aMove.toString))
    builder.toString
  }
}