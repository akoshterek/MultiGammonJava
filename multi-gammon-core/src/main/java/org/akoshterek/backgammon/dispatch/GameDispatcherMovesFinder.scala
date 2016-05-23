package org.akoshterek.backgammon.dispatch

import java.util

import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.move.{FindData, Move, MoveGenerator, MoveList}
import org.akoshterek.backgammon.matchstate.MatchState
/**
  * @author oleksii.koshterek
  *         On: 22.05.16
  */
class GameDispatcherMovesFinder(agents: Array[AgentEntry]) {

    def findMove(currentMatch: MatchState, pfd: FindData, amMoves: Array[Move]) {
        findAndSaveBestMoves(currentMatch, pfd.ml, amMoves, currentMatch.anDice(0), currentMatch.anDice(1), pfd.board)
    }

    private def findAndSaveBestMoves(currentMatch: MatchState, pml: MoveList, amMoves: Array[Move],
                                     nDice0: Int, nDice1: Int, anBoard: Board) {
        MoveGenerator.generateMoves(anBoard, pml, amMoves, nDice0, nDice1, false)
        agents(currentMatch.fMove).agent.setCurrentBoard(currentMatch.board)
        if (pml.cMoves == 0) {
            pml.amMoves = null
            return
        }
        val pm: Array[Move] = new Array[Move](pml.cMoves)
        System.arraycopy(pml.amMoves, 0, pm, 0, pml.cMoves)
        pml.amMoves = pm
        scoreMoves(currentMatch, pml)
        util.Arrays.sort(pml.amMoves, 0, pml.cMoves, Move.moveComparator)
        pml.iMoveBest = 0
    }

    private def findBestMove(currentMatch: MatchState, pml: MoveList) {
        pml.rBestScore = Integer.MIN_VALUE
        for (i <- 0 until pml.cMoves) {
            if (pml.amMoves(i).rScore > pml.rBestScore) {
                pml.iMoveBest = i
                pml.rBestScore = pml.amMoves(i).rScore
            }
        }
    }

    private def scoreMoves(currentMatch: MatchState, pml: MoveList) {
        val agent: Agent = agents(currentMatch.fMove).agent
        agent.scoreMoves(pml.amMoves, pml.cMoves)
        findBestMove(currentMatch, pml)
    }
}
