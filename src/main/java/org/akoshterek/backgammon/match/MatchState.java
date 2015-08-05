package org.akoshterek.backgammon.match;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.CubeInfo;
import org.akoshterek.backgammon.eval.Evaluator;

/**
 * @author Alex
 *         date 05.08.2015.
 */
public class MatchState {
    public enum GameState { GAME_NONE, GAME_PLAYING, GAME_OVER, GAME_RESIGNED, GAME_DROP }

    public Board board;
    public int[] anDice = new int[] {0, 0};	// (0,0) for unrolled anDice
    public int fTurn = 0;		// who makes the next decision
    public boolean fResigned = false;
    public boolean fResignationDeclined = false;
    public boolean fDoubled = false;
    public int cGames = 0;
    public int fMove = 0;		// player on roll
    public int fCubeOwner = -1;
    public boolean fCrawford = false;
    public boolean fPostCrawford = false;
    public int nMatchTo = 0;
    public int[] anScore = new int[] {0, 0};
    public int nCube = 1;
    public int cBeavers = 0;
    public boolean fCubeUse = false;
    public boolean fJacoby = false;
    public GameState gs = GameState.GAME_NONE;

    public void rollDice()
    {
        anDice[0] = Evaluator.getInstance().nextDice();
        anDice[1] = Evaluator.getInstance().nextDice();
    }

    public CubeInfo getMatchStateCubeInfo() {
        return setCubeInfo( nCube, fCubeOwner, fMove, nMatchTo, anScore, fCrawford, fJacoby, false);
    }

    public CubeInfo setCubeInfo (int nCube, int fCubeOwner,
                      int fMove, int nMatchTo, int[] anScore,
                      boolean fCrawford, boolean fJacoby, boolean fBeavers) {
        if(nMatchTo != 0)
        {
            //return  SetCubeInfoMatch( pci, nCube, fCubeOwner, fMove,
            //		nMatchTo, anScore, fCrawford, bgv );
            //no matches
            throw new IllegalArgumentException();
        }
        else {
            return SetCubeInfoMoney(nCube, fCubeOwner, fMove, fJacoby, fBeavers);
        }

    }

    private CubeInfo SetCubeInfoMoney(int nCube, int fCubeOwner, int fMove, boolean fJacoby, boolean fBeavers) {

	    // also illegal if nCube is not a power of 2
        if( nCube < 1 || fCubeOwner < -1 || fCubeOwner > 1 || fMove < 0 || fMove > 1 )
        {
            throw new IllegalArgumentException();
        }

        CubeInfo pci = new CubeInfo();
        pci.nCube = nCube;
        pci.fCubeOwner = fCubeOwner;
        pci.fMove = fMove;
        pci.fJacoby = fJacoby;
        pci.fBeavers = fBeavers;
        pci.nMatchTo = pci.anScore[ 0 ] = pci.anScore[ 1 ] = 0;
        pci.fCrawford = false;

        pci.gammonPrice[ 0 ] = pci.gammonPrice[ 1 ] =
                pci.gammonPrice[ 2 ] = pci.gammonPrice[ 3 ] =
                        ( fJacoby && fCubeOwner == -1 ) ? 0.0f : 1.0f;

        return pci;
    }
}
