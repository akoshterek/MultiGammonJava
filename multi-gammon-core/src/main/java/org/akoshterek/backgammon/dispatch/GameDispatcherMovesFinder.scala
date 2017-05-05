package org.akoshterek.backgammon.dispatch

import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.matchstate.MatchState
import org.akoshterek.backgammon.move.{FindData, Move, MoveGenerator, MoveList}

/**
  * @author oleksii.koshterek
  *         On: 22.05.16
  */
class GameDispatcherMovesFinder(agents: Array[AgentEntry]) {

  def findMove(currentMatch: MatchState, pfd: FindData, amMoves: Array[Move]) {
    findAndSaveBestMoves(currentMatch, pfd.ml, amMoves, currentMatch.anDice, pfd.board)
  }

  private def findAndSaveBestMoves(currentMatch: MatchState, pml: MoveList, amMoves: Array[Move],
                                   dice: (Int, Int), anBoard: Board) {
    MoveGenerator.generateMoves(anBoard, pml, amMoves, dice)
    agents(currentMatch.fMove).agent.currentBoard = currentMatch.board
    if (pml.cMoves == 0) {
      pml.deleteMoves()
    } else {
      val pm: Array[Move] = pml.amMoves.take(pml.cMoves)
      pml.amMoves = pm
      scoreMoves(currentMatch, pml)
      scala.util.Sorting.stableSort(pml.amMoves, Move.gt _)
    }
  }

  private def scoreMoves(currentMatch: MatchState, pml: MoveList) {
    val agent: Agent = agents(currentMatch.fMove).agent
    agent.scoreMoves(pml.amMoves)
  }
}
