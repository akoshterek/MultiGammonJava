package org.akoshterek.backgammon.dispatch;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.BoardFormatter;
import org.akoshterek.backgammon.match.MatchMove;
import org.akoshterek.backgammon.match.MatchState;
import org.akoshterek.backgammon.move.ChequerMove;
import org.akoshterek.backgammon.move.MoveRecord;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Deque;

/**
 * @author Alex
 *         date 05.08.2015.
 */
public class GameInfoPrinter {
    private static final char[] signs = new char[]{'O', 'X'};
    private static final String[] gameResult = new String[]{"single game", "gammon", "backgammon"};


    public static void printStatistics(GameDispatcher.AgentEntry[] agents, int numGames) {
        System.out.println(String.format("\tStatistics after %d game(s)", numGames));

        for (int i = 0; i < 2; i++) {
            System.out.println(String.format("%c:%s: games %d/%d = %5.2f%%, points %d = %5.2f%%",
                    signs[i], agents[i].agent.getFullName(),
                    agents[i].wonGames, numGames,
                    (float) agents[i].wonGames / numGames * 100, agents[i].wonPoints,
                    (float) agents[i].wonPoints / (agents[0].wonPoints + agents[1].wonPoints) * 100));
        }

        float pointDiff = agents[0].wonPoints - agents[1].wonPoints;
        System.out.println(String.format("%c:%s: won %+5.3f ppg\n", signs[0], agents[0].agent.getFullName(), pointDiff / numGames));

        Path logPath = Paths.get(agents[0].agent.getPath().toString(), getLogFileName(agents));
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(logPath, StandardOpenOption.APPEND))) {
            writer.println(String.format("%d;%f\n", agents[0].agent.getPlayedGames(), pointDiff / numGames));
        } catch (IOException ignored) {
        }
    }

    public static void printRoll(GameDispatcher.AgentEntry[] agents, int[] dice) {
        System.out.println(String.format("%s rolls %d, %s rolls %d.",
                agents[0].agent.getFullName(), dice[0],
                agents[1].agent.getFullName(), dice[1]));
    }

    public static void printBoard(GameDispatcher.AgentEntry[] agents, MatchState match, Deque<MatchMove> matchMoves) {
        String[] apch = new String[7];

        Board an = new Board(match.board);
        if (match.fMove == 0) {
            an.swapSides();
        }

        apch[0] = "O: " + agents[0].agent.getFullName();
        apch[6] = "X: " + agents[1].agent.getFullName();
        //apch[1] = String.format("%d point(s)", match.anScore[0]);
        //apch[5] = String.format("%d point(s)", match.anScore[1]);
        apch[match.fMove != 0 ? 4 : 2] = "";

        if (match.anDice[0] != 0) {
            System.out.println(String.format("%s %s %d%d", match.fTurn == 0 ? apch[0] : apch[6], "rolled",
                    match.anDice[0], match.anDice[1]));
        } else {
            System.out.println(match.board.gameStatus() != 0 ? "On roll" : "");
        }

        System.out.println(BoardFormatter.drawBoard(an, match.fMove, apch));

        if (!matchMoves.isEmpty() && !matchMoves.getLast().moveRecords.isEmpty()) {
            MoveRecord pmr = matchMoves.getLast().moveRecords.getLast();
            if (pmr.sz != null && !pmr.sz.isEmpty()) {
                System.out.println(pmr.sz);
            }
        }
    }

    public static void printWin(GameDispatcher.AgentEntry[] agents, MatchState match, int fWinner, int nPoints) {
        int n = match.board.gameStatus();
        System.out.println(String.format("%s wins a %s and %d point(s).\n",
                agents[fWinner].agent.getFullName(),
                gameResult[n - 1], nPoints));
    }

    public static void printScore(GameDispatcher.AgentEntry[] agents, MatchState match, int playedGames) {
        String str = String.format((playedGames == 1
                        ? "The score (after %d game) is: %s %d, %s %d"
                        : "The score (after %d games) is: %s %d, %s %d"),
                playedGames,
                agents[0].agent.getFullName(), match.anScore[0],
                agents[1].agent.getFullName(), match.anScore[1]);
        System.out.println(str);
    }

    public static void showAutoMove(ChequerMove anMove, GameDispatcher.AgentEntry[] agents, MatchState match) {
        char symbol = match.fTurn != 0? 'X' : 'O';
        if( anMove.move[ 0 ] == -1 ) {
            System.out.println(String.format("%c:%s cannot move.\n", symbol, agents[match.fTurn].agent.getFullName()));
        } else {
            System.out.println(String.format("%c:%s moves %s.\n", symbol, agents[match.fTurn].agent.getFullName(),
                    BoardFormatter.formatMovePlain(anMove, match.board)));
        }
    }

    public static void printGameOver(GameDispatcher.AgentEntry[] agents, int fWinner, int nPoints, int result) {
        System.out.println(String.format("Game over.\n%c:%s wins a %s and %d point(s)\n",
                fWinner != 0 ? 'X' : 'O',
                agents[fWinner].agent.getFullName(),
                gameResult[ result - 1 ], nPoints));
    }

    private static String getLogFileName(GameDispatcher.AgentEntry[] agents) {
        return agents[0].agent.getFullName() + " vs " + agents[1].agent.getFullName() + ".csv";
    }
}
