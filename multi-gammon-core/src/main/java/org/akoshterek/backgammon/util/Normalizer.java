package org.akoshterek.backgammon.util;

/**
 * @author Alex
 *         date 24.09.2015.
 */
public class Normalizer {
    /**
     * normalizes from [0, 1] range to [-1, 1]
     * @param data the input array
     */
    public static void sigmoidToTanhNormalizer(double[] data) {
        for(int i = 0; i < data.length; i++) {
            data[i] = data[i] * 2 - 1;
        }
    }

    /**
     * normalizes from [0, 1] range to [-0.9, 0.9]
     * @param data the input array
     */
    public static void sigmoidToSmallerTanhNormalizer(double[] data) {
        for(int i = 0; i < data.length; i++) {
            data[i] = data[i] * 1.8 - 0.9;
        }
    }

    /**
     * normalizes from [0, 1] range to [0.1, 0.9]
     * @param data the input array
     */
    public static void sigmoidToSmaller(double[] data) {
        for(int i = 0; i < data.length; i++) {
            data[i] = data[i] * 0.8 + 0.1;
        }
    }
}
