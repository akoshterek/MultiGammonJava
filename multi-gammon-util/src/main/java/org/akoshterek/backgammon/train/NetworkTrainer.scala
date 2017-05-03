package org.akoshterek.backgammon.train

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.agent.inputrepresentation.{InputRepresentation, SuttonCodec}
import org.akoshterek.backgammon.agent.raw.RawRepresentation
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.data.TrainDataLoader
import org.akoshterek.backgammon.util.Normalizer
import org.encog.engine.network.activation.{ActivationClippedLinear, ActivationLinear, ActivationRamp}
import org.encog.mathutil.randomize.RangeRandomizer
import org.encog.ml.data.basic.{BasicMLData, BasicMLDataPair, BasicMLDataSet}
import org.encog.ml.data.{MLData, MLDataPair, MLDataSet}
import org.encog.ml.train.strategy.StopTrainingStrategy
import org.encog.ml.train.strategy.end.SimpleEarlyStoppingStrategy
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.Propagation
import org.encog.neural.networks.training.propagation.resilient.{RPROPType, ResilientPropagation}

import scala.util.Random

/**
  * @author Alex
  *         date 22.09.2015.
  */
object NetworkTrainer {
  private def createNetwork(inputNeurons: Int, hiddenNeurons: Int, outputNeurons: Int): BasicNetwork = {
    val network: BasicNetwork = new BasicNetwork
    network.addLayer(new BasicLayer(new ActivationLinear, false, inputNeurons))
    network.addLayer(new BasicLayer(new ActivationRamp(10, 0, 10, 0), false, hiddenNeurons))
    network.addLayer(new BasicLayer(new ActivationClippedLinear, false, outputNeurons))
    network.getStructure.finalizeStructure()
    new RangeRandomizer(-0.5, 0.5).randomize(network)
    network.reset()
    network
  }

  private def createPropagation(holder: NetworkHolder, trainingSet: MLDataSet): Propagation = {
    val train: ResilientPropagation = new ResilientPropagation(holder.network, trainingSet)
    train.setRPROPType(RPROPType.iRPROPp)
    val stop: StopTrainingStrategy = new StopTrainingStrategy(0.0001, 100)
    train.addStrategy(new SimpleEarlyStoppingStrategy(trainingSet, 10))
    train.addStrategy(stop)
    if (holder.continuation != null) {
      train.resume(holder.continuation)
    }
    train
  }
}

class NetworkTrainer(val settings: AgentSettings, val networkType: PositionClass) {
  def trainNetwork: NetworkHolder = {
    if (NetworkHolder.deserializeTrainedNetwork(settings, networkType) != null) {
      System.out.println("The network is already trained. Exiting.")
    }
    val trainingSet: MLDataSet = loadTraingSet(getResourceName)
    val holder: NetworkHolder = createLoadNetwork
    val train: Propagation = NetworkTrainer.createPropagation(holder, trainingSet)
    trainingLoop(holder, train)
    holder
  }

  private def trainingLoop(holder: NetworkHolder, train: Propagation) {
    do {
      train.iteration()
      System.out.println("Epoch #" + holder.epoch + " Error:" + train.getError)
      holder.incEpoch()
      if (holder.epoch % 10 == 0) {
        holder.continuation_$eq(train.pause)
        holder.serialize(settings)
      }
    } while (!train.isTrainingDone)
    train.finishTraining()
    holder.serializeTrainedNetwork(settings)
  }

  private def createLoadNetwork: NetworkHolder = {
    val holder: NetworkHolder = new NetworkHolder(NetworkTrainer.createNetwork(getInputNeuronsCount, settings.hiddenNeuronCount, Constants.NUM_OUTPUTS), networkType)
    val loadedHolder: NetworkHolder = NetworkHolder.deserialize(holder, settings).get
    if (loadedHolder != null) loadedHolder else holder
  }

  private def getResourceName: String = {
    String.format("/org/akoshterek/backgammon/data/%s-train-data.gz", PositionClass.getNetworkType(networkType))
  }

  private def getInputNeuronsCount: Int = {
    networkType match {
      case PositionClass.CLASS_CONTACT => settings.representation.getContactInputsCount
      case PositionClass.CLASS_CRASHED => settings.representation.getCrashedInputsCount
      case PositionClass.CLASS_RACE => settings.representation.getRaceInputsCount
      case _ => throw new IllegalArgumentException("Unknown network type " + networkType)
    }
  }

  private def loadTraingSet(resource: String): MLDataSet = {
    val data = Random.shuffle(TrainDataLoader.loadGzipResourceData(resource))
    val trainingSet: MLDataSet = new BasicMLDataSet
    val representation: InputRepresentation = new RawRepresentation(new SuttonCodec)
    for (e <- data) {
      val input: MLData = new BasicMLData(representation.calculateContactInputs(Board.positionFromID(e.positionId)))
      val ideal: MLData = new BasicMLData(Normalizer.toSmallerSigmoid(e.reward.toArray))
      val pair: MLDataPair = new BasicMLDataPair(input, ideal)
      trainingSet.add(pair)
    }

    trainingSet
  }
}