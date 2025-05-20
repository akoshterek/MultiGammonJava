package org.akoshterek.backgammon.nn.encog.activation

import org.encog.engine.network.activation.ActivationFunction
import org.encog.mathutil.BoundNumbers
import org.encog.util.obj.ActivationUtil

/**
  * Created by Alex on 20-05-17.
  */
@SerialVersionUID(1L)
class ActivationSoftplus extends ActivationFunction {
  private val params = Array[Double] ()

  override def activationFunction(x: Array[Double], start: Int, size: Int): Unit = {
    var i = start
    while (i < start + size) {
      x(i) = Math.log(1 + BoundNumbers.bound(Math.exp(x(i))))
      i += 1
    }
  }

  override def clone = new ActivationSoftplus()

  override def derivativeFunction(b: Double, a: Double): Double = {
    // 1.0 / (1 + BoundNumbers.bound(Math.exp(-b))) alternative form
    val e = BoundNumbers.bound(Math.exp(b))
    e / (e + 1)
  }

  override def getParamNames: Array[String] = Array[String]()

  override def getParams: Array[Double] = params

  override def hasDerivative = true

  override def setParam(index: Int, value: Double): Unit = {
    params(index) = value
  }

  override def getFactoryCode: String = ActivationUtil.generateActivationFactory("softplus", this)

  override def getLabel: String = "softplus"
}
