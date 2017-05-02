package org.akoshterek.backgammon.dispatch

import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.move.{FindData, Move, MoveGenerator, MoveList}
import org.akoshterek.backgammon.matchstate.MatchState

/**
  * @author Alex
  *         On: 22.05.16
  */
class GameDispatcherMovesFinder(agents: Array[AgentEntry]) {

  def findMove(currentMatch: MatchState, pfd: FindData) {
    findAndSaveBestMoves(currentMatch, pfd.ml, currentMatch.anDice, pfd.board)
  }

  private def findAndSaveBestMoves(currentMatch: MatchState, pml: MoveList,
                                   dice: (Int, Int), anBoard: Board) {
    MoveGenerator.generateMoves(anBoard, pml, dice)
    agents(currentMatch.fMove).agent.currentBoard = currentMatch.board
    if (pml.cMoves == 0) {
      pml.amMoves = null
    } else {
      scoreMoves(currentMatch, pml)
      scala.util.Sorting.stableSort(pml.amMoves, Move.gt _)
    }
  }

  private def scoreMoves(currentMatch: MatchState, pml: MoveList) {
    val agent: Agent = agents(currentMatch.fMove).agent
    agent.scoreMoves(pml.amMoves)
  }
}
