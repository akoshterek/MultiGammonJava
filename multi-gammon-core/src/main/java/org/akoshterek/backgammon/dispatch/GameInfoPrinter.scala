package org.akoshterek.backgammon.dispatch

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.BoardFormatter
import org.akoshterek.backgammon.matchstate.MatchMove
import org.akoshterek.backgammon.matchstate.MatchState
import org.akoshterek.backgammon.move.ChequersMove
import org.akoshterek.backgammon.move.MoveRecord
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util

import resource.managed

object GameInfoPrinter {
  private val signs: Array[Char] = Array[Char]('O', 'X')
  private val gameResult: Array[String] = Array[String]("single game", "gammon", "backgammon")

  def printStatistics(agents: Array[AgentEntry], numGames: Int) {
    System.out.println("\tStatistics after %d game(s)".format(numGames))

    for (i <- agents.indices) {
      System.out.println("%c:%s: games %d/%d = %5.2f%%, points %d = %5.2f%%".format(
        signs(i), agents(i).agent.fullName,
        agents(i).wonGames, numGames,
        agents(i).wonGames.asInstanceOf[Float] / numGames * 100, agents(i).wonPoints,
        agents(i).wonPoints.asInstanceOf[Float] / (agents(0).wonPoints + agents(1).wonPoints) * 100))
    }

    val pointDiff: Float = agents(0).wonPoints - agents(1).wonPoints
    System.out.println("%c:%s: won %+5.3f ppg\n".format(signs(0), agents(0).agent.fullName, pointDiff / numGames))

    val logPath: Path = Paths.get(agents(0).agent.path.toString, "bin", getLogFileName(agents))
    managed(new PrintWriter(Files.newBufferedWriter(logPath, StandardOpenOption.APPEND, StandardOpenOption.CREATE))).acquireAndGet(writer => {
      writer.println("%d;%f".format(agents(0).agent.playedGames, pointDiff / numGames))
    })
  }

  def printRoll(agents: Array[AgentEntry], dice: Array[Int]) {
    System.out.println("%s rolls %d, %s rolls %d.".format(agents(0).agent.fullName, dice(0), agents(1).agent.fullName, dice(1)))
  }

  def printBoard(agents: Array[AgentEntry], matchState: MatchState, matchMoves: util.Deque[MatchMove]) {
    val apch: Array[String] = new Array[String](7)

    val an: Board = new Board(matchState.board)
    if (matchState.fMove == 0) {
      an.swapSides()
    }

    apch(0) = "O: " + agents(0).agent.fullName
    apch(6) = "X: " + agents(1).agent.fullName
    //apch[1] = String.format("%d point(s)", match.anScore[0]);
    //apch[5] = String.format("%d point(s)", match.anScore[1]);
    apch(if (matchState.fMove != 0) 4 else 2) = ""

    if (matchState.anDice(0) != 0) {
      val agentName = if (matchState.fTurn == 0) apch(0) else apch(6)
      System.out.println("%s rolled %d %d".format(agentName, matchState.anDice(0), matchState.anDice(1)))
    }
    else {
      System.out.println(if (matchState.board.gameStatus != 0) "On roll" else "")
    }

    System.out.println(BoardFormatter.drawBoard(an, matchState.fMove, apch))

    if (!matchMoves.isEmpty && !matchMoves.getLast.moveRecords.isEmpty) {
      val pmr: MoveRecord = matchMoves.getLast.moveRecords.getLast
      if (pmr.sz != null && !pmr.sz.isEmpty) {
        System.out.println(pmr.sz)
      }
    }
  }

  def printWin(agents: Array[AgentEntry], matchState: MatchState, fWinner: Int, nPoints: Int) {
    val n: Int = matchState.board.gameStatus
    System.out.println("%s wins a %s and %d point(s).\n".format(
      agents(fWinner).agent.fullName,
      gameResult(n - 1), nPoints))
  }

  def printScore(agents: Array[AgentEntry], matchState: MatchState, playedGames: Int) {
    val template: String = if (playedGames == 1) "The score (after %d game) is: %s %d, %s %d"
    else "The score (after %d games) is: %s %d, %s %d"
    val str: String = template.format(
      playedGames, agents(0).agent.fullName, matchState.anScore(0),
      agents(1).agent.fullName, matchState.anScore(1))
    System.out.println(str)
  }

  def showAutoMove(anMove: ChequersMove, agents: Array[AgentEntry], matchState: MatchState) {
    val symbol: Char = if (matchState.fTurn != 0) 'X'
    else 'O'
    if (anMove.move(0).from == -1) {
      System.out.println("%c:%s cannot move.\n".format(
        symbol, agents(matchState.fTurn).agent.fullName))
    }
    else {
      System.out.println("%c:%s moves %s.\n".format(
        symbol, agents(matchState.fTurn).agent.fullName,
        BoardFormatter.formatMovePlain(anMove, matchState.board)))
    }
  }

  def printGameOver(agents: Array[AgentEntry], fWinner: Int, nPoints: Int, result: Int) {
    val sign: Char = if (fWinner != 0) 'X' else 'O'
    System.out.println("Game over.\n%c:%s wins a %s and %d point(s)\n".format(
      sign, agents(fWinner).agent.fullName, gameResult(result - 1), nPoints))
  }

  private def getLogFileName(agents: Array[AgentEntry]): String = {
    agents(0).agent.fullName + " vs " + agents(1).agent.fullName + ".csv"
  }
}