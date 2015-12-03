package org.akoshterek.backgammon.train;

import org.akoshterek.backgammon.Constants;
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation;
import org.akoshterek.backgammon.agent.inputrepresentation.SuttonCodec;
import org.akoshterek.backgammon.agent.raw.RawRepresentation;
import org.akoshterek.backgammon.board.Board;
import org.akoshterek.backgammon.board.PositionClass;
import org.akoshterek.backgammon.data.TrainDataLoader;
import org.akoshterek.backgammon.data.TrainEntry;
import org.akoshterek.backgammon.util.Normalizer;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataPair;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.ml.train.strategy.end.SimpleEarlyStoppingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
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
        if(NetworkHolder.deserializeTrainedNetwork(settings, networkType) != null) {
            System.out.println("The network is already trained. Exiting.");
        }
        MLDataSet trainingSet = loadTraingSet(getResourceName());
        NetworkHolder holder = createLoadNetwork();
        Propagation train = createPropagation(holder, trainingSet);
        trainingLoop(holder, train);
        return holder;
    }

    private void trainingLoop(NetworkHolder holder, Propagation train) {
        do {
            train.iteration();
            System.out.println("Epoch #" + holder.getEpoch() + " Error:" + train.getError());
            holder.incEpoch();
            if(holder.getEpoch() % 10 == 0) {
                holder.setContinuation(train.pause());
                holder.serialize(settings);
            }
        } while(!train.isTrainingDone());
        train.finishTraining();
        holder.serializeTrainedNetwork(settings);
    }

    private NetworkHolder createLoadNetwork() {
        NetworkHolder holder = new NetworkHolder(
                createNetwork(getInputNeuronsCount(), settings.hiddenNeuronCount, Constants.NUM_OUTPUTS),
                networkType
        );
        NetworkHolder loadedHolder = NetworkHolder.deserialize(holder, settings);
        return loadedHolder != null ? loadedHolder : holder;
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

    private static Propagation createPropagation(NetworkHolder holder, MLDataSet trainingSet) {
        ResilientPropagation train = new ResilientPropagation(holder.getNetwork(), trainingSet);
        train.setRPROPType(RPROPType.iRPROPp);
        StopTrainingStrategy stop = new StopTrainingStrategy(0.00001, 100);
        train.addStrategy(new SimpleEarlyStoppingStrategy(trainingSet, 10));
        train.addStrategy(stop);

        if(holder.getContinuation() != null) {
            train.resume(holder.getContinuation());
        }

        return train;
    }

    private MLDataSet loadTraingSet(String resource) {
        List<TrainEntry> data = TrainDataLoader.loadGzipResourceData(resource);
        Collections.shuffle(data);

        MLDataSet trainingSet = new BasicMLDataSet();
        InputRepresentation representation = new RawRepresentation(new SuttonCodec());
        //double min = Double.MAX_VALUE;
        //double max = Double.MIN_VALUE;
        for(TrainEntry e : data) {
            MLData input = new BasicMLData(representation.calculateContactInputs(Board.positionFromID(e.positionId)));
//            for(int i = 0; i < input.size(); i++) {
//                min = Math.min(min, input.getData()[i]);
//                max = Math.max(max, input.getData()[i]);
//            }
            Normalizer.toSmallerSigmoid(e.reward);
            MLData ideal = new BasicMLData(e.reward);
            MLDataPair pair = new BasicMLDataPair(input, ideal);
            trainingSet.add(pair);
        }

        return trainingSet;
    }
}
