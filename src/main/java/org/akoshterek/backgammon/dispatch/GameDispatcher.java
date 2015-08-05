package org.akoshterek.backgammon.dispatch;

import org.akoshterek.backgammon.agent.IAgent;
import org.akoshterek.backgammon.match.MatchState;
import org.akoshterek.backgammon.move.Move;
import org.akoshterek.backgammon.move.MoveList;
import org.akoshterek.backgammon.move.MoveRecord;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class GameDispatcher {
    private static String[] gameResult = new String[] {"single game", "gammon", "backgammon"};
    private AgentEntry[] agents = new AgentEntry[2];
    private boolean isLearningMode = false;
    private boolean showLog = false;
    private int numGames = 0;

    private MatchState currentMatch;
    private List<List<MoveRecord>> lMatch = new LinkedList<>();
    //std::list<std::list<moverecord> > m_lMatch;
    private MoveRecord pmrHint;
    private Move[] amMoves = new Move[MoveList.MAX_INCOMPLETE_MOVES];
    private boolean autoCrawford = true;

    public GameDispatcher(IAgent agent1, IAgent agent2) {
        agents[0].agent = agent1;
        agents[1].agent = agent2;

    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setIsShowLog(boolean isShowLog) {
        this.showLog = isShowLog;
    }

    public void playGames(int games, boolean learn)
    {
        isLearningMode = learn;

        agents[0].agent.setLearnMode(learn);
        agents[1].agent.setLearnMode(learn);

        for(int i = numGames + 1; i < numGames + games + 1; i++)
        {
            playGame();
            if(i % 100 == 0) {
                System.out.println(String.format("%d ", i));
            }

            //ExportMatchMat("-", true);
        }

        numGames += games;
    }

    public void printStatistics() {
        GameStatisticsPrinter.printStatistics(agents, numGames);
    }

    class AgentEntry {
        public IAgent agent;
        public int wonGames = 0;
        public int wonPoints = 0;
    }
}
