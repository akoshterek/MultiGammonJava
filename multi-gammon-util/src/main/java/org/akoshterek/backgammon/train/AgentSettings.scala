package org.akoshterek.backgammon.train

import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation
import org.encog.engine.network.activation.ActivationFunction

class AgentSettings (val representation: InputRepresentation,
                     val hiddenNeuronCount: Int,
                     val agentName: String,
                     val activationFunction: ActivationFunction){
}