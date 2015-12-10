package org.akoshterek.backgammon.util;

/**
 * @author Alex
 *         date 24.09.2015.
 */
public class Normalizer {
    /**
     * normalizes from [0, 1] range to [0.1, 0.9]
     * @param data the input array
     */
    public static void toSmallerSigmoid(double[] data) {
        toSmallerSigmoid(data, data.length);
    }

    /**
     * normalizes from [0, 1] range to [0.1, 0.9]
     * @param data the input array
     */
    public static void toSmallerSigmoid(double[] data, int length) {
        for(int i = 0; i < length; i++) {
            data[i] = data[i] * 0.98 + 0.01;
        }
    }

    /**
     * normalizes from [0.1, 0.9] range to [0, 1]
     * @param data the input array
     */
    public static void fromSmallerSigmoid(double[] data) {
        fromSmallerSigmoid(data, data.length);
    }

    /**
     * normalizes from [0.1, 0.9] range to [0, 1]
     * @param data the input array
     * @param length the array length
     */
    public static void fromSmallerSigmoid(double[] data, int length) {
        for(int i = 0; i < length; i++) {
            data[i] = (data[i] - 0.01 ) / 0.98;
        }
    }
}
