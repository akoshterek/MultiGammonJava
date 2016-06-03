package org.akoshterek.backgammon.agent.fa

import org.encog.neural.networks.BasicNetwork
import org.encog.persist.EncogDirectoryPersistence
import java.io.File

abstract class AbsNeuralNetworkFA(val network: BasicNetwork) extends NeuralNetworkFA {

}

object AbsNeuralNetworkFA {
    def loadNN(file: File): BasicNetwork = {
        return EncogDirectoryPersistence.loadObject(file).asInstanceOf[BasicNetwork]
    }

    def loadNNFromResource(resource: String): BasicNetwork = {
        return EncogDirectoryPersistence.loadResourceObject(resource).asInstanceOf[BasicNetwork]
    }
}

