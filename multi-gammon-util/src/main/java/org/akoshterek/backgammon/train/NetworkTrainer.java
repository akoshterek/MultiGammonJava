package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.agent.inputrepresentation.SuttonCodec;
import org.akoshterek.backgammon.agent.raw.RawRepresentation;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.data.TrainDataLoader;
import org.akoshterek.backgammon.data.TrainEntry;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.CalculateScore;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.HybridStrategy;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.propagation.Propagation;
import org.encog.neural.networks.training.propagation.resilient.RPROPType;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.util.Collections;
import java.util.List;

/**
 * @author Alex
 *         date 22.09.2015.
 */
public class NetworkTrainer {
    private final AgentSettings settings;
    private final PositionClass networkType;

    public NetworkTrainer(AgentSettings settings, PositionClass networkType) {
        this.settings = settings;
        this.networkType = networkType;
    }

    public NetworkHolder trainNetwork() {
        MLDataSet trainingSet = loadTraingSet(getResourceName());

        NetworkHolder holder = new NetworkHolder(
                createNetwork(getInputNeuronsCount(), settings.hiddenNeuronCount, Constants.NUM_OUTPUTS),
                networkType, settings
        );
        Propagation train = createPropagation(holder.getNetwork(), trainingSet);

        do {
            train.iteration();
            System.out.println("Epoch #" + holder.getEpoch() + " Error:" + train.getError());
            holder.incEpoch();
        } while(!train.isTrainingDone());
        train.finishTraining();
        return holder;
    }

    private String getResourceName() {
        return String.format("/org/akoshterek/backgammon/data/%s-train-data.gz",
                PositionClass.getNetworkType(networkType));
    }

    private int getInputNeuronsCount() {
        switch (networkType) {
            case CLASS_CONTACT:
                return settings.representation.getContactInputsCount();
            case CLASS_CRASHED:
                return settings.representation.getCrashedInputsCount();
            case CLASS_RACE:
                return settings.representation.getRaceInputsCouns();
            default:
                throw new IllegalArgumentException("Unknown network type " + networkType);
        }
    }

    private static BasicNetwork createNetwork(int inputNeurons, int hiddenNeurons, int outputNeurons) {
        BasicNetwork network = new BasicNetwork();
        network.addLayer(new BasicLayer(null, false, inputNeurons));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, hiddenNeurons));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, outputNeurons));
        network.getStructure().finalizeStructure();
        (new RangeRandomizer(-0.5,0.5)).randomize(network);
        network.reset();
        return network;
    }

    private static Propagation createPropagation(BasicNetwork network, MLDataSet trainingSet) {
        ResilientPropagation train = new ResilientPropagation(network, trainingSet);
        train.setRPROPType(RPROPType.iRPROPp);
        CalculateScore score = new TrainingSetScore(trainingSet);
        MLTrain trainAlt = new NeuralSimulatedAnnealing(network, score, 10, 2, 100);
        StopTrainingStrategy stop = new StopTrainingStrategy();
        train.addStrategy(new HybridStrategy(trainAlt));
        train.addStrategy(stop);
        return train;
    }

    private MLDataSet loadTraingSet(String resource) {
        List<TrainEntry> data = TrainDataLoader.loadGzipResourceData(resource);
        Collections.shuffle(data);

        MLDataSet trainingSet = new BasicMLDataSet();
        InputRepresentation representation = new RawRepresentation(new SuttonCodec());
        data.stream().forEach(e -> {
            MLData input = new BasicMLData(representation.calculateContactInputs(Board.positionFromID(e.positionId)));
            MLData ideal = new BasicMLData(e.reward);
            MLDataPair pair = new BasicMLDataPair(input, ideal);
            trainingSet.add(pair);
        });

        return trainingSet;
    }
}
