package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date 25.07.2015.
 */
public class Evaluator {
    public static int CHEQUERS = 15;

    private static Evaluator instance;
    private final RandomGenerator rng;
    private final UniformIntegerDistribution distribution;
    private Path basePath;

    public static Evaluator getInstance() {
        if (instance == null) {
            instance = new Evaluator();
        }

        return instance;
    }

    private Evaluator() {
        rng = new Well19937c();
        distribution = new UniformIntegerDistribution(rng, 1, 6);
    }

    public int nextDice() {
        return distribution.sample();
    }

    public void setSeed(long seed) {
        rng.setSeed(seed);
    }

    public PositionClass classifyPosition(Board anBoard) {
        int nOppBack = -1, nBack = -1;

        for (nOppBack = 24; nOppBack >= 0; --nOppBack) {
            if (anBoard.anBoard[0][nOppBack] != 0) {
                break;
            }
        }

        for (nBack = 24; nBack >= 0; --nBack) {
            if (anBoard.anBoard[1][nBack] != 0) {
                break;
            }
        }

        if (nBack < 0 || nOppBack < 0) {
            return PositionClass.CLASS_OVER;
        }

        // normal backgammon
        if (nBack + nOppBack > 22) {
            // contact position
            final int N = 6;
            int i;
            int side;

            for (side = 0; side < 2; ++side) {
                int tot = 0;
                byte[] board = anBoard.anBoard[side];

                for (i = 0; i < 25; ++i)
                    tot += anBoard.anBoard[side][i];

                if (tot <= N) {
                    return PositionClass.CLASS_CRASHED;
                } else {
                    if (board[0] > 1) {
                        if (tot <= (N + board[0])) {
                            return PositionClass.CLASS_CRASHED;
                        } else if (board[1] > 1 && (1 + tot - (board[0] + board[1])) <= N) {
                            return PositionClass.CLASS_CRASHED;
                        }
                    } else if (tot <= (N + (board[1] - 1)))
                        return PositionClass.CLASS_CRASHED;
                }
            }

            return PositionClass.CLASS_CONTACT;
        } else {
//            if ( isBearoff ( pbc2, anBoard ) )
//                return PositionClass.CLASS_BEAROFF2;
//
//            if ( isBearoff ( pbcTS, anBoard ) )
//                return PositionClass.CLASS_BEAROFF_TS;
//
//            if ( isBearoff ( pbc1, anBoard ) )
//                return PositionClass.CLASS_BEAROFF1;
//
//            if ( isBearoff ( pbcOS, anBoard ) )
//                return PositionClass.CLASS_BEAROFF_OS;

            return PositionClass.CLASS_RACE;
        }
    }

    public Reward evalOver(Board anBoard) {
        Reward reward = new Reward();
        final double[] arOutput = reward.data;
        int i, c;
        int n = CHEQUERS;

        for (i = 0; i < 25; i++) {
            if (anBoard.anBoard[0][i] != 0) {
                break;
            }
        }

        if (i == 25) {
        /* opponent has no pieces on board; player has lost */
            arOutput[OUTPUT_WIN] = arOutput[OUTPUT_WINGAMMON] = arOutput[OUTPUT_WINBACKGAMMON] = 0.0f;

            for (i = 0, c = 0; i < 25; i++) {
                c += anBoard.anBoard[1][i];
            }

            if (c == n) {
			/* player still has all pieces on board; loses gammon */
                arOutput[OUTPUT_LOSEGAMMON] = 1.0f;

                for (i = 18; i < 25; i++) {
                    if (anBoard.anBoard[1][i] != 0) {
					/* player still has pieces in opponent's home board;
					loses backgammon */
                        arOutput[OUTPUT_LOSEBACKGAMMON] = 1.0f;
                        return reward;
                    }
                }

                arOutput[OUTPUT_LOSEBACKGAMMON] = 0.0f;
                return reward;
            }

            arOutput[OUTPUT_LOSEGAMMON] = arOutput[OUTPUT_LOSEBACKGAMMON] = 0.0f;
            return reward;
        }

        for (i = 0; i < 25; i++) {
            if (anBoard.anBoard[1][i] != 0) {
                break;
            }
        }

        if (i == 25) {
		/* player has no pieces on board; wins */
            arOutput[OUTPUT_WIN] = 1.0f;
            arOutput[OUTPUT_LOSEGAMMON] = arOutput[OUTPUT_LOSEBACKGAMMON] = 0.0f;

            for (i = 0, c = 0; i < 25; i++) {
                c += anBoard.anBoard[0][i];
            }

            if (c == n) {
			/* opponent still has all pieces on board; win gammon */
                arOutput[OUTPUT_WINGAMMON] = 1.0f;

                for (i = 18; i < 25; i++) {
                    if (anBoard.anBoard[0][i] != 0) {
					/* opponent still has pieces in player's home board;
					win backgammon */
                        arOutput[OUTPUT_WINBACKGAMMON] = 1.0f;
                        return reward;
                    }
                }

                arOutput[OUTPUT_WINBACKGAMMON] = 0.0f;
                return reward;
            }

            arOutput[OUTPUT_WINGAMMON] = arOutput[OUTPUT_WINBACKGAMMON] = 0.0f;
        }

        return reward;
    }

    public Path getBasePath() {
        return basePath;
    }

    public void load(Path basePath) {
        this.basePath = basePath;
    }
}
