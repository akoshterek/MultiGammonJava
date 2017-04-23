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

  private def sort() {
    scala.util.Sorting.quickSort(move)
  }

  override def toString: String = {
    sort()
    val builder: StringBuilder = new StringBuilder
    move.foreach(aMove => builder.append(aMove.toString))
    builder.toString
  }
}