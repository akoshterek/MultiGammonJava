package org.akoshterek.backgammon.eval;

/**
 * @author Alex
 *         date 05.08.2015.
 *
 * Cubeinfo contains the information necesary for evaluation
 * of a position.
 * These structs are placed here so that the move struct can be defined
 */

public class CubeInfo {
    /*
     * nCube: the current value of the cube,
     * fCubeOwner: the owner of the cube,
     * fMove: the player for which we are
     *        calculating equity for,
     * fCrawford, fJacoby, fBeavers: optional rules in effect,
     * arGammonPrice: the gammon prices;
     *   [ 0 ] = gammon price for player 0,
     *   [ 1 ] = gammon price for player 1,
     *   [ 2 ] = backgammon price for player 0,
     *   [ 3 ] = backgammon price for player 1.
     *
     */
    public int nCube = 1;
    public int fCubeOwner = -1;
    public int fMove = 0;
    public int nMatchTo = 0;
    public int[] anScore = new int[] {0, 0};

    public boolean fCrawford = false;
    public boolean fJacoby = false;
    public boolean fBeavers = false;
    public float[] gammonPrice = new float[] {0, 0, 0, 0};
}
