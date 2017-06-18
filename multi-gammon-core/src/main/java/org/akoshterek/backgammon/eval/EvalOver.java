package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.board.Board;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date: 18-06-17.
 */
class EvalOver {
    public static Reward apply(final Board anBoard) {
        final float[] arOutput = new float[NUM_OUTPUTS()];
        final int CHEQUERS = 15;

        int i = anBoard.firstChequerIndex(Board.OPPONENT());
        if (i == -1) {
            // opponent has no pieces on board; player has lost
            arOutput[OUTPUT_WIN()] = arOutput[OUTPUT_WINGAMMON()] = arOutput[OUTPUT_WINBACKGAMMON()] = 0.0f;
            if (CHEQUERS == anBoard.chequersCount(Board.SELF())) {
                // player still has all pieces on board; loses gammon
                arOutput[OUTPUT_LOSEGAMMON()] = 1.0f;

                for (i = 18; i < 25; i++) {
                    if (anBoard.anBoard()[1][i] != 0) {
                        // player still has pieces in opponent's home board;
                        // loses backgammon
                        arOutput[OUTPUT_LOSEBACKGAMMON()] = 1.0f;
                        return new Reward(arOutput);
                    }
                }

                arOutput[OUTPUT_LOSEBACKGAMMON()] = 0.0f;
                return new Reward(arOutput);
            }

            arOutput[OUTPUT_LOSEGAMMON()] = arOutput[OUTPUT_LOSEBACKGAMMON()] = 0.0f;
            return new Reward(arOutput);
        }

        i = anBoard.firstChequerIndex(Board.SELF());
        if (i == -1) {
            // player has no pieces on board; wins
            arOutput[OUTPUT_WIN()] = 1.0f;
            arOutput[OUTPUT_LOSEGAMMON()] = arOutput[OUTPUT_LOSEBACKGAMMON()] = 0.0f;

            if (CHEQUERS == anBoard.chequersCount(Board.OPPONENT())) {
                // opponent still has all pieces on board; win gammon
                arOutput[OUTPUT_WINGAMMON()] = 1.0f;

                for (i = 18; i < 25; i++) {
                    if (anBoard.anBoard()[0][i] != 0) {
                        //opponent still has pieces in player's home board;
                        //win backgammon
                        arOutput[OUTPUT_WINBACKGAMMON()] = 1.0f;
                        return new Reward(arOutput);
                    }
                }

                arOutput[OUTPUT_WINBACKGAMMON()] = 0.0f;
                return new Reward(arOutput);
            }

            arOutput[OUTPUT_WINGAMMON()] = arOutput[OUTPUT_WINBACKGAMMON()] = 0.0f;
        }

        return new Reward(arOutput);
    }
}
