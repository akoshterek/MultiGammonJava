package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 03.08.2015.
 */
public class HeuristicAgent extends AbsAgent {
    public HeuristicAgent(Path path) {
        super(path);
        fullName = "Heuristic";
    }

    @Override
    public Reward evalContact(Board board) {
        Reward reward = new Reward();
        double value = 0;
        byte[] points = board.anBoard[Board.SELF];

        byte total = 0;
        for (int i = 0; i < 25; i++) {
            total += points[i];
        }
        byte atHome = (byte) (15 - total);

        // 1/15th of a point per man home
        value += atHome / 15.0;

        // -1/5th of a point per man on the bar
        value -= points[Board.BAR] / 5.0f;

        for (int i = 0; i < 24; i++) {
            // -1/10th of a point for each blot
            // +1/20th for contiguous points
            if (points[i] == 1) {
                value -= 0.10f;
            } else if (i > 0 && points[i] >= 2 && points[i - 1] >= 2) {
                value += 0.05;
            }

            int dist = 25 - i;

            // value based on closeness to home
            value += (12.5f - dist) * (float) points[i] / 225.0f;
        }

        reward.data[Constants.OUTPUT_WIN] = value;
        return reward;
    }
}
