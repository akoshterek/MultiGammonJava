package org.akoshterek.backgammon.agent.gnubg;

/**
 * @author Alex
 *         date 12.09.2015.
 */
public interface InputConstants {
    interface RaceInputs {
        /** In a race position, bar and the 24 points are always empty, so only
         *  23*4 (92) are needed */

        /** (0 <= k < 14), RI_OFF + k =
         *  1 if exactly k+1 checkers are off, 0 otherwise */

        int RI_OFF = 92;

        /** Number of cross-overs by outside checkers */
        int RI_NCROSS = 92 + 14;

        int HALF_RACE_INPUTS = RI_NCROSS + 1;
    }

    interface ContactInputs {
        /** n - number of checkers off
         *
         *  off1 -  1         n >= 5
         *  n/5       otherwise
         *
         *  off2 -  1         n >= 10
         *  (n-5)/5   n < 5 < 10
         *  0         otherwise
         *
         *  off3 -  (n-10)/5  n > 10
         *  0         otherwise
         */
        int I_OFF1 = 0, I_OFF2 = 1, I_OFF3 = 2;

        /* Minimum number of pips required to break contact.

         For each checker x, N(x) is checker location,
         C(x) is max({forall o : N(x) - N(o)}, 0)

         Break Contact : (sum over x of C(x)) / 152

         152 is dgree of contact of start position.
        */
        int I_BREAK_CONTACT = 3;

        //Location of back checker (Normalized to [01])
        int I_BACK_CHEQUER = 4;

        // Location of most backward anchor.  (Normalized to [01])
        int I_BACK_ANCHOR = 5;

      /* Forward anchor in opponents home.

         Normalized in the following way:  If there is an anchor in opponents
         home at point k (1 <= k <= 6), value is k/6. Otherwise, if there is an
         anchor in points (7 <= k <= 12), take k/6 as well. Otherwise set to 2.

         This is an attempt for some continuity, since a 0 would be the "same" as
         a forward anchor at the bar.
       */
        int I_FORWARD_ANCHOR = 6;

      /* Average number of pips opponent loses from hits.

         Some heuristics are required to estimate it, since we have no idea what
         the best move actually is.

         1. If board is weak (less than 3 anchors), don't consider hitting on
            points 22 and 23.
         2. Don't break anchors inside home to hit.
       */
        int I_PIPLOSS = 7;

        //Number of rolls that hit at least one checker.
        int I_P1 = 8;

        // Number of rolls that hit at least two checkers.
        int I_P2 = 9;

        // How many rolls permit the back checker to escape (Normalized to [01])
        int I_BACKESCAPES = 10;

        /** Maximum containment of opponent checkers, from our points 9 to op back checker.
         *
         * Value is (1 - n/36), where n is number of rolls to escape.
         */
        int I_ACONTAIN = 11;

        // Above squared
        int I_ACONTAIN2 = 12;

        /** Maximum containment, from our point 9 to home.
          * Value is (1 - n/36), where n is number of rolls to escape.
          */
        int I_CONTAIN = 13;

        // Above squared
        int I_CONTAIN2 = 14;

      /* For all checkers out of home,
         sum (Number of rolls that let x escape * distance from home)

         Normalized by dividing by 3600.
      */
        int I_MOBILITY = 15;

      /* One sided moment.
         Let A be the point of weighted average:
         A = sum of N(x) for all x) / nCheckers.

         Then for all x : A < N(x), M = (average (N(X) - A)^2)

         Diveded by 400 to normalize.
       */
        int I_MOMENT2 = 16;

      /* Average number of pips lost when on the bar.
         Normalized to [01]
      */
        int I_ENTER = 17;

      /* Probability of one checker not entering from bar.
         1 - (1 - n/6)^2, where n is number of closed points in op home.
       */
        int I_ENTER2 = 18;

        int I_TIMING = 19;

        int I_BACKBONE = 20;

        int I_BACKG = 21;

        int I_BACKG1 = 22;

        int I_FREEPIP = 23;

        int I_BACKRESCAPES = 24;

        int MORE_INPUTS = 25;
    }

    int MINPPERPOINT = 4;
    int NUM_INPUTS = ((25 * MINPPERPOINT + ContactInputs.MORE_INPUTS) * 2);
    int NUM_RACE_INPUTS = RaceInputs.HALF_RACE_INPUTS * 2;
}
