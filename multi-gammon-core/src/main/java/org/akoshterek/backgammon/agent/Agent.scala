package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import java.nio.file.Path

trait Agent {
    def getFullName: String

    def getPath: Path

    def getPlayedGames: Int

    def startGame()

    def endGame()

    def doMove(move: Move)

    def evaluatePosition(board: Board, pc: PositionClass): Reward = {
        val effectivePc: PositionClass = if (PositionClass.isBearoff(pc) && !supportsBearoff) PositionClass.CLASS_RACE else pc
        effectivePc match {
            case PositionClass.CLASS_OVER =>
                evalOver(board)
            case PositionClass.CLASS_RACE =>
                evalRace(board)
            case PositionClass.CLASS_CRASHED =>
                evalCrashed(board)
            case PositionClass.CLASS_CONTACT =>
                evalContact(board)
            case PositionClass.CLASS_BEAROFF1 =>
                Evaluator.getInstance.evalBearoff1(board)
            case PositionClass.CLASS_BEAROFF2 =>
                Evaluator.getInstance.evalBearoff2(board)
            case _ =>
                throw new RuntimeException("Unknown class. How did we get here?")
        }
    }

    def scoreMoves(moves: Array[Move], count: Int)

    def scoreMove(pm: Move): Reward

    def evalOver(board: Board): Reward = {
        Evaluator.getInstance.evalOver(board)
    }

    def evalRace(board: Board): Reward = {
        evalContact(board)
    }

    def evalCrashed(board: Board): Reward = {
        evalContact(board)
    }

    def evalContact(board: Board): Reward

    def isLearnMode: Boolean

    def setLearnMode(learn: Boolean)

    //fixed means it's unable to learn
    def isFixed: Boolean

    def needsInvertedEval: Boolean

    def setNeedsInvertedEval(needsInvertedEval: Boolean)

    def supportsSanityCheck: Boolean

    def setSanityCheck(sc: Boolean)

    def supportsBearoff: Boolean

    def setCurrentBoard(board: Board)

    def load() {
    }

    def save() {
    }
}