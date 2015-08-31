package org.akoshterek.backgammon.eval;

import java.util.Arrays;

import static org.akoshterek.backgammon.Constants.*;

/**
 * @author Alex
 *         date 19.07.2015.
 *
 * backgammon reward
 */
public final class Reward {
    public final double[] data = new double[NUM_ROLLOUT_OUTPUTS];

    public Reward() {}

    public Reward(double[] data) {
        System.arraycopy(data, 0, this.data, 0, this.data.length);
    }

    public Reward(Reward reward) {
        this(reward.data);
    }

    public void reset() {
        Arrays.fill(data, 0);
    }

    public void assign(Reward reward) {
        System.arraycopy(reward.data, 0, this.data, 0, this.data.length);
    }

    /**
     * Move evaluation
     * let's keep things simple as I don't want to go into cube handling
     * At least for now
     *
     * @return money equity
     */
    public double utility() {
        return data[OUTPUT_WIN] * 2.0 - 1.0 +
                (data[OUTPUT_WINGAMMON] - data[OUTPUT_LOSEGAMMON]) +
                (data[OUTPUT_WINBACKGAMMON] - data[OUTPUT_LOSEBACKGAMMON]);
    }

    public Reward invert() {
        Reward reward = new Reward(this);
        double r;

        reward.data[ OUTPUT_WIN ] = 1.0 - reward.data[ OUTPUT_WIN ];

        r = reward.data[ OUTPUT_WINGAMMON ];
        reward.data[ OUTPUT_WINGAMMON ] = reward.data[ OUTPUT_LOSEGAMMON ];
        reward.data[ OUTPUT_LOSEGAMMON ] = r;

        r = reward.data[ OUTPUT_WINBACKGAMMON ];
        reward.data[ OUTPUT_WINBACKGAMMON ] = reward.data[ OUTPUT_LOSEBACKGAMMON ];
        reward.data[ OUTPUT_LOSEBACKGAMMON ] = r;

        reward.data[ OUTPUT_EQUITY ] = -reward.data[ OUTPUT_EQUITY ];

        return reward;
    }

    public Reward clamp() {
        Reward reward = new Reward(this);
        for(int i = 0; i < reward.data.length; i++) {
            reward.data[i] = crop(0, 1, reward.data[i]);
        }

        return reward;
    }

    public static double crop(double minVal, double maxVal, double val) {
        if (minVal > maxVal) {
            double tmp = minVal;
            minVal = maxVal;
            maxVal = tmp;
        }

        return Math.min(Math.max(val, minVal), maxVal);
    }

    public static Reward plus(Reward r1, Reward r2) {
        Reward reward = new Reward();
        for (int i = 0; i < reward.data.length; i++) {
            reward.data[i] = r1.data[i] + r2.data[i];
        }
        return reward;
    }
}
