package org.akoshterek.backgammon.agent.inputrepresentation;

/**
 * @author Alex
 *         date 21.09.2015.
 */
public interface PointCodec {
    default int getInputsPerPoint() {return 4;}
    void setPoint(byte men, double[] inputs, int offset);
}
