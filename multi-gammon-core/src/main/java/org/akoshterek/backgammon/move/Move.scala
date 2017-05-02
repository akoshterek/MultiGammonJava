package org.akoshterek.backgammon.move

import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.board.PositionClass

/**
  * @author Alex
  *         date 20.07.2015.
  */

object Move extends Ordering[Move] {
  def compare(x: Move, y: Move): Int = {
    if (x eq y) {
      0
    } else if (x.rScore != y.rScore) {
      // smaller score is 'smaller' move
      x.rScore.compareTo(y.rScore)
    } else {
      // bigger back chequer is 'smaller' move
      y.backChequer.compareTo(x.backChequer)
    }
  }
}

class Move {
  var anMove: ChequersMove = new ChequersMove
  var auch: AuchKey = new AuchKey
  var cMoves = 0
  var cPips = 0
  /** scores for this move */
  def rScore = arEvalMove.equity
  /** evaluation for this move */
  var arEvalMove: Reward = new Reward()
  var pc: PositionClass = PositionClass.CLASS_OVER
  var backChequer = 0
}