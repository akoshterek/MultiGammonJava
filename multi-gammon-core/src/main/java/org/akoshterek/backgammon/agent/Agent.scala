package org.akoshterek.backgammon.agent

import java.nio.file.Path

import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.{Evaluator, Reward}
import org.akoshterek.backgammon.move.Move

trait Agent {
  protected val _fullName: String

  def fullName: String = _fullName

  protected val _path: Path

  def path: Path = _path

  var isLearnMode: Boolean = false
  val supportsSanityCheck: Boolean = false
  val needsInvertedEval: Boolean = false
  val supportsBearoff: Boolean = false

  private var _playedGames: Int = 0

  def playedGames: Int = _playedGames

  protected var fixed: Boolean = true

  protected var _currentBoard: Board = _
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
      case PositionClass.CLASS_OVER => evalOver(board)
      case PositionClass.CLASS_RACE => evalRace(board)
      case PositionClass.CLASS_CRASHED => evalCrashed(board)
      case PositionClass.CLASS_CONTACT => evalContact(board)
      case PositionClass.CLASS_BEAROFF1 => Evaluator.getInstance.evalBearoff1(board)
      case PositionClass.CLASS_BEAROFF2 => Evaluator.getInstance.evalBearoff2(board)
      case _ => throw new RuntimeException("Unknown class. How did we get here?")
    }
  }

  def scoreMoves(moves: Seq[Move]): Unit = moves.foreach(m => m.arEvalMove = scoreMove(m))

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

  def currentBoard: Board = _currentBoard

  def currentBoard_=(board: Board): Unit = {
    _currentBoard = board.clone()
    curPC = Evaluator.getInstance.classifyPosition(currentBoard)
  }

  def load() {
  }

  def save() {
  }
}