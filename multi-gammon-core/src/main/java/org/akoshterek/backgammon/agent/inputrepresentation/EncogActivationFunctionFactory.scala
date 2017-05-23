package org.akoshterek.backgammon.agent.inputrepresentation

import org.akoshterek.backgammon.nn.encog.activation.{ActivationElu, ActivationLeakingRelu, ActivationSoftplus}
import org.encog.engine.network.activation._

/**
  * Created by Alex on 18-05-17.
  */
object EncogActivationFunctionFactory {
  def apply(name: String): ActivationFunction = name.toLowerCase match {
    case "sigmoid" => new ActivationSigmoid()
    case "tanh" => new ActivationTANH()
    case "relu" => new ActivationRamp(10, 0, 10, 0)
    case "lrelu" => new ActivationLeakingRelu(10, -10, 10, -10, 0.01)
    case "elu" => new ActivationElu(10, -10, 10, -10, 0.1)
    case "softplus" => new ActivationSoftplus()
    case "elliot" => new ActivationElliottSymmetric()
    case "gaussian" => new ActivationGaussian()
    case "log" => new ActivationLOG()
    case "sin" => new ActivationSIN()
    case _ => throw new IllegalArgumentException(s"Unknown activation function $name")
  }
}
