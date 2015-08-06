package org.akoshterek.backgammon.dispatch;

import org.akoshterek.backgammon.agent.IAgent;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.match.GameState;
import org.akoshterek.backgammon.match.MatchMove;
import org.akoshterek.backgammon.match.MatchState;
import org.akoshterek.backgammon.move.*;

import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class GameDispatcher {
    private AgentEntry[] agents = new AgentEntry[2];
    private boolean isLearningMode = false;
    private boolean showLog = false;
    private int numGames = 0;
    private int playedGames = 0;

    private MatchState currentMatch;
    private Deque<MatchMove> lMatch = new LinkedList<>();
    //std::list<std::list<moverecord> > m_lMatch;
    private MoveRecord pmrHint;
    private Move[] amMoves = new Move[MoveList.MAX_INCOMPLETE_MOVES];

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

    public void playGame() {
        for(int i = 0; i < 2; i++) {
            agents[i].agent.startGame();
        }

        startGame();
        //main game loop
        do {
            nextTurn();
        }
        while (currentMatch.gs == GameState.GAME_PLAYING);

        for(int i = 0; i < 2; i++) {
            agents[i].agent.endGame();
        }

        for(int i = 0; i < 2; i++) {
            if(currentMatch.anScore[i] != 0) {
                agents[i].wonGames++;
            }
            agents[i].wonPoints += currentMatch.anScore[i];
        }
    }

    public void printStatistics() {
        GameInfoPrinter.printStatistics(agents, numGames);
    }

    private void startGame() {
        currentMatch = new MatchState();
        currentMatch.board.initBoard();

        lMatch.clear();
        lMatch.add(new MatchMove());

        {
            MoveRecord pmr = new MoveRecord();
            pmr.mt = MoveType.MOVE_GAMEINFO;
            //pmr.sz = NULL;

            pmr.g.anScore[0] = currentMatch.anScore[0];
            pmr.g.anScore[1] = currentMatch.anScore[1];
            pmr.g.fWinner = -1;
            pmr.g.nPoints = 0;
            //pmr.g.sc = statcontext();
            addMoveRecord(pmr);
        }

        do {
            currentMatch.rollDice();
            if (isShowLog()) {
                GameInfoPrinter.printRoll(agents, currentMatch.anDice);
            }
        } while (currentMatch.anDice[1] == currentMatch.anDice[0]);

        {
            MoveRecord pmr = new MoveRecord();
            pmr.mt = MoveType.MOVE_SETDICE;

            pmr.anDice[0] = currentMatch.anDice[0];
            pmr.anDice[1] = currentMatch.anDice[1];
            pmr.fPlayer = currentMatch.anDice[1] > currentMatch.anDice[0] ? 1 : 0;

            addMoveRecord(pmr);
        }

        diceRolled();
    }

    private void diceRolled() {
        if (isShowLog()) {
            GameInfoPrinter.printBoard(agents, currentMatch, lMatch);
        }
    }

    private void addMoveRecord(MoveRecord pmr) {
        MoveRecord pmrOld = null;

        addMoveRecordGetCur(pmr);
        addMoverecordSanityCheck(pmr);

        boolean hasMoves = !lMatch.isEmpty() && !lMatch.getLast().moveRecords.isEmpty();
        if(hasMoves) {
            pmrOld = lMatch.getLast().moveRecords.getLast();
        }

        if(hasMoves && pmr.mt == MoveType.MOVE_NORMAL &&
                pmrOld.mt == MoveType.MOVE_SETDICE && pmrOld.fPlayer == pmr.fPlayer )
        {
            lMatch.getLast().moveRecords.removeLast();
        }

        //perform other elision (e.g. consecutive "set" records)
        fixMatchState(pmr);
        lMatch.getLast().moveRecords.addLast(pmr);
        ApplyMoveRecord( pmr );
    }

    private void addMoverecordSanityCheck(MoveRecord pmr) {
        assert(pmr.fPlayer >= 0 && pmr.fPlayer <= 1);
        assert(pmr.ml.cMoves < MoveList.MAX_MOVES);
        switch (pmr.mt) {
            case MOVE_GAMEINFO:
                break;

            case MOVE_NORMAL:
                if (pmr.ml.cMoves != 0) {
                    assert (pmr.n.iMove <= pmr.ml.cMoves);
                }
                break;

            case MOVE_SETDICE:
                break;

            default:
                throw new IllegalArgumentException();
        }
    }

    private void copyFromPmrCur(MoveRecord pmr, boolean get_move) {
        MoveRecord pmr_cur = getCurrentMoveRecord();
        if (pmr_cur == null)
            return;

        if (get_move && pmr_cur.ml.cMoves > 0) {
            pmr.ml = new MoveList(pmr_cur.ml);
            pmr.n.iMove = currentMatch.board.locateMove(pmr.n.anMove, pmr.ml);
        }
    }

    private void addMoveRecordGetCur(MoveRecord pmr) {
        switch (pmr.mt) {
            case MOVE_NORMAL:
                copyFromPmrCur(pmr, true);
                pmrHint = null;
                break;
            case MOVE_SETDICE:
                copyFromPmrCur(pmr, false);
                break;
            default:
                pmrHint = null;
                break;
        }
    }

    private MoveRecord getCurrentMoveRecord() {
        boolean hasMoves = !lMatch.isEmpty() && !lMatch.getLast().moveRecords.isEmpty();
        
        if(hasMoves)
        {
            return lMatch.getLast().moveRecords.getLast();
        }

        if (currentMatch.gs != GameState.GAME_PLAYING)
        {
            pmrHint = null;
            return null;
        }

	    // invalidate on changed dice 
        if (currentMatch.anDice[0] > 0 && pmrHint != null && pmrHint.anDice[0] > 0
                && (pmrHint.anDice[0] != currentMatch.anDice[0]
                || pmrHint.anDice[1] != currentMatch.anDice[1]))  {
            pmrHint = null;
        }

        if (pmrHint == null)
        {
            pmrHint = new MoveRecord();
            pmrHint.fPlayer = currentMatch.fTurn;
        }

        if (currentMatch.anDice[0] > 0)
        {
            pmrHint.mt = MoveType.MOVE_NORMAL;
            pmrHint.anDice[0] = currentMatch.anDice[0];
            pmrHint.anDice[1] = currentMatch.anDice[1];
        }
        return pmrHint;
    }

    private void fixMatchState(MoveRecord pmr) {
        switch (pmr.mt) {
            case MOVE_NORMAL:
                if (currentMatch.fTurn != pmr.fPlayer) {
                    // previous moverecord is missing
                    currentMatch.board.swapSides();
                    currentMatch.fMove = currentMatch.fTurn = pmr.fPlayer;
                }
                break;
            default:
                // no-op
                break;
        }
    }

    private void nextTurn() {
        MatchState ms = currentMatch;

        if (ms.board.gameStatus() != 0) {
            MoveRecord pmr = lMatch.getLast().moveRecords.getLast();
            XMoveGameInfo pmgi = pmr.g;

            int n = ms.board.gameStatus();

            if (showLog) {
                GameInfoPrinter.printWin(agents, currentMatch, pmgi.fWinner, pmgi.nPoints);
            }

            if (showLog) {
                GameInfoPrinter.printScore(agents, ms, playedGames);
            }
        }

        assert (currentMatch.gs == GameState.GAME_PLAYING);
        computerTurn();
    }

    private void computerTurn() {
        MatchState ms = currentMatch;
        if( ms.gs != GameState.GAME_PLAYING)
            return;

        FindData fd = new FindData();
        final char[] achResign = new char[] { 'n', 'g', 'b' };

		//Don't use the global board for this call, to avoid
		//race conditions with updating the board and aborting the
		//move with an interrupt.
        Board anBoardMove = new Board( ms.board);

        // Roll dice and move
        if (ms.anDice[ 0 ] == 0) {
            ms.rollDice();
            diceRolled();
        }

        MoveRecord pmr = new MoveRecord();
        pmr.mt = MoveType.MOVE_NORMAL;
        pmr.anDice[ 0 ] = ms.anDice[ 0 ];
        pmr.anDice[ 1 ] = ms.anDice[ 1 ];
        pmr.fPlayer = ms.fTurn;

        fd.ml = pmr.ml;
        fd.board = anBoardMove;
        fd.auchMove = null;
        fd.rThr = 0.0;
        if (FindMove(fd) != 0) {
            return;
        }
		/* resorts the moves according to cubeful (if applicable),
		* cubeless and chequer on highest point to avoid some silly
		* looking moves */
            //RefreshMoveList(pmr.ml, NULL);

		/* make the move found above */
            if ( pmr.ml.cMoves != 0) {
                pmr.n.anMove = pmr.ml.amMoves[0].anMove;
                pmr.n.iMove = 0;
                agents[currentMatch.fMove].agent.doMove(pmr.ml.amMoves[0]);
                if(pmr.ml.amMoves[0].pc == PositionClass.CLASS_OVER) {
                    Move endMove = pmr.ml.amMoves[0];
                    Board board = Board.positionFromKey(endMove.auch);
                    board.swapSides();
                    endMove.auch = board.PositionKey();
                    endMove.arEvalMove.invert();
                    agents[currentMatch.fMove == 1 ? 0 : 1].agent.doMove(endMove);
                }
            }
            pmr.ml.deleteMoves();

		// write move to status bar or stdout
        if(isShowLog()) {
            ShowAutoMove( pmr.n.anMove );
        }
        AddMoveRecord(pmr);
    }

    class AgentEntry {
        public IAgent agent;
        public int wonGames = 0;
        public int wonPoints = 0;
    }
}
