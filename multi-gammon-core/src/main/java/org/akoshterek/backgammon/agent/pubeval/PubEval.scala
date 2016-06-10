package org.akoshterek.backgammon.agent.pubeval

import java.util

class PubEval {
    private val _contactWeights: Array[Double] = new Array[Double](122)
    private val _raceWeights: Array[Double] = new Array[Double](122)

    def this(contactWeights: Array[Double], raceWeights: Array[Double]) {
       this()
        System.arraycopy(contactWeights, 0, this._contactWeights, 0, this._contactWeights.length)
        System.arraycopy(raceWeights, 0, this._raceWeights, 0, this._raceWeights.length)
    }

    final private val x: Array[Double] = new Array[Double](122)

    /**
      * sets input vector x[] given board position pos[]
      */
    private def setx(pos: Array[Int]) {
        // initialize
        util.Arrays.fill(x, 0)

        // first encode board locations 24-1
        for (j <- 1 to 24) {
            val jm1: Int = j - 1
            val n: Int = pos(25 - j)
            if (n != 0) {
                if (n == -1) x(5 * jm1) = 1.0f
                if (n == 1) x(5 * jm1 + 1) = 1.0f
                if (n >= 2) x(5 * jm1 + 2) = 1.0f
                if (n == 3) x(5 * jm1 + 3) = 1.0f
                if (n >= 4) x(5 * jm1 + 4) = (n - 3) / 2.0f
            }
        }
        // encode opponent barmen
        x(120) = -pos(0).toDouble / 2.0
        // encode computer's menoff
        x(121) = pos(26).toDouble / 15.0
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
    def evaluate(race: Int, pos: Array[Int]): Double = {
        if (pos(26) == 15) {
            // all men off, best possible move
            99999999.0
        } else {
            setx(pos)
            val score = (x, if(race != 0) raceWeights else contactWeights).zipped.map(_ * _).sum
            score
        }
    }

    def contactWeights: Array[Double] = _contactWeights

    def raceWeights: Array[Double] = _raceWeights
}