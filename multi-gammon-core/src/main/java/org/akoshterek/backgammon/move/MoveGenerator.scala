package org.akoshterek.backgammon.move

import org.akoshterek.backgammon.board.Board

/**
  * @author Alex
  *         date 29.07.2015.
  */
object MoveGenerator {
  def generateMoves(board: Board, pml: MoveList, amMoves: Array[Move], dice: (Int, Int)): Int = {
    val anRoll: Array[Int] = makeAnRoll(dice)
    val anMoves: ChequersMove = new ChequersMove

    pml.cMoves = 0
    pml.cMaxMoves = 0
    pml.cMaxPips = 0
    pml.amMoves = amMoves
    generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves)

    if (anRoll(0) != anRoll(1)) {
      val tmp: Int = anRoll(1)
      anRoll(1) = anRoll(0)
      anRoll(1) = tmp
      generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves)
    }

    pml.cMoves
  }

  private def makeAnRoll(dice: (Int, Int)): Array[Int] = Array[Int](
    dice._1,
    dice._2,
    if (dice._1 == dice._2) dice._1 else 0,
    if (dice._1 == dice._2) dice._1 else 0
  )

  private[move] def generateMovesSub(board: Board, pml: MoveList, anRoll: Array[Int], nMoveDepth: Int,
                                     iPip: Int, cPip: Int, anMoves: ChequersMove): Boolean = {
    var fUsed: Boolean = false
    val anBoard: Array[Array[Int]] = board.anBoard

    if (nMoveDepth > 3 || anRoll(nMoveDepth) == 0) {
      true
    } else if (anBoard(Board.SELF)(Board.BAR) != 0) {
      if (anBoard(Board.OPPONENT)(anRoll(nMoveDepth) - 1) >= 2) {
        true
      } else {
        anMoves.move(nMoveDepth).from = 24
        anMoves.move(nMoveDepth).to = 24 - anRoll(nMoveDepth)

        val anBoardNew: Board = board.clone()
        anBoardNew.applySubMove(24, anRoll(nMoveDepth), fCheckLegal = true)

        if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1, 23, cPip + anRoll(nMoveDepth), anMoves)) {
          anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip + anRoll(nMoveDepth), anMoves)
        }

        false
      }
    }
    else {
      var i = iPip
      while (i >= 0) {
        if (anBoard(Board.SELF)(i) != 0 && board.isLegalMove(i, anRoll(nMoveDepth))) {
          anMoves.move(nMoveDepth).from = i
          anMoves.move(nMoveDepth).to = i - anRoll(nMoveDepth)

          val anBoardNew: Board = board.clone()
          anBoardNew.applySubMove(i, anRoll(nMoveDepth), fCheckLegal = true)

          if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1,
            if (anRoll(0) == anRoll(1)) i else 23,
            cPip + anRoll(nMoveDepth), anMoves)) {
            anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip + anRoll(nMoveDepth), anMoves)
          }
          fUsed = true
        }

        i -= 1
      }

      !fUsed
    }
  }
}