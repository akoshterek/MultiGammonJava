package org.akoshterek.backgammon.agent.fa;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.eval.Reward;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.persist.EncogDirectoryPersistence;

import java.io.File;

/**
 * @author Alex
 *         date 26.09.2015.
 */
public class SimpleEncogFA implements NeuralNetworkFA {
    private BasicNetwork network;

    @Override
    public void createNN(int inputNeurons, int hiddenNeurons) {
        network = new BasicNetwork();
        network.addLayer(new BasicLayer(null, false, inputNeurons));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, hiddenNeurons));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, Constants.NUM_OUTPUTS));
        network.getStructure().finalizeStructure();
        (new RangeRandomizer(-0.5,0.5)).randomize(network);
        network.reset();
    }

    @Override
    public void saveNN(File file) {
        EncogDirectoryPersistence.saveObject(file, network);
    }

    @Override
    public void loadNN(File file) {
        network = (BasicNetwork)EncogDirectoryPersistence.loadObject(file);
    }

    @Override
    public Reward getReward(double[] input) {
        Reward reward = new Reward();
        network.compute(input, reward.data);
        return reward;
    }

    @Override
    public void setReward(double[] input, Reward reward) {
        throw new UnsupportedOperationException("Learning is not implemented in this FA");
    }
}