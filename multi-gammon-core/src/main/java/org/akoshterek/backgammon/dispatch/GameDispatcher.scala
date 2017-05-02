package org.akoshterek.backgammon.dispatch

import java.util

import scala.util.control.Breaks._
import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.matchstate.{GameResult, GameState, MatchMove, MatchState}
import org.akoshterek.backgammon.move._

class GameDispatcher(val agent1: Agent, val agent2: Agent) {
  private val agents: Array[AgentEntry] = Array[AgentEntry](
    new AgentEntry(agent1),
    new AgentEntry(agent2)
  )

  private val movesHelper: GameDispatcherMovesFinder = new GameDispatcherMovesFinder(agents)

  var showLog: Boolean = false
  private var numGames: Int = 0
  private var playedGames: Int = 0
  private var currentMatch: MatchState = _
  private val lMatch: util.Deque[MatchMove] = new util.LinkedList[MatchMove]
  private var pmrHint: MoveRecord = _

  def playGames(games: Int, learn: Boolean) {
    agents(0).agent.isLearnMode = learn
    agents(1).agent.isLearnMode = learn
    for (i <- numGames + 1 until numGames + games + 1) {
      playGame()
      if (i % 100 == 0) {
        System.out.print("%d ".format(i))
      }
    }
    numGames += games
    if (learn) {
      agents(0).agent.save()
    }
  }

  def currentAgent: Agent = {
    agents(if (currentMatch.fMove == 1) 0 else 1).agent
  }

  def playGame() {
    agents.foreach(a => a.agent.startGame())
    startGame()

    do {
      nextTurn()
    } while (currentMatch.gs == GameState.GAME_PLAYING)

    agents.foreach(a => a.agent.endGame())

    for (i <- 0 until 2) {
      if (currentMatch.anScore(i) != 0) {
        agents(i).wonGames += 1
      }
      agents(i).wonPoints += currentMatch.anScore(i)
    }
  }

  def printStatistics(): Unit = GameInfoPrinter.printStatistics(agents, numGames)

  private def startGame() {
    currentMatch = new MatchState
    currentMatch.board.initBoard()

    lMatch.clear()
    lMatch.add(new MatchMove)

    {
      val pmr: MoveRecord = new MoveRecord
      pmr.mt = MoveType.MOVE_GAMEINFO
      pmr.g.anScore(0) = currentMatch.anScore(0)
      pmr.g.anScore(1) = currentMatch.anScore(1)
      pmr.g.fWinner = -1
      pmr.g.nPoints = 0
      addMoveRecord(pmr)
    }

    do {
      currentMatch.rollDice()
      if (showLog) {
        GameInfoPrinter.printRoll(agents, currentMatch.anDice)
      }
    } while (currentMatch.anDice._1 == currentMatch.anDice._2)

    {
      val pmr: MoveRecord = new MoveRecord
      pmr.mt = MoveType.MOVE_SETDICE
      pmr.anDice = currentMatch.anDice.copy()
      pmr.fPlayer = if (currentMatch.anDice._1 > currentMatch.anDice._2) 1 else 0
      addMoveRecord(pmr)
    }
    diceRolled()
  }

  private def diceRolled() {
    if (showLog) {
      GameInfoPrinter.printBoard(agents, currentMatch, lMatch)
    }
  }

  private def addMoveRecord(pmr: MoveRecord) {
    var pmrOld: MoveRecord = null
    addMoveRecordGetCur(pmr)
    addMoverecordSanityCheck(pmr)
    val hasMoves: Boolean = !lMatch.isEmpty && lMatch.getLast.moveRecords.nonEmpty

    if (hasMoves) {
      pmrOld = lMatch.getLast.moveRecords.last
    }

    if (hasMoves && pmr.mt == MoveType.MOVE_NORMAL && pmrOld.mt == MoveType.MOVE_SETDICE && pmrOld.fPlayer == pmr.fPlayer) {
      lMatch.getLast.moveRecords.dropRight(1)
    }

    fixMatchState(pmr)
    lMatch.getLast.moveRecords :+ pmr
    applyMoveRecord(pmr)
  }

  private def applyMoveRecord(pmr: MoveRecord) {
    val lGame = lMatch.getLast.moveRecords

    val pmrx: MoveRecord = lGame.head
    //this is wrong -- plGame is not necessarily the right game

    require(pmr.mt == MoveType.MOVE_GAMEINFO || pmrx.mt == MoveType.MOVE_GAMEINFO)
    val pmgi = pmrx.g
    currentMatch.gs = GameState.GAME_PLAYING

    pmr.mt match {
      case MoveType.MOVE_GAMEINFO =>
        currentMatch.board.initBoard()
        currentMatch.anScore(0) = pmr.g.anScore(0)
        currentMatch.anScore(1) = pmr.g.anScore(1)
        currentMatch.gs = GameState.GAME_NONE
        currentMatch.fMove = -1
        currentMatch.fTurn = -1
        currentMatch.anDice = (0, 0)

      case MoveType.MOVE_NORMAL =>
        playMove(pmr.n.anMove, pmr.fPlayer)
        currentMatch.anDice = (0, 0)
        val n = currentMatch.board.gameResult
        if (n != GameResult.PLAYING) {
          currentMatch.gs = GameState.GAME_OVER
          pmgi.nPoints = n.value
          pmgi.fWinner = pmr.fPlayer
          applyGameOver()
        }

      case MoveType.MOVE_SETBOARD =>
        currentMatch.board = Board.positionFromKey(pmr.sb.auchKey)
        if (currentMatch.fMove < 0) {
          currentMatch.fTurn = 0
          currentMatch.fMove = 0
        }

        if (currentMatch.fMove != 0) {
          currentMatch.board.swapSides()
        }

      case MoveType.MOVE_SETDICE =>
        currentMatch.anDice = pmr.anDice.copy()
        if (currentMatch.fMove != pmr.fPlayer) currentMatch.board.swapSides()
        currentMatch.fTurn = pmr.fPlayer
        currentMatch.fMove = pmr.fPlayer

      case _ =>
    }
  }

  private def playMove(anMove: ChequersMove, fPlayer: Int) {
    if (currentMatch.fMove != -1 && fPlayer != currentMatch.fMove) {
      currentMatch.board.swapSides()
    }

    breakable {
      for (i <- 0 until 4) {
        val nSrc: Int = anMove.move(i).from
        val nDest: Int = anMove.move(i).to
        if (nSrc < 0) {
          break
        }

        val board = currentMatch.board.anBoard
        if (board(1)(nSrc) != 0) {
          // source point is not empty
          board(1)(nSrc) = (board(1)(nSrc) - 1).asInstanceOf[Byte]
          if (nDest >= 0) {
            board(1)(nDest) = (board(1)(nDest) + 1).asInstanceOf[Byte]
          }

          if (nDest >= 0 && nDest <= 23) {
            board(0)(24) = (board(0)(24) + board(0)(23 - nDest)).asInstanceOf[Byte]
            board(0)(23 - nDest) = 0.asInstanceOf[Byte]
          }
        }
      }
    }

    currentMatch.fMove = if (fPlayer != 0) 0 else 1
    currentMatch.fTurn = currentMatch.fMove
    currentMatch.board.swapSides()
  }

  private def applyGameOver() {
    val pmr: MoveRecord = lMatch.getLast.moveRecords.head
    val pmgi: XMoveGameInfo = pmr.g

    require(pmr.mt == MoveType.MOVE_GAMEINFO)

    if (pmgi.fWinner >= 0) {
      val n = currentMatch.board.gameResult
      currentMatch.anScore(pmgi.fWinner) += pmgi.nPoints
      playedGames += 1
      if (showLog) {
        GameInfoPrinter.printGameOver(agents, pmgi.fWinner, pmgi.nPoints, n)
      }
    }
  }

  private def addMoverecordSanityCheck(pmr: MoveRecord) {
    require(pmr.fPlayer >= 0 && pmr.fPlayer <= 1)
    require(pmr.ml.cMoves < MoveList.MAX_MOVES)

    pmr.mt match {
      case MoveType.MOVE_GAMEINFO =>
      case MoveType.MOVE_NORMAL =>
        if (pmr.ml.cMoves != 0) {
          require(pmr.n.iMove <= pmr.ml.cMoves)
        }
      case MoveType.MOVE_SETDICE =>
      case MoveType.MOVE_SETBOARD =>
      case _ =>
        throw new IllegalArgumentException
    }
  }

  private def copyFromPmrCur(pmr: MoveRecord, get_move: Boolean) {
    getCurrentMoveRecord match {
      case null =>
      case pmr_cur: MoveRecord =>
        if (get_move && pmr_cur.ml.cMoves > 0) {
        pmr.ml = new MoveList(pmr_cur.ml)
        pmr.n.iMove = currentMatch.board.locateMove(pmr.n.anMove, pmr.ml)
      }
    }
  }

  private def addMoveRecordGetCur(pmr: MoveRecord) {
    pmr.mt match {
      case MoveType.MOVE_NORMAL =>
        copyFromPmrCur(pmr, get_move = true)
        pmrHint = null
      case MoveType.MOVE_SETDICE =>
        copyFromPmrCur(pmr, get_move = false)
      case _ =>
        pmrHint = null
    }
  }

  private def getCurrentMoveRecord: MoveRecord = {
    val hasMoves: Boolean = !lMatch.isEmpty && lMatch.getLast.moveRecords.nonEmpty
    if (hasMoves) {
      lMatch.getLast.moveRecords.last
    } else if (currentMatch.gs ne GameState.GAME_PLAYING) {
      pmrHint = null
      pmrHint
    } else {
      // invalidate on changed dice
      if (currentMatch.anDice._1 > 0 && pmrHint != null && pmrHint.anDice._1 > 0
        && (pmrHint.anDice._1 != currentMatch.anDice._1 || pmrHint.anDice._2 != currentMatch.anDice._2)) {
        pmrHint = null
      }
      if (pmrHint == null) {
        pmrHint = new MoveRecord
        pmrHint.fPlayer = currentMatch.fTurn
      }
      if (currentMatch.anDice._1 > 0) {
        pmrHint.mt = MoveType.MOVE_NORMAL
        pmrHint.anDice = currentMatch.anDice.copy()
      }

      pmrHint
    }
  }

  private def fixMatchState(pmr: MoveRecord) {
    pmr.mt match {
      case MoveType.MOVE_NORMAL =>
        if (currentMatch.fTurn != pmr.fPlayer) {
          currentMatch.board.swapSides()
          currentMatch.fMove = pmr.fPlayer
          currentMatch.fTurn = pmr.fPlayer
        }

      case _ =>
    }
  }

  private def nextTurn() {
    val ms: MatchState = currentMatch
    if (ms.board.gameResult != GameResult.PLAYING) {
      val pmr: MoveRecord = lMatch.getLast.moveRecords.last
      val pmgi: XMoveGameInfo = pmr.g
      if (showLog) {
        GameInfoPrinter.printWin(agents, currentMatch, pmgi.fWinner, pmgi.nPoints)
        GameInfoPrinter.printScore(agents, ms, playedGames)
      }
    }
    require(currentMatch.gs == GameState.GAME_PLAYING)
    computerTurn()
  }

  private def computerTurn() {
    val ms: MatchState = currentMatch
    if (ms.gs == GameState.GAME_PLAYING) {

      //Don't use the global board for this call, to avoid
      //race conditions with updating the board and aborting the
      //move with an interrupt.
      val anBoardMove: Board = ms.board.clone()

      // Roll dice and move
      if (ms.anDice._1 == 0) {
        ms.rollDice()
        diceRolled()
      }

      val pmr: MoveRecord = new MoveRecord
      pmr.mt = MoveType.MOVE_NORMAL
      ms.anDice = pmr.anDice.copy()
      pmr.fPlayer = ms.fTurn

      val fd: FindData = new FindData(pmr.ml, anBoardMove)
      movesHelper.findMove(currentMatch, fd)

      // make the move found above
      if (pmr.ml.cMoves != 0) {
        pmr.n.anMove = pmr.ml.amMoves(0).anMove
        pmr.n.iMove = 0
        agents(currentMatch.fMove).agent.doMove(pmr.ml.amMoves(0))
        if (pmr.ml.amMoves(0).pc eq PositionClass.CLASS_OVER) {
          //update the lost agent
          forceMove(pmr.ml.amMoves(0))
        }
      }
      //else {
      //            // no moves possible agent is blocked.
      //            // Update the blocked agent with current position for learning purpose
      //            Board board = new Board(ms.board);
      //            Move move = new Move();
      //            move.auch = board.calcPositionKey();
      //            move.pc = Evaluator.getInstance().classifyPosition(board);
      //            move.arEvalMove = agents[currentMatch.fMove == 1 ? 0 : 1].agent.evaluatePosition(board, move.pc);
      //            //forceMove(move);
      //        }
      pmr.ml.deleteMoves()

      // write move to status bar or stdout
      if (showLog) {
        GameInfoPrinter.showAutoMove(pmr.n.anMove, agents, currentMatch)
      }
      addMoveRecord(pmr)
    }
  }

  private def forceMove(endMove: Move) {
    val board: Board = Board.positionFromKey(endMove.auch)
    board.swapSides()
    endMove.auch = board.calcPositionKey
    endMove.arEvalMove = endMove.arEvalMove.invert
    currentAgent.doMove(endMove)
  }
}