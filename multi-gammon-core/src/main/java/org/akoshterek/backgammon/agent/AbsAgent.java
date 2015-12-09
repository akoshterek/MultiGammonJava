package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.eval.Evaluator;
import org.akoshterek.backgammon.move.Move;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public abstract class AbsAgent implements Agent {
    private static final String AGENTS_SUBFOLDER = "bin/agents";

    private final Path path;
    protected String fullName = "";
    private boolean learnMode = false;
    private boolean supportsSanityCheck = false;
    private int playedGames = 0;
    protected boolean fixed = true;
    private boolean needsInvertedEval = false;
    protected boolean supportsBearoff = false;
    protected Board curBoard = null;
    protected PositionClass curPC = PositionClass.CLASS_OVER;

    public AbsAgent(Path path) {
        this.path = Paths.get(path.toString(), AGENTS_SUBFOLDER);
    }

    public String getFullName() {
        return fullName;
    }

    public Path getPath() {
        return path;
    }

    public int getPlayedGames() {
        return playedGames;
    }
    public void startGame() {}

    public void endGame() {
        if(learnMode) {
            playedGames++;
        }
    }

    public void doMove(Move move) {}

    public boolean isLearnMode() {
        return learnMode;
    }

    public void setLearnMode(boolean learn) {
        this.learnMode = learn;
    }

    public boolean isFixed() {
        return this.fixed;
    }

    public boolean needsInvertedEval() {
        return this.needsInvertedEval;
    }

    public void setNeedsInvertedEval(boolean needsInvertedEval) {
        this.needsInvertedEval = needsInvertedEval;
    }

    public boolean supportsSanityCheck() {
        return this.supportsSanityCheck;
    }

    public void setSanityCheck(boolean sc) {
        this.supportsSanityCheck = sc;
    }

    public boolean supportsBearoff() {
        return this.supportsBearoff;
    }

    public void setCurrentBoard(Board board) {
        curBoard = new Board(board);
        curPC = Evaluator.getInstance().classifyPosition(curBoard);
    }

    @Override
    public AbsAgent clone() {
        try {
            return (AbsAgent)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
