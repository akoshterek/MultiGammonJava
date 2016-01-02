package org.akoshterek.backgammon.dispatch;

import org.akoshterek.backgammon.agent.Agent;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.eval.Reward;
import org.akoshterek.backgammon.match.GameState;
import org.akoshterek.backgammon.match.MatchMove;
import org.akoshterek.backgammon.match.MatchState;
import org.akoshterek.backgammon.move.*;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @author Alex
 *         date 04.08.2015.
 */
public class GameDispatcher {
    private AgentEntry[] agents = new AgentEntry[] {new AgentEntry(), new AgentEntry()};
    private boolean showLog = false;
    private int numGames = 0;
    private int playedGames = 0;

    private MatchState currentMatch;
    private Deque<MatchMove> lMatch = new LinkedList<>();
    //std::list<std::list<moverecord> > m_lMatch;
    private MoveRecord pmrHint;
    private Move[] amMoves;

    public GameDispatcher(Agent agent1, Agent agent2) {
        agents[0].agent = agent1;
        agents[1].agent = agent2;

        amMoves = new Move[MoveList.MAX_INCOMPLETE_MOVES];
        for(int i = 0; i < MoveList.MAX_INCOMPLETE_MOVES; i++) {
            amMoves[i] = new Move();
        }
    }

    public boolean isShowLog() {
        return showLog;
    }

    public void setIsShowLog(boolean isShowLog) {
        this.showLog = isShowLog;
    }

    public void playGames(int games, boolean learn)
    {
        agents[0].agent.setLearnMode(learn);
        agents[1].agent.setLearnMode(learn);

        for(int i = numGames + 1; i < numGames + games + 1; i++)
        {
            playGame();
            if(i % 100 == 0) {
                System.out.print(String.format("%d ", i));
            }

            //ExportMatchMat("-", true);
        }

        numGames += games;
        if(learn) {
            agents[0].agent.save();
        }
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
        applyMoveRecord(pmr);
    }

    private void applyMoveRecord(MoveRecord pmr) {
        Deque<MoveRecord> lGame = lMatch.getLast().moveRecords;

        int n;
        MoveRecord pmrx = lGame.getFirst();
        XMoveGameInfo pmgi;
        //this is wrong -- plGame is not necessarily the right game

        assert (pmr.mt == MoveType.MOVE_GAMEINFO || pmrx.mt == MoveType.MOVE_GAMEINFO);
        pmgi = pmrx.g;

        currentMatch.gs = GameState.GAME_PLAYING;

        switch (pmr.mt) {
            case MOVE_GAMEINFO:
                currentMatch.board.initBoard();

                currentMatch.anScore[0] = pmr.g.anScore[0];
                currentMatch.anScore[1] = pmr.g.anScore[1];

                currentMatch.gs = GameState.GAME_NONE;
                currentMatch.fMove = currentMatch.fTurn = -1;
                currentMatch.anDice[0] = currentMatch.anDice[1] = 0;
                break;

            case MOVE_NORMAL:
                playMove(pmr.n.anMove, pmr.fPlayer);
                currentMatch.anDice[0] = currentMatch.anDice[1] = 0;

                if ((n = currentMatch.board.gameStatus()) != 0) {
                    currentMatch.gs = GameState.GAME_OVER;
                    pmgi.nPoints = n;
                    pmgi.fWinner = pmr.fPlayer;
                    applyGameOver();
                }
                break;

            case MOVE_SETBOARD:
                currentMatch.board = Board.positionFromKey(pmr.sb.auchKey);

                if (currentMatch.fMove < 0) {
                    currentMatch.fTurn = currentMatch.fMove = 0;
                }

                if (currentMatch.fMove != 0) {
                    currentMatch.board.swapSides();
                }
                break;

            case MOVE_SETDICE:
                currentMatch.anDice[0] = pmr.anDice[0];
                currentMatch.anDice[1] = pmr.anDice[1];
                if (currentMatch.fMove != pmr.fPlayer)
                    currentMatch.board.swapSides();
                currentMatch.fTurn = currentMatch.fMove = pmr.fPlayer;
                break;
        }
    }

    private void playMove(ChequersMove anMove, int fPlayer) {
        if (currentMatch.fMove != -1 && fPlayer != currentMatch.fMove) {
            currentMatch.board.swapSides();
        }

        for (int i = 0; i < 4; i++) {
            int nSrc = anMove.move[i].from;
            int nDest = anMove.move[i].to;

            if (nSrc < 0) {
                // move is finished
                break;
            }

            if (currentMatch.board.anBoard[1][nSrc] == 0) {
                // source point is empty; ignore
                continue;
            }

            currentMatch.board.anBoard[1][nSrc]--;
            if (nDest >= 0) {
                currentMatch.board.anBoard[1][nDest]++;
            }

            if (nDest >= 0 && nDest <= 23) {
                currentMatch.board.anBoard[0][24] += currentMatch.board.anBoard[0][23 - nDest];
                currentMatch.board.anBoard[0][23 - nDest] = 0;
            }
        }

        currentMatch.fMove = currentMatch.fTurn = fPlayer != 0 ? 0 : 1;
        currentMatch.board.swapSides();
    }

    private void applyGameOver()
    {
        MoveRecord pmr = lMatch.getLast().moveRecords.getFirst();
        XMoveGameInfo pmgi = pmr.g;

        assert( pmr.mt == MoveType.MOVE_GAMEINFO );

        if( pmgi.fWinner < 0 ) {
            return;
        }

        int n = currentMatch.board.gameStatus();
        currentMatch.anScore[ pmgi.fWinner ] += pmgi.nPoints;
        playedGames++;
        if (isShowLog()) {
            GameInfoPrinter.printGameOver(agents, pmgi.fWinner, pmgi.nPoints, n);
        }
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
            case MOVE_SETBOARD:
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

            if (showLog) {
                GameInfoPrinter.printWin(agents, currentMatch, pmgi.fWinner, pmgi.nPoints);
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

		//Don't use the global board for this call, to avoid
		//race conditions with updating the board and aborting the
		//move with an interrupt.
        Board anBoardMove = new Board(ms.board);

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
        findMove(fd);
		/* resorts the moves according to cubeful (if applicable),
		  cubeless and chequer on highest point to avoid some silly
		  looking moves */
            //RefreshMoveList(pmr.ml, NULL);

		// make the move found above
        if (pmr.ml.cMoves != 0) {
            pmr.n.anMove = pmr.ml.amMoves[0].anMove;
            pmr.n.iMove = 0;
            agents[currentMatch.fMove].agent.doMove(pmr.ml.amMoves[0]);
            if (pmr.ml.amMoves[0].pc == PositionClass.CLASS_OVER) {
                //update the lost agent
                forceMove(pmr.ml.amMoves[0]);
            }
        } //else {
//            // no moves possible agent is blocked.
//            // Update the blocked agent with current position for learning purpose
//            Board board = new Board(ms.board);
//            Move move = new Move();
//            move.auch = board.calcPositionKey();
//            move.pc = Evaluator.getInstance().classifyPosition(board);
//            move.arEvalMove = agents[currentMatch.fMove == 1 ? 0 : 1].agent.evaluatePosition(board, move.pc);
//            //forceMove(move);
//        }
        pmr.ml.deleteMoves();

		// write move to status bar or stdout
        if(isShowLog()) {
            GameInfoPrinter.showAutoMove( pmr.n.anMove, agents, currentMatch);
        }
        addMoveRecord(pmr);
    }

    private void forceMove(Move endMove) {
        Board board = Board.positionFromKey(endMove.auch);
        board.swapSides();
        endMove.auch = board.calcPositionKey();
        endMove.arEvalMove.invert();
        agents[currentMatch.fMove == 1 ? 0 : 1].agent.doMove(endMove);
    }

    private void findMove(FindData pfd) {
        findAndSaveBestMoves(pfd.ml, currentMatch.anDice[0], currentMatch.anDice[1], pfd.board);
    }

    private void findAndSaveBestMoves(MoveList pml, int nDice0, int nDice1,Board anBoard) {
        // Find best moves.
	    /* Find all moves -- note that pml contains internal pointers to static
		data, so we can't call GenerateMoves again (or anything that calls
		it, such as ScoreMoves at more than 0 plies) until we have saved
		the moves we want to keep in amCandidates. */
        //it doesn't now
        MoveGenerator.generateMoves(anBoard, pml, amMoves, nDice0, nDice1, false);
        agents[currentMatch.fMove].agent.setCurrentBoard(currentMatch.board);
        if (pml.cMoves == 0) {
            // no legal moves
            pml.amMoves = null;
            return;
        }

	    // Save moves
        Move[] pm = new Move[pml.cMoves];
        System.arraycopy(pml.amMoves, 0, pm, 0, pml.cMoves);
        pml.amMoves = pm;
	    // evaluate moves on top ply
        scoreMoves(pml);
	    // Resort the moves, in case the new evaluation reordered them.
        Arrays.sort(pml.amMoves, 0, pml.cMoves, Move.moveComparator);
        pml.iMoveBest = 0;
    }

    private void scoreMoves(MoveList pml) {
        pml.rBestScore = -99999.9;
        for (int i = 0; i < pml.cMoves; i++) {
            scoreMove(pml.amMoves[i]);
            if (pml.amMoves[i].rScore > pml.rBestScore) {
                pml.iMoveBest = i;
                pml.rBestScore = pml.amMoves[i].rScore;
            }
        }
    }

    private void scoreMove(Move pm) {
        Board anBoardTemp = Board.positionFromKey(pm.auch);
        anBoardTemp.swapSides();

        pm.pc = Evaluator.getInstance().classifyPosition(anBoardTemp);
        Reward arEval = evaluatePositionFull(anBoardTemp, pm.pc);

        Agent agent = agents[currentMatch.fMove].agent;
        if(agent.needsInvertedEval()) {
            arEval.invert();
        } else if(PositionClass.isExact(pm.pc)) {
            //TODO why?
            if(pm.pc == PositionClass.CLASS_OVER || agent.supportsBearoff()) {
                arEval.invert();
            }
        }

        // Save evaluations
        pm.arEvalMove = arEval;
        pm.rScore = arEval.equity();
    }

    private Reward evaluatePositionFull(Board anBoard, PositionClass pc ) {
	    // at leaf node; use static evaluation
        Agent agent = agents[currentMatch.fMove].agent;
        Reward reward = new Reward(agent.evaluatePosition(anBoard, pc));

        if (!PositionClass.isExact(pc) && agent.supportsSanityCheck() && !agent.isLearnMode()) {
		    // no sanity check needed for exact evaluations
            Evaluator.getInstance().sanityCheck(anBoard, reward);
        }

        return reward;
    }


    class AgentEntry {
        public Agent agent;
        public int wonGames = 0;
        public int wonPoints = 0;
    }
}
