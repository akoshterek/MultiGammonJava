package org.akoshterek.backgammon.eval;

import org.akoshterek.backgammon.bearoff.Bearoff;
import org.akoshterek.backgammon.bearoff.BearoffContext;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.board.PositionId;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;

import java.nio.file.Path;

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

    //private BearoffContext pbcOS;
    //private BearoffContext pbcTS;
    private BearoffContext pbc1;
    private BearoffContext pbc2;

    public static Evaluator getInstance() {
        if (instance == null) {
            instance = new Evaluator();
        }

        return instance;
    }

    private Evaluator() {
        rng = new Well19937c();
        distribution = new UniformIntegerDistribution(rng, 1, 6);

        loadBearoff();
    }

    public int nextDice() {
        return distribution.sample();
    }

    public void setSeed(long seed) {
        rng.setSeed(seed);
    }

    public PositionClass classifyPosition(Board anBoard) {
        int nOppBack, nBack;

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
            if (Bearoff.isBearoff(pbc2, anBoard))
                return PositionClass.CLASS_BEAROFF2;

            //if (Bearoff.isBearoff(pbcTS, anBoard))
            //    return PositionClass.CLASS_BEAROFF_TS;

            if (Bearoff.isBearoff(pbc1, anBoard))
                return PositionClass.CLASS_BEAROFF1;

            //if (Bearoff.isBearoff(pbcOS, anBoard))
            //    return PositionClass.CLASS_BEAROFF_OS;

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

    public Reward evalBearoff2(Board anBoard) {
        return Bearoff.bearoffEval(pbc2, anBoard);
    }

    public Reward evalBearoff1(Board anBoard) {
        return Bearoff.bearoffEval(pbc1, anBoard);
    }

//    public Reward evalBearoffTS(Board anBoard) {
//        return Bearoff.bearoffEval(pbcTS, anBoard);
//    }
//
//    public Reward evalBearoffOS(Board anBoard) {
//        return Bearoff.bearoffEval(pbcOS, anBoard);
//    }

    public void sanityCheck(Board board, Reward reward) {
        int i, j, nciq;
        int[] ac = new int[2];
        int[] anBack = new int[2];
        int[] anCross = new int[2];
        int[] anGammonCross = new int[2];
        int[] anBackgammonCross = new int[2];
        int[] anMaxTurns = new int[2];
        boolean fContact;

        if (reward.data[OUTPUT_WIN] < 0.0f) {
            reward.data[OUTPUT_WIN] = 0.0f;
        } else if (reward.data[OUTPUT_WIN] > 1.0f) {
            reward.data[OUTPUT_WIN] = 1.0f;
        }

        ac[0] = ac[1] = anBack[0] = anBack[1] = anCross[0] =
                anCross[1] = anBackgammonCross[0] = anBackgammonCross[1] = 0;
        anGammonCross[0] = anGammonCross[1] = 1;

        for (j = 0; j < 2; j++) {
            for (i = 0, nciq = 0; i < 6; i++)
                if (board.anBoard[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard[j][i];
                }
            ac[j] = anCross[j] = nciq;

            for (i = 6, nciq = 0; i < 12; i++)
                if (board.anBoard[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 2 * nciq;
            anGammonCross[j] += nciq;

            for (i = 12, nciq = 0; i < 18; i++)
                if (board.anBoard[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 3 * nciq;
            anGammonCross[j] += 2 * nciq;

            for (i = 18, nciq = 0; i < 24; i++)
                if (board.anBoard[j][i] != 0) {
                    anBack[j] = i;
                    nciq += board.anBoard[j][i];
                }
            ac[j] += nciq;
            anCross[j] += 4 * nciq;
            anGammonCross[j] += 3 * nciq;
            anBackgammonCross[j] = nciq;

            if (board.anBoard[j][24] != 0) {
                anBack[j] = 24;
                ac[j] += board.anBoard[j][24];
                anCross[j] += 5 * board.anBoard[j][24];
                anGammonCross[j] += 4 * board.anBoard[j][24];
                anBackgammonCross[j] += 2 * board.anBoard[j][24];
            }
        }

        fContact = anBack[0] + anBack[1] >= 24;

        if (!fContact) {
            for (i = 0; i < 2; i++) {
                if (anBack[i] < 6) {
                    anMaxTurns[i] =
                            maxTurns(PositionId.positionBearoff(board.anBoard[i], pbc1.getnPoints(), pbc1.getnChequers()));
                } else {
                    anMaxTurns[i] = anCross[i] * 2;
                }
            }

            if (anMaxTurns[1] == 0) {
                anMaxTurns[1] = 1;
            }
        }

        if (!fContact && anCross[0] > 4 * (anMaxTurns[1] - 1)) {
            // Certain win
            reward.data[OUTPUT_WIN] = 1.0f;
        }

        if (ac[0] < 15) {
            // Opponent has borne off; no gammons or backgammons possible
            reward.data[OUTPUT_WINGAMMON] = reward.data[OUTPUT_WINBACKGAMMON] = 0.0f;
        } else if (!fContact) {
            if (anCross[1] > 8 * anGammonCross[0]) {
                // Gammon impossible
                reward.data[OUTPUT_WINGAMMON] = 0.0f;
            } else if (anGammonCross[0] > 4 * (anMaxTurns[1] - 1)) {
                // Certain gammon
                reward.data[OUTPUT_WINGAMMON] = 1.0f;
            }

            if (anCross[1] > 8 * anBackgammonCross[0]) {
                // Backgammon impossible
                reward.data[OUTPUT_WINBACKGAMMON] = 0.0f;
            } else if (anBackgammonCross[0] > 4 * (anMaxTurns[1] - 1)) {
                // Certain backgammon
                reward.data[OUTPUT_WINGAMMON] = reward.data[OUTPUT_WINBACKGAMMON] = 1.0f;
            }
        }

        if (!fContact && anCross[1] > 4 * anMaxTurns[0]) {
            // Certain loss
            reward.data[OUTPUT_WIN] = 0.0f;
        }

        if (ac[1] < 15) {
            // Player has borne off; no gammon or backgammon losses possible
            reward.data[OUTPUT_LOSEGAMMON] = reward.data[OUTPUT_LOSEBACKGAMMON] = 0.0f;
        } else if (!fContact) {
            if (anCross[0] > 8 * anGammonCross[1] - 4) {
                // Gammon loss impossible
                reward.data[OUTPUT_LOSEGAMMON] = 0.0f;
            } else if (anGammonCross[1] > 4 * anMaxTurns[0]) {
                // Certain gammon loss
                reward.data[OUTPUT_LOSEGAMMON] = 1.0f;
            }

            if (anCross[0] > 8 * anBackgammonCross[1] - 4) {
                // Backgammon loss impossible
                reward.data[OUTPUT_LOSEBACKGAMMON] = 0.0f;
            } else if (anBackgammonCross[1] > 4 * anMaxTurns[0]) {
                // Certain backgammon loss
                reward.data[OUTPUT_LOSEGAMMON] = reward.data[OUTPUT_LOSEBACKGAMMON] = 1.0f;
            }
        }

        // gammons must be less than wins
        if (reward.data[OUTPUT_WINGAMMON] > reward.data[OUTPUT_WIN])
            reward.data[OUTPUT_WINGAMMON] = reward.data[OUTPUT_WIN];

        double lose = 1.0 - reward.data[OUTPUT_WIN];
        if (reward.data[OUTPUT_LOSEGAMMON] > lose)
            reward.data[OUTPUT_LOSEGAMMON] = lose;

        // Backgammons cannot exceed gammons
        if (reward.data[OUTPUT_WINBACKGAMMON] > reward.data[OUTPUT_WINGAMMON])
            reward.data[OUTPUT_WINBACKGAMMON] = reward.data[OUTPUT_WINGAMMON];

        if (reward.data[OUTPUT_LOSEBACKGAMMON] > reward.data[OUTPUT_LOSEGAMMON])
            reward.data[OUTPUT_LOSEBACKGAMMON] = reward.data[OUTPUT_LOSEGAMMON];

        double noise = 1 / 10000.0f;
        for (i = OUTPUT_WINGAMMON; i < OUTPUT_EQUITY; i++) {
            if (reward.data[i] < noise)
                reward.data[i] = 0;
        }
    }

    private int maxTurns(int id) {
        short[] aus = new short[32];

        Bearoff.bearoffDist(pbc1, id, null, null, null, aus, null);
        for (int i = 31; i >= 0; i--) {
            if (aus[i] != 0)
                return i;
        }

        return -1;
    }

    public Path getBasePath() {
        return basePath;
    }

    public void load(Path basePath) {
        this.basePath = basePath;
    }

    private void loadBearoff() {
        pbc1 = BearoffContext.bearoffInit("/org/akoshterek/backgammon/gnu/gnubg_os0.bd");
        pbc2 = BearoffContext.bearoffInit("/org/akoshterek/backgammon/gnu/gnubg_ts0.bd");
        //pbcOS = BearoffContext.bearoffInit("/org/akoshterek/backgammon/gnu/gnubg_os0.bd");
        //pbcTS = BearoffContext.bearoffInit("/org/akoshterek/backgammon/gnu/gnubg_ts0.bd");
    }
}