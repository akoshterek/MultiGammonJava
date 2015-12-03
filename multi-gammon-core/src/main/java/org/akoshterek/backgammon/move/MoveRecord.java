package org.akoshterek.backgammon.move;

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
    /* evaluation of the moves */
    public MoveList ml = new MoveList();
    /* "private" data */
    public XMoveGameInfo g = new XMoveGameInfo();	/* game information */
    public XMoveNormal n = new XMoveNormal();		/* chequerplay move */
    public XMoveSetBoard sb = new XMoveSetBoard();	/* setting up board */
}
