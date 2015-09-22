package org.akoshterek.backgammon.agent.inputrepresentation;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public class SuttonCodec implements PointCodec {
    @Override
    public String getName() {
        return "Sutton";
    }

    @Override
    public void setPoint(byte men, double[] inputs, int offset) {
        inputs[offset] = men >= 1 ? 1.0f : 0.0f;
        inputs[offset + 1] = men >= 2 ? 1.0f : 0.0f;
        inputs[offset + 2] = men >= 3 ? 1.0f : 0.0f;
        inputs[offset + 3] = men >= 4 ? (men - 3) / 12.0f : 0.0f;
    }
}
