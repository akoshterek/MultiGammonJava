package org.akoshterek.backgammon.move

import org.akoshterek.backgammon.board.BoardFormatter

/**
  * Created by Alex on 23.04.2017.
  */
class ChequerMove extends Ordered[ChequerMove] with Cloneable {
  private var _from: Int = -1
  private var _to: Int = -1

  def this(that: ChequerMove) = {
    this()
    _from = that._from
    _to = that._to
  }

  def from: Int = _from

  def from_=(value: Int): Unit = _from = value

  def to: Int = _to

  def to_=(value: Int): Unit = _to = Math.max(-1, value)

  override def compare(that: ChequerMove): Int = {
    val o1 = that
    val o2 = this

    if (o1.from != o2.from) {
      Integer.valueOf(o1.from).compareTo(o2.from)
    } else if (o1.to != o2.to) {
      Integer.valueOf(o1.to).compareTo(o2.to)
    } else {
      0
    }
  }

  override def clone(): ChequerMove = {
    new ChequerMove(this)
  }

  override def toString: String = {
    if (from >= 0) {
      String.format("%s/%s",
        BoardFormatter.formatPointPlain(from + 1),
        BoardFormatter.formatPointPlain(to + 1))
    } else {
      ""
    }
  }
}
