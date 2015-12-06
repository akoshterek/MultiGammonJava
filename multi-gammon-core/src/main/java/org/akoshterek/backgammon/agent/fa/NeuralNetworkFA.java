package org.akoshterek.backgammon.agent.fa;

import java.io.File;

/**
 * @author Alex
 *         date 26.09.2015.
 */
public interface NeuralNetworkFA extends FunctionApproximator {
    void saveNN(File path);
}
