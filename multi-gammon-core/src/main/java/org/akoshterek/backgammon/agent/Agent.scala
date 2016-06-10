package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import java.nio.file.Path

trait Agent {
    protected val _fullName: String
    def fullName = _fullName

    protected val _path: Path
    def path = _path

    var isLearnMode: Boolean = false
    protected var supportsSanityCheck: Boolean = false

    private var _playedGames: Int = 0
    def playedGames = _playedGames

    protected var _fixed: Boolean = true
    def isFixed = _fixed

    protected var needsInvertedEval: Boolean = false
    protected var _supportsBearoff: Boolean = false
    def supportsBearoff = _supportsBearoff

    protected var _currentBoard: Board = null
    protected var curPC: PositionClass = PositionClass.CLASS_OVER

    def startGame() {}

    def endGame(): Unit = {
        if (isLearnMode) {
            _playedGames += 1
        }
    }

    def doMove(move: Move) {}

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

    def currentBoard = _currentBoard

    def currentBoard_(board: Board) {
        _currentBoard = new Board(board)
        curPC = Evaluator.getInstance.classifyPosition(currentBoard)
    }

    def load() {
    }

    def save() {
    }
}