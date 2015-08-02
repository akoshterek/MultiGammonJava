package org.akoshterek.backgammon.move;

/**
 * @author Alex
 *         date 20.07.2015.
 */
public class XMoveGameInfo {
    /* ordinal number of the game within a match */
    public int i;
    /* match length */
    public int nMatch;
    /* match score BEFORE the game */
    public int[] anScore = new int[] {0, 0};
    /* the Crawford rule applies during this match */
    public int fCrawford;
    /* this is the Crawford game */
    public int fCrawfordGame;
    public int fJacoby;
    /* who won (-1 = unfinished) */
    public int fWinner;
    /* how many points were scored by the winner */
    public int nPoints;
    /* the game was ended by resignation */
    public int fResigned;
    /* how many automatic doubles were rolled */
    public int nAutoDoubles;
    /* Cube used in game */
    int fCubeUse;
}
