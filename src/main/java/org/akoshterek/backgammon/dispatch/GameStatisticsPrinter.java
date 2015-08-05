package org.akoshterek.backgammon.dispatch;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * @author Alex
 *         date 05.08.2015.
 */
public class GameStatisticsPrinter {
    private static final char[] signs = new char[] {'O', 'X'};

    public static void printStatistics(GameDispatcher.AgentEntry[] agents, int numGames) {
        System.out.println(String.format("\n\tStatistics after %d game(s)", numGames));

        for(int i = 0; i < 2; i++) {
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
        } catch (IOException ignored) {}
    }

    private static String getLogFileName(GameDispatcher.AgentEntry[] agents) {
        return agents[0].agent.getFullName() + " vs " + agents[1].agent.getFullName() + ".csv";
    }
}
