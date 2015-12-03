package org.akoshterek.backgammon.agent.fa;

import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

/**
 * @author Alex
 *         date 29.11.2015.
 */
public abstract class AbsNeuralNetworkFA implements NeuralNetworkFA {
    protected BasicNetwork network;

    public AbsNeuralNetworkFA(BasicNetwork network) {
        this.network = network;
    }

    public static BasicNetwork loadNN(File file) {
        return (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
    }

    public static BasicNetwork loadNNFromResource(String resource) {
        return  (BasicNetwork)EncogDirectoryPersistence.loadResourceObject(resource);
    }
}
