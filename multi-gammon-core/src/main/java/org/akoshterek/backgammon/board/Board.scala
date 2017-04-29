package org.akoshterek.backgammon.board

import org.akoshterek.backgammon.Constants.OUTPUT_LOSEBACKGAMMON
import org.akoshterek.backgammon.Constants.OUTPUT_LOSEGAMMON
import org.akoshterek.backgammon.Constants.OUTPUT_WINBACKGAMMON
import org.akoshterek.backgammon.Constants.OUTPUT_WINGAMMON
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.matchstate.GameResult
import org.akoshterek.backgammon.move.AuchKey
import org.akoshterek.backgammon.move.ChequersMove
import org.akoshterek.backgammon.move.Move
import org.akoshterek.backgammon.move.MoveList
import org.akoshterek.backgammon.util.Base64

object Board {
  val OPPONENT: Int = 0
  val SELF: Int = 1

  val BAR: Int = 24
  val TOTAL_MEN: Int = 15
  val HALF_BOARD_SIZE = 25

  def positionFromKey(auch: AuchKey): Board = {
    var i: Int = 0
    var j: Int = 0
    val newBoard: Board = new Board

    for (a <- auch.key.indices) {
      var cur: Byte = auch.key(a)
      for (_ <- 0 until 8) {
        if ((cur & 0x1) != 0) {
          require(i < 2 && j < Board.HALF_BOARD_SIZE, "Invalid key")
          newBoard.anBoard(i)(j) = (newBoard.anBoard(i)(j) + 1).toByte
        }
        else {
          if ( { j += 1; j } == 25) {
            i += 1
            j = 0
          }
        }
        cur = (cur >> 1).toByte
      }
    }

    newBoard
  }

  def positionFromID(pchEnc: String): Board = {
    val auchKey: AuchKey = new AuchKey
    val ach: Array[Byte] = new Array[Byte](PositionId.L_POSITIONID)
    var pch: Int = 0
    var puch: Int = 0

    for (i <- 0 until PositionId.L_POSITIONID) {
      ach(pch + i) = Base64.base64(pchEnc.charAt(i).toByte)
    }

    for (_ <- 0 until 3) {
      auchKey.key({
        puch += 1; puch - 1
      }) = ((ach(pch) << 2) | (ach(pch + 1) >> 4)).toByte
      auchKey.key({
        puch += 1; puch - 1
      }) = ((ach(pch + 1) << 4) | (ach(pch + 2) >> 2)).toByte
      auchKey.key({
        puch += 1; puch - 1
      }) = ((ach(pch + 2) << 6) | ach(pch + 3)).toByte

      pch += 4
    }

    auchKey.key(puch) = ((ach(pch) << 2) | (ach(pch + 1) >> 4)).toByte
    val anBoard: Board = positionFromKey(auchKey)
    if (!anBoard.checkPosition) {
      throw new IllegalArgumentException("Invalid PositionID")
    }

    anBoard
  }

  private def addBits(auchKey: AuchKey, bitPos: Int, nBits: Int) {
    val k: Int = bitPos >> 3
    val r: Int = bitPos & 0x7
    val b: Long = ((0x1 << nBits) - 1) << r
    auchKey.key(k) = (auchKey.key(k) | b).toByte
    if (k < 8) {
      auchKey.key(k + 1) = (auchKey.key(k + 1) | (b >> 8)).toByte
      auchKey.key(k + 2) = (auchKey.key(k + 2) | (b >> 16)).toByte
    }
    else if (k == 8) {
      auchKey.key(k + 1) = (auchKey.key(k + 1) | (b >> 8)).toByte
    }
  }
}

class Board extends Cloneable {
  val anBoard: Array[Array[Int]] = Array.ofDim[Int](2, 25)

  override def clone(): Board = {
    val board = new Board
    for (i <- this.anBoard.indices) {
      Array.copy(this.anBoard(i), 0, board.anBoard(i), 0, Board.HALF_BOARD_SIZE)
    }
    board
  }

  def swapSides(): Board = {
    val tmp: Array[Int] = anBoard(0)
    anBoard(0) = anBoard(1)
    anBoard(1) = tmp
    this
  }

  def gameResult: GameResult = {
    if (Evaluator.getInstance.classifyPosition(this) != PositionClass.CLASS_OVER) {
      GameResult.PLAYING
    } else {
      val ar: Reward = Evaluator.getInstance.evalOver(this)
      if (ar(OUTPUT_WINBACKGAMMON) != 0 || ar(OUTPUT_LOSEBACKGAMMON) != 0) {
        GameResult.BACKGAMMON
      }
      else if (ar(OUTPUT_WINGAMMON) != 0 || ar(OUTPUT_LOSEGAMMON) != 0) {
        GameResult.GAMMON
      }
      else {
        GameResult.SINGLE
      }
    }
  }

  def initBoard(): Unit = {
    clearBoard()
    anBoard(0)(5) = 5
    anBoard(1)(5) = 5
    anBoard(0)(12) = 5
    anBoard(1)(12) = 5
    anBoard(0)(7) = 3
    anBoard(1)(7) = 3
    anBoard(0)(23) = 2
    anBoard(1)(23) = 2
  }

  def chequersCount(side: Int): Int = anBoard(side).sum

  def chequersCount: (Int, Int) = {
    (chequersCount(Board.OPPONENT), chequersCount(Board.SELF))
  }

  def backChequerIndex(side: Int): Int = {
    anBoard(side).lastIndexWhere(_ > 0)
  }

  def firstChequerIndex(side: Int): Int = {
    anBoard(side).indexWhere(_ > 0)
  }

  def calcPositionKey: AuchKey = {
    var iBit: Int = 0
    val auchKey: AuchKey = new AuchKey
    anBoard.foreach(_.foreach(nc => {
      if (nc != 0) {
        Board.addBits(auchKey, iBit, nc)
        iBit += nc + 1
      }
      else {
        iBit += 1
      }
    }))

    auchKey
  }

  def positionID: String = {
    val auch: AuchKey = calcPositionKey
    PositionId.positionIDFromKey(auch)
  }

  private def applyMove(anMove: ChequersMove): Boolean = {
    !anMove.move.exists(m => {
      m.from >= 0 && !applySubMove(m.from, m.to - m.from, fCheckLegal = false)
    })
  }

  def applySubMove(iSrc: Int, nRoll: Int, fCheckLegal: Boolean): Boolean = {
    val iDest: Int = iSrc - nRoll

    if (fCheckLegal && (nRoll < 1 || nRoll > 6)) {
      // Invalid dice roll
      return false
    }

    if (iSrc < 0 || iSrc > 24 || iDest > 24 || anBoard(1)(iSrc) < 1) {
      // Invalid point number, or source point is empty
      return false
    }

    anBoard(Board.SELF)(iSrc) = (anBoard(Board.SELF)(iSrc) - 1).toByte

    if (iDest < 0) {
      return true
    }

    if (anBoard(0)(23 - iDest) != 0) {
      if (anBoard(0)(23 - iDest) > 1) {
        // Trying to move to a point already made by the opponent
        return false
      }

      //blot hit
      anBoard(Board.SELF)(iDest) = 1
      anBoard(Board.OPPONENT)(23 - iDest) = 0

      //send to bar
      anBoard(Board.OPPONENT)(Board.BAR) += 1
    }
    else {
      anBoard(Board.SELF)(iDest) += 1
    }

    true
  }

  def isLegalMove(iSrc: Int, nPips: Int): Boolean = {
    val iDest: Int = iSrc - nPips

    if (iDest >= 0) {
      // Here we can do the Chris rule check
      anBoard(Board.OPPONENT)(23 - iDest) < 2
    } else {
      // otherwise, attempting to bear off
      val nBack = anBoard(Board.SELF).indexWhere(_ > 0, 1)
      nBack <= 5 && (iSrc == nBack || iDest == -1)
    }
  }

  def saveMoves(pml: MoveList, cMoves: Int, cPip: Int, anMoves: ChequersMove) {
      //Save only legal moves: if the current move moves plays less
      //chequers or pips than those already found, it is illegal; if
      //it plays more, the old moves are illegal.
      if (cMoves < pml.cMaxMoves || cPip < pml.cMaxPips)
        return

      if (cMoves > pml.cMaxMoves || cPip > pml.cMaxPips)
        pml.cMoves = 0

      pml.cMaxMoves = cMoves
      pml.cMaxPips = cPip

    val pm: Move = pml.amMoves(pml.cMoves)
    val auch: AuchKey = calcPositionKey

    for (i <- 0 until pml.cMoves) {
      if (auch == pml.amMoves(i).auch) {
        if (cMoves > pml.amMoves(i).cMoves || cPip > pml.amMoves(i).cPips) {
          pml.amMoves(i).anMove.copyFrom(anMoves)
          pml.amMoves(i).cMoves = cMoves
          pml.amMoves(i).cPips = cPip
        }

        return
      }
    }

    pm.anMove.copyFrom(anMoves)
    pm.auch = auch

    pm.cMoves = cMoves
    pm.cPips = cPip
    pm.backChequer = backChequerIndex(Board.SELF)

    pm.arEvalMove = new Reward()
    pml.cMoves += 1
    require(pml.cMoves < MoveList.MAX_INCOMPLETE_MOVES)
  }

  def locateMove(anMove: ChequersMove, pml: MoveList): Int = {
    val moveKey = calcMoveKey(anMove)
    pml.amMoves.take(pml.cMoves).indexWhere(m => moveKey == calcMoveKey(m.anMove))
  }

  private def calcMoveKey(anMove: ChequersMove): AuchKey = {
    val anBoardMove: Board = clone()
    anBoardMove.applyMove(anMove)
    anBoardMove.calcPositionKey
  }

  private def checkSamePoint: Boolean = {
    for (i <- 0 until 24) {
      if (anBoard(0)(i) != 0 && anBoard(1)(23 - i) != 0) {
        return false
      }
    }

    true
  }

  private def checkPosition: Boolean = {
    // Check for a player with over 15 chequers
    val (opponent, self) = chequersCount
    if (opponent > 15 || self > 15) {
      return false
    }

    // Check for both players having chequers on the same point
    if (!checkSamePoint) {
      return false
    }

    // Check for both players on the bar against closed boards
    for (i <- 0 until 6) {
      if (anBoard(Board.OPPONENT)(i) < 2 || anBoard(Board.SELF)(i) < 2)
        return true
    }

    anBoard(0)(Board.BAR) == 0 || anBoard(1)(Board.BAR) == 0
  }

  private def canEqual(a: Any) = a.isInstanceOf[Board]

  override def equals(that: Any): Boolean = {
    that match {
      case that: Board => that.canEqual(this) && anBoard.deep == that.anBoard.deep
      case _ => false
    }
  }

  private def clearBoard() {
    anBoard.transform(_ => Array.fill[Int](Board.HALF_BOARD_SIZE)(0))
  }
}