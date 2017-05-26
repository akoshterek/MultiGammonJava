package org.akoshterek.backgammon.eval

import org.akoshterek.backgammon.bearoff.Bearoff
import org.akoshterek.backgammon.board.{Board, PositionClass}

/**
  * Created by oleksii on 26-May-17.
  */
object PositionClassificator {
  def classifyPosition(anBoard: Board): PositionClass = {
    val nOppBack: Int = anBoard.backChequerIndex(Board.OPPONENT)
    val nBack: Int = anBoard.backChequerIndex(Board.SELF)

    if (nBack < 0 || nOppBack < 0) {
      PositionClass.CLASS_OVER
    } else if (nBack + nOppBack > 22) {
      // normal backgammon
      classifyContact(anBoard)
    }
    else {
      classifyBearoff(anBoard)
    }
  }

  private def classifyContact(anBoard: Board): PositionClass = {
    classifyContactSide(anBoard(Board.OPPONENT)) match {
      case PositionClass.CLASS_CONTACT => classifyContactSide(anBoard(Board.SELF))
      case res => res
    }
  }

  private def classifyContactSide(board: Array[Int]): PositionClass = {
    // contact position

    val N: Int = 6 //crashed edge
    val tot = board.sum
    var res = PositionClass.CLASS_CONTACT

    if (tot <= N) {
      res = PositionClass.CLASS_CRASHED
    }
    else if (board(0) > 1) {
      if (tot <= (N + board(0))) {
        res = PositionClass.CLASS_CRASHED
      } else if (board(1) > 1 && (1 + tot - (board(0) + board(1))) <= N) {
        res = PositionClass.CLASS_CRASHED
      }
    }
    else if (tot <= (N + (board(1) - 1))) {
      res = PositionClass.CLASS_CRASHED
    }

    res
  }

  private def classifyBearoff(anBoard: Board): PositionClass = {
    if (Bearoff.isBearoff(Evaluator.getInstance().getPbc2, anBoard)) {
      PositionClass.CLASS_BEAROFF2
    } else if (Bearoff.isBearoff(Evaluator.getInstance().getPbc1, anBoard)) {
      PositionClass.CLASS_BEAROFF1
    } else {
      PositionClass.CLASS_RACE
    }
  }
}
