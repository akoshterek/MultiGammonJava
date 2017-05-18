package org.akoshterek.backgammon.agent.inputrepresentation

import org.encog.engine.network.activation.{ActivationFunction, ActivationRamp, ActivationSigmoid, ActivationTANH}
/**
  * Created by Alex on 18-05-17.
  */
object EncogActivationFunctionFactory {
  def apply(name: String): ActivationFunction = name.toLowerCase match {
    case "sigmoid" => new ActivationSigmoid()
    case "tanh" => new ActivationTANH()
    case "relu" => new ActivationRamp(10, 0, 10, 0)
    case _ => throw new IllegalArgumentException(s"Unknown activation function $name")
  }
}
