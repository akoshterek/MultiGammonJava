package org.akoshterek.backgammon.agent.fa;

import java.nio.file.Path;

/**
 * @author Alex
 *         date 26.09.2015.
 */
public interface NeuralNetworkFA extends FunctionApproximator {
    void saveNN(Path path);
}
