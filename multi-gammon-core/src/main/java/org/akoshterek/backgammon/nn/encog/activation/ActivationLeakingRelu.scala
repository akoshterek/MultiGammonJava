package org.akoshterek.backgammon.nn.encog.activation

import org.encog.engine.network.activation.ActivationFunction
import org.encog.util.obj.ActivationUtil

/**
  * Created by Alex on 20-05-17.
  */
@SerialVersionUID(1L)
class ActivationLeakingRelu(thresholdHigh: Double, thresholdLow: Double, high: Double, low: Double, leak: Double) extends ActivationFunction {
  override def getLabel(): String = "LEAKYRELU"
  private val params = Array[Double] (thresholdHigh, thresholdLow, high, low, leak)
  require(leak >= 0 && leak <= 1, "Leak must be in [0, 1] range")

  val PARAM_RAMP_HIGH_THRESHOLD = 0
  val PARAM_RAMP_LOW_THRESHOLD = 1
  val PARAM_RAMP_HIGH = 2
  val PARAM_RAMP_LOW = 3
  val PARAM_RAMP_LEAK = 4

  private def slope: Double = (params(PARAM_RAMP_HIGH_THRESHOLD) - params(PARAM_RAMP_LOW_THRESHOLD)) / (params(PARAM_RAMP_HIGH) - params(PARAM_RAMP_LOW))

  def this() = {
    this(1.0D, -1.0D, 1.0D, -1.0D, 0.01)
  }

  override def activationFunction(x: Array[Double], start: Int, size: Int): Unit = {
    var i = start
    while (i < start + size) {
      val v = x(i)
      if (v < params(PARAM_RAMP_LOW_THRESHOLD)) {
        x(i) = params(PARAM_RAMP_LOW)
      }
      else if (v > params(PARAM_RAMP_HIGH_THRESHOLD)) {
        x(i) = params(PARAM_RAMP_HIGH)
      }
      else {
        x(i) = if (v >= 0) slope * v else params(PARAM_RAMP_LEAK) * v
      }

      i += 1
    }
  }

  override def clone = new ActivationLeakingRelu(
    params(PARAM_RAMP_HIGH_THRESHOLD),
    params(PARAM_RAMP_LOW_THRESHOLD),
    params(PARAM_RAMP_HIGH),
    params(PARAM_RAMP_LOW),
    params(PARAM_RAMP_LEAK)
  )

  override def derivativeFunction(b: Double, a: Double): Double = {
    if (b >= 0)
      slope
    else
      params(PARAM_RAMP_LEAK)
  }

  override def getParamNames: Array[String] = {
    Array[String]("thresholdHigh", "thresholdLow", "high", "low", "leak")
  }

  override def getParams: Array[Double] = params

  override def hasDerivative = true

  override def setParam(index: Int, value: Double): Unit = {
    params(index) = value
  }

  override def getFactoryCode: String = ActivationUtil.generateActivationFactory("lrelu", this)
}
