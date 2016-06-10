package org.akoshterek.backgammon.agent.pubeval;

import java.util.Arrays;

/**
 * @author Alex
 *         date 31.08.2015.
 */
class PubEval {
    private final double[] contactWeights = new double[122];
    private final double[] raceWeights = new double[122];

    private final double[] x = new double[122];

    PubEval(final double[] contactWeights, final double[] raceWeights) {
        System.arraycopy(contactWeights, 0, this.contactWeights, 0, this.contactWeights.length);
        System.arraycopy(raceWeights, 0, this.raceWeights, 0, this.raceWeights.length);
    }

    /**
     * sets input vector x[] given board position pos[]
     */
    private void setx(final int[] pos) {
        // initialize
        Arrays.fill(x, 0);

        //* first encode board locations 24-1 */
        for (int j = 1; j <= 24; ++j) {
            int jm1 = j - 1;
            int n = pos[25 - j];
            if (n != 0) {
                if (n == -1) x[5 * jm1] = 1.0f;
                if (n == 1) x[5 * jm1 + 1] = 1.0f;
                if (n >= 2) x[5 * jm1 + 2] = 1.0f;
                if (n == 3) x[5 * jm1 + 3] = 1.0f;
                if (n >= 4) x[5 * jm1 + 4] = (n - 3) / 2.0f;
            }
        }
        // encode opponent barmen
        x[120] = -(float) (pos[0]) / 2.0f;
        // encode computer's menoff
        x[121] = (float) (pos[26]) / 15.0f;
    }

    /* Backgammon move-selection evaluation function
       for benchmark comparisons.  Computes a linear
       evaluation function:  Score = W * X, where X is
       an input vector encoding the board state (using
       a raw encoding of the number of men at each location),
       and W is a weight vector.  Separate weight vectors
       are used for racing positions and contact positions.
       Makes lots of obvious mistakes, but provides a
       decent level of play for benchmarking purposes. */

    /* Provided as a public service to the backgammon
       programming community by Gerry Tesauro, IBM Research.
       (e-mail: tesauro@watson.ibm.com)                     */

    /* The following inputs are needed for this routine:

       race   is an integer variable which should be set
       based on the INITIAL position BEFORE the move.
       Set race=1 if the position is a race (i.e. no contact)
       and 0 if the position is a contact position.

       pos[]  is an integer array of dimension 28 which
       should represent a legal final board state after
       the move. Elements 1-24 correspond to board locations
       1-24 from computer's point of view, i.e. computer's
       men move in the negative direction from 24 to 1, and
       opponent's men move in the positive direction from
       1 to 24. Computer's men are represented by positive
       integers, and opponent's men are represented by negative
       integers. Element 25 represents computer's men on the
       bar (positive integer), and element 0 represents opponent's
       men on the bar (negative integer). Element 26 represents
       computer's men off the board (positive integer), and
       element 27 represents opponent's men off the board
       (negative integer).                                  */
    double evaluate(final int race, final int pos[]) {
        int i;
        double score;

        if (pos[26] == 15) return (99999999.);
        // all men off, best possible move

        setx(pos); // sets input array x[]
        score = 0.0f;
        if (race != 0) {// use race weights
            for (i = 0; i < 122; ++i) {
                score += raceWeights[i] * x[i];
            }
        } else { // use contact weights
            for (i = 0; i < 122; ++i) {
                score += contactWeights[i] * x[i];
            }
        }

        return (score);
    }
}
