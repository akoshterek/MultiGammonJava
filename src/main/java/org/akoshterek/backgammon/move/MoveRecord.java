package org.akoshterek.backgammon.move;

import org.akoshterek.backgammon.eval.EvalSetup;
import org.akoshterek.backgammon.eval.EvalType;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public class MoveRecord {
    /*
     * Common variables
     */
	/* type of the move */
    public MoveType mt = MoveType.MOVE_INVALID;
	/* annotation */
    public String sz;
    /* move record is for player */
    public int fPlayer = 0;
	/* luck analysis (shared between MOVE_SETDICE and MOVE_NORMAL) */
	/* dice rolled */
    public int[] anDice = new int[] {0, 0};
    /* move analysis (shared between MOVE_SETDICE and MOVE_NORMAL) */
	/* evaluation setup for move analysis */
    EvalSetup esChequer;
    /* evaluation of the moves */
    public MoveList ml = new MoveList();
    /* cube analysis (shared between MOVE_NORMAL and MOVE_DOUBLE) */
	/* 0 in match play, even numbers are doubles, raccoons
	   odd numbers are beavers, aardvarken, etc. */
    public int nAnimals = 0;
    /* "private" data */
    public XMoveGameInfo g = new XMoveGameInfo();	/* game information */
    public XMoveNormal n = new XMoveNormal();		/* chequerplay move */
    public XMoveResign r = new XMoveResign();		/* resignation */
    public XMoveSetBoard sb = new XMoveSetBoard();	/* setting up board */
    public XMoveSetCubeVal scv = new XMoveSetCubeVal();	/* setting cube */
    public XMoveSetCubePos scp = new XMoveSetCubePos();	/* setting cube owner */

    public MoveRecord() {
        n.anMove.move[ 0 ] = n.anMove.move[ 1 ] = -1;
        n.iMove = Integer.MAX_VALUE;
        esChequer.et = EvalType.EVAL_NONE;
    }
}
