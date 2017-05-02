package org.akoshterek.backgammon.move

import scala.collection.mutable.ArrayBuffer

/**
  * @author Alex
  *         date 20.07.2015.
  */
object MoveList {
  /* A trivial upper bound on the number of (complete or incomplete)
 * legal moves of a single roll: if all 15 chequers are spread out,
 * then there are 18 C 4 + 17 C 3 + 16 C 2 + 15 C 1 = 3875
 * combinations in which a roll of 11 could be played (up to 4 choices from
 * 15 chequers, and a chequer may be chosen more than once).  The true
 * bound will be lower than this (because there are only 26 points,
 * some plays of 15 chequers must "overlap" and map to the same
 * resulting position), but that would be more difficult to
 * compute. */
  val MAX_INCOMPLETE_MOVES = 3875
  val MAX_MOVES = 3060
}

class MoveList {
  var cMaxMoves: Int = 0
  var cMaxPips: Int = 0
  var amMoves: ArrayBuffer[Move] = ArrayBuffer[Move]()
  def cMoves = amMoves.length

  def this(src: MoveList) {
    this()
    amMoves.clear()
    amMoves :+ src.amMoves
    cMaxMoves = src.cMaxMoves
    cMaxPips = src.cMaxPips
  }

  def deleteMoves(): Unit = {
    amMoves.clear()
  }
}
