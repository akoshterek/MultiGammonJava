package org.akoshterek.backgammon.move

import org.akoshterek.backgammon.board.Board

/**
  * @author Alex
  *         date 29.07.2015.
  */
object MoveGenerator {
    def generateMoves(board: Board, pml: MoveList, amMoves: Array[Move], n0: Int, n1: Int, fPartial: Boolean): Int = {
        val anRoll: Array[Int] = new Array[Int](4)
        val anMoves: ChequersMove = new ChequersMove

        anRoll(0) = n0
        anRoll(1) = n1
        anRoll(2) = if (n0 == n1) n0 else 0
        anRoll(3) = anRoll(2)

        pml.cMoves = 0
        pml.cMaxMoves = 0
        pml.cMaxPips = 0
        pml.iMoveBest = 0
        pml.amMoves = amMoves
        generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves, fPartial)

        if (anRoll(0) != anRoll(1)) {
            val tmp: Int = anRoll(1)
            anRoll(1) = anRoll(0)
            anRoll(1) = tmp
            generateMovesSub(board, pml, anRoll, 0, 23, 0, anMoves, fPartial)
        }

        pml.cMoves
    }

    private[move] def generateMovesSub(board: Board, pml: MoveList, anRoll: Array[Int], nMoveDepth: Int,
                                       iPip: Int, cPip: Int, anMoves: ChequersMove, fPartial: Boolean): Boolean = {
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

                val anBoardNew: Board = new Board(board)
                anBoardNew.applySubMove(24, anRoll(nMoveDepth), fCheckLegal = true)

                if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1, 23, cPip + anRoll(nMoveDepth), anMoves, fPartial)) {
                    anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip + anRoll(nMoveDepth), anMoves, fPartial)
                }

                fPartial
            }
        }
        else {
            for (i <- iPip to 0 by -1) {
                if (anBoard(Board.SELF)(i) != 0 && board.isLegalMove(i, anRoll(nMoveDepth))) {
                    anMoves.move(nMoveDepth).from = i
                    anMoves.move(nMoveDepth).to = i - anRoll(nMoveDepth)

                    val anBoardNew: Board = new Board(board)
                    anBoardNew.applySubMove(i, anRoll(nMoveDepth), fCheckLegal = true)

                    if (generateMovesSub(anBoardNew, pml, anRoll, nMoveDepth + 1,
                        if (anRoll(0) == anRoll(1)) i else 23,
                        cPip + anRoll(nMoveDepth), anMoves, fPartial)) {
                        anBoardNew.saveMoves(pml, nMoveDepth + 1, cPip + anRoll(nMoveDepth), anMoves, fPartial)
                    }
                    fUsed = true
                }
            }

            !fUsed || fPartial
        }
    }
}