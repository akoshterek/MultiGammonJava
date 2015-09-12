package org.akoshterek.backgammon.agent.fa;

import org.akoshterek.backgammon.eval.Reward;

import java.io.File;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public interface FunctionApproximator {
    Reward getReward(float[] input);
    void setReward(float[] input, Reward reward);

    default void addToReward(float[] input, Reward deltaReward)
    {
        Reward currentReward = getReward(input);
        setReward(input, Reward.plus(currentReward, deltaReward).clamp());
    }

    void createNN(int input, int hidden, int output);
    void saveNN(File path, String name);
    boolean loadNN(File path, String name);
}
