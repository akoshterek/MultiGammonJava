package org.akoshterek.backgammon.agent.fa

import java.io.File

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.util.Normalizer
import org.encog.engine.network.activation.ActivationSigmoid
import org.encog.mathutil.randomize.RangeRandomizer
import org.encog.ml.data.MLDataSet
import org.encog.neural.data.basic.BasicNeuralDataSet
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.layers.BasicLayer
import org.encog.neural.networks.training.propagation.Propagation
import org.encog.neural.networks.training.propagation.back.Backpropagation
import org.encog.persist.EncogDirectoryPersistence
import java.nio.file.Path

object SimpleEncogFA {
    def createNN(inputNeurons: Int, hiddenNeurons: Int): BasicNetwork = {
        val network: BasicNetwork = new BasicNetwork
        network.addLayer(new BasicLayer(null, false, inputNeurons))
        network.addLayer(new BasicLayer(new ActivationSigmoid, false, hiddenNeurons))
        network.addLayer(new BasicLayer(new ActivationSigmoid, false, 1))
        network.getStructure.finalizeStructure()
        new RangeRandomizer(-0.1, 0.1).randomize(network)
        network.reset()
        network
    }

    def loadNN(file: File): BasicNetwork = {
        EncogDirectoryPersistence.loadObject(file).asInstanceOf[BasicNetwork]
    }

    def loadNNFromResource(resource: String): BasicNetwork = {
        EncogDirectoryPersistence.loadResourceObject(resource).asInstanceOf[BasicNetwork]
    }
}

class SimpleEncogFA(override val network: BasicNetwork) extends AbsNeuralNetworkFA(network) {
    private val trainingSet: MLDataSet = new BasicNeuralDataSet(
        Array[Array[Double]](new Array[Double](network.getInputCount)),
        Array[Array[Double]](new Array[Double](network.getOutputCount))
    )

    private val propagation: Propagation = new Backpropagation(network, trainingSet, 0.05, 0)

    override def saveNN(file: Path) {
        EncogDirectoryPersistence.saveObject(file.toFile, network)
    }

    override def calculateReward(input: Array[Double]): Reward = {
        val reward = Reward.rewardArray
        network.compute(input, reward)
        new Reward(Normalizer.fromSmallerSigmoid(reward))
    }

    override def setReward(input: Array[Double], reward: Reward) {
        val output = Normalizer.toSmallerSigmoid(reward.data.toArray, Constants.NUM_OUTPUTS)
        trainingSet.get(0).getInput.setData(input)
        trainingSet.get(0).getIdeal.setData(output)
        propagation.iteration()
    }
}