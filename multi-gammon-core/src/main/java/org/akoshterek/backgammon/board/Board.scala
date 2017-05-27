package org.akoshterek.backgammon.board

import org.akoshterek.backgammon.Constants._
import org.akoshterek.backgammon.eval.{Evaluator, Reward}
import org.akoshterek.backgammon.matchstate.GameResult
import org.akoshterek.backgammon.move.{AuchKey, ChequersMove, Move, MoveList}
import org.akoshterek.backgammon.util.Base64

object Board {
  final val OPPONENT: Int = 0
  final val SELF: Int = 1

  final val BAR: Int = 24
  final val TOTAL_MEN: Int = 15
  final val HALF_BOARD_SIZE = 25

  def positionFromKey(auch: AuchKey): Board = {
    var i: Int = 0
    var j: Int = 0
    val newBoard: Board = new Board

    var a = 0
    while (a < auch.key.length) {
      var cur: Byte = auch.key(a)
      var k = 0
      while (k < 8) {
        if ((cur & 0x1) != 0) {
          require(i < 2 && j < Board.HALF_BOARD_SIZE, "Invalid key")
          newBoard.anBoard(i)(j) = (newBoard.anBoard(i)(j) + 1).toByte
        }
        else {
          j += 1
          if (j == 25) {
            i += 1
            j = 0
          }
        }
        cur = (cur >> 1).toByte
        k += 1
      }

      a += 1
    }

    newBoard
  }

  def positionFromID(pchEnc: String): Board = {
    val auchKey: AuchKey = new AuchKey
    val ach: Array[Int] = new Array[Int](PositionId.L_POSITIONID)
    var pch: Int = 0
    var puch: Int = 0

    var i = 0
    while (i < PositionId.L_POSITIONID) {
      ach(pch + i) = Base64.base64(pchEnc.charAt(i).toByte)
      i += 1
    }

    i = 0
    while (i < 3) {
      auchKey.key(puch) = ((ach(pch) << 2) | (ach(pch + 1) >> 4)).toByte
      puch += 1
      auchKey.key(puch) = ((ach(pch + 1) << 4) | (ach(pch + 2) >> 2)).toByte
      puch += 1
      auchKey.key(puch) = ((ach(pch + 2) << 6) | ach(pch + 3)).toByte
      puch += 1

      pch += 4
      i += 1
    }

    auchKey.key(puch) = ((ach(pch) << 2) | (ach(pch + 1) >> 4)).toByte
    val anBoard: Board = positionFromKey(auchKey)
    require(anBoard.checkPosition, "Invalid PositionID")

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

  def apply(board: Array[Array[Int]]): Board = new Board(board)

  def apply(): Board = new Board()

  def initialPosition: Board = {
    val anBoard: Array[Array[Int]] = Array.ofDim[Int](2, 25)
    anBoard(0)(5) = 5
    anBoard(1)(5) = 5
    anBoard(0)(12) = 5
    anBoard(1)(12) = 5
    anBoard(0)(7) = 3
    anBoard(1)(7) = 3
    anBoard(0)(23) = 2
    anBoard(1)(23) = 2

    Board(anBoard)
  }
}

class Board extends Cloneable {
  val anBoard: Array[Array[Int]] = Array.ofDim[Int](2, 25)

  def this(board: Array[Array[Int]]) {
    this()
    require(board.length == 2
      && board(0).length == Board.HALF_BOARD_SIZE
      && board(1).length == Board.HALF_BOARD_SIZE, "Invalid board array")
    this.anBoard(0) = board(0).clone()
    this.anBoard(1) = board(1).clone()
  }

  override def clone(): Board = {
    val board = new Board
    board.anBoard(0) = this.anBoard(0).clone()
    board.anBoard(1) = this.anBoard(1).clone()
    board
  }

  def apply(side: Int): Array[Int] = anBoard(side)
  
  def swapSides: Board = {
    val board: Array[Array[Int]] = Array.ofDim[Int](2, 25)
    board(0) = this.anBoard(1).clone()
    board(1) = this.anBoard(0).clone()
    Board(board)
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

  def chequersCount(side: Int): Int = {
    var count = 0
    var i = 0
    while (i < anBoard(side).length) {
      count += anBoard(side)(i)
      i += 1
    }
    count
  }

  def chequersCount: (Int, Int) = {
    (chequersCount(Board.OPPONENT), chequersCount(Board.SELF))
  }

  def backChequerIndex(side: Int): Int = {
    var i = anBoard(side).length - 1
    while (i >= 0 && anBoard(side)(i) == 0) i -= 1
    i
  }

  def firstChequerIndex(side: Int): Int = {
    def negLength(n: Int) = if (n >= Board.HALF_BOARD_SIZE) -1 else n

    def segmentLength: Int = {
      var i = 0
      while (i < Board.HALF_BOARD_SIZE && anBoard(side)(i) == 0) {
        i += 1
      }
      i
    }

    negLength(segmentLength)
  }


  //def firstChequerIndex(side: Int): Int = {
  //  anBoard(side).indexWhere(_ > 0)
  //}

  def calcPositionKey: AuchKey = {
    var iBit: Int = 0
    val auchKey: AuchKey = new AuchKey
    var side = 0
    while (side < 2) {
      var i = 0
      while (i < anBoard(side).length) {
        val nc = anBoard(side)(i)
        if (nc != 0) {
          Board.addBits(auchKey, iBit, nc)
          iBit += nc + 1
        }
        else {
          iBit += 1
        }
        i += 1
      }
      side += 1
    }

    auchKey
  }

  def positionID: String = PositionId.positionIDFromKey(calcPositionKey)

  private def applyMove(anMove: ChequersMove): Boolean = {
    !anMove.move.exists(m => {
      m.from >= 0 && !applySubMove(m.from, m.to - m.from, fCheckLegal = false)
    })
  }

  def applySubMove(iSrc: Int, nRoll: Int, fCheckLegal: Boolean): Boolean = {
    val iDest: Int = iSrc - nRoll

    if (!isLegalSubMove(iSrc, nRoll, fCheckLegal)) {
      false
    } else {
      anBoard(Board.SELF)(iSrc) = (anBoard(Board.SELF)(iSrc) - 1).toByte
      if (iDest < 0) {
        true
      } else {
        if (anBoard(0)(23 - iDest) != 0) {
          var res = true
          if (anBoard(0)(23 - iDest) > 1) {
            // Trying to move to a point already made by the opponent
            res = false
          }

          if (res) {
            //blot hit
            anBoard(Board.SELF)(iDest) = 1
            anBoard(Board.OPPONENT)(23 - iDest) = 0

            //send to bar
            anBoard(Board.OPPONENT)(Board.BAR) += 1
          }
          res
        }
        else {
          anBoard(Board.SELF)(iDest) += 1
          true
        }
      }
    }
  }

  def isLegalSubMove(iSrc: Int, nRoll: Int, fCheckLegal: Boolean): Boolean = {
    val iDest: Int = iSrc - nRoll
    if (fCheckLegal && (nRoll < 1 || nRoll > 6)) {
      // Invalid dice roll
      false
    } else if (iSrc < 0 || iSrc > 24 || iDest > 24 || anBoard(1)(iSrc) < 1) {
      // Invalid point number, or source point is empty
      false
    } else {
      true
    }
  }

  def isLegalMove(iSrc: Int, nPips: Int): Boolean = {
    val iDest: Int = iSrc - nPips

    if (iDest >= 0) {
      // Here we can do the Chris rule check
      anBoard(Board.OPPONENT)(23 - iDest) < 2
    } else {
      // otherwise, attempting to bear off
      val nBack = Math.max(0, backChequerIndex(Board.SELF))
      nBack <= 5 && (iSrc == nBack || iDest == -1)
    }
  }

  def saveMoves(pml: MoveList, cMoves: Int, cPip: Int, anMoves: ChequersMove) {
    //Save only legal moves: if the current move moves plays less
    //chequers or pips than those already found, it is illegal; if
    //it plays more, the old moves are illegal.
    if (cMoves >= pml.cMaxMoves && cPip >= pml.cMaxPips) {
      if (cMoves > pml.cMaxMoves || cPip > pml.cMaxPips) {
        pml.cMoves = 0
      }

      pml.cMaxMoves = cMoves
      pml.cMaxPips = cPip

      val auch: AuchKey = calcPositionKey
      pml.amMoves
        .take(pml.cMoves)
        .filter(m => auch == m.auch && (cMoves > m.cMoves || cPip > m.cPips))
        .take(1).headOption match {
        case Some(m) =>
          m.anMove.copyFrom(anMoves)
          m.cMoves = cMoves
          m.cPips = cPip
        case None =>
          val pm: Move = pml.amMoves(pml.cMoves)
          pm.anMove.copyFrom(anMoves)
          pm.auch = auch

          pm.cMoves = cMoves
          pm.cPips = cPip
          pm.backChequer = backChequerIndex(Board.SELF)

          pm.arEvalMove = new Reward()
          pml.cMoves += 1
      }

      require(pml.cMoves < MoveList.MAX_INCOMPLETE_MOVES)
    }
  }

  def locateMove(anMove: ChequersMove, pml: MoveList): Int = {
    val key: AuchKey = calcMoveKey(anMove)
    Math.max(pml.amMoves.take(pml.cMoves).indexWhere(_ => calcMoveKey(anMove) == key), 0)
  }

  private def calcMoveKey(anMove: ChequersMove): AuchKey = {
    val anBoardMove: Board = clone()
    anBoardMove.applyMove(anMove)
    anBoardMove.calcPositionKey
  }

  private def checkSamePoint: Boolean = {
    !(0 until 24)
      .exists(i => anBoard(Board.OPPONENT)(i) != 0 && anBoard(Board.SELF)(23 - i) != 0)
  }

  private def checkPosition: Boolean = {
    val (opponent, self) = chequersCount
    if (opponent > 15 || self > 15) {
      // Check for a player with over 15 chequers
      false
    } else if (!checkSamePoint) {
      // Check for both players having chequers on the same point
      false
    } else {
      // Check for both players on the bar against closed boards
      if ((0 until 6).exists(i => anBoard(Board.OPPONENT)(i) < 2 || anBoard(Board.SELF)(i) < 2)) {
        true
      } else {
        anBoard(Board.OPPONENT)(Board.BAR) == 0 || anBoard(Board.SELF)(Board.BAR) == 0
      }
    }
  }

  override def equals(that: Any): Boolean = {
    that match {
      case that: Board => anBoard.deep == that.anBoard.deep
      case _ => false
    }
  }

  override def hashCode: Int = {
    31 + anBoard.deep.hashCode()
  }
}