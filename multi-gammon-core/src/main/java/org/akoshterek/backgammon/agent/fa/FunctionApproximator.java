package org.akoshterek.backgammon.agent.fa;

import org.akoshterek.backgammon.eval.Reward;

/**
 * @author Alex
 *         date 02.08.2015.
 */
public interface FunctionApproximator {
    Reward calculateReward(double[] input);
    void setReward(double[] input, Reward reward);

    default void updateAddToReward(double[] input, Reward deltaReward)
    {
        Reward currentReward = calculateReward(input);
        setReward(input, Reward.plus(currentReward, deltaReward).clamp());
    }
}
