package org.akoshterek.backgammon.agent;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.eval.Reward;

import java.nio.file.Path;
import java.util.Random;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public class RandomAgent extends AbsAgent {
    private final Random random = new Random();
    public RandomAgent(Path path) {
        super(path);
        fullName = "Random";
    }

    @Override
    public Reward evalContact(Board board) {
        Reward reward = new Reward();
        reward.data[Constants.OUTPUT_WIN] = random.nextDouble();
        return reward;
    }
}
