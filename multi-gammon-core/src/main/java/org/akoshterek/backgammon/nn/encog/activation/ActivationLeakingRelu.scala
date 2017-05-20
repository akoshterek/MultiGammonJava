package org.akoshterek.backgammon.nn.encog.activation

import org.encog.engine.network.activation.ActivationFunction
import org.encog.util.obj.ActivationUtil

/**
  * Created by Alex on 20-05-17.
  */
@SerialVersionUID(1L)
class ActivationLeakingRelu(thresholdHigh: Double, thresholdLow: Double, high: Double, low: Double, leak: Double) extends ActivationFunction {
  private val params = Array[Double] (thresholdHigh, thresholdLow, high, low, leak)
  require(leak <= 0, "Leak can not be positive")

  val PARAM_RAMP_HIGH_THRESHOLD = 0
  val PARAM_RAMP_LOW_THRESHOLD = 1
  val PARAM_RAMP_HIGH = 2
  val PARAM_RAMP_LOW = 3
  val PARAM_RAMP_LEAK = 4


  def this() = {
    this(1.0D, 0.0D, 1.0D, 0.0D, -0.01)
  }

  override def activationFunction(x: Array[Double], start: Int, size: Int): Unit = {
    val slope = (this.params(PARAM_RAMP_HIGH_THRESHOLD) - this.params(PARAM_RAMP_LOW_THRESHOLD)) / (this.params(PARAM_RAMP_HIGH) - this.params(PARAM_RAMP_LOW))

    var i = start
    while (i < start + size) {
      val v = x(i)
      if (v < this.params(PARAM_RAMP_LOW_THRESHOLD)) {
        x(i) = this.params(PARAM_RAMP_LOW)
      }
      else if (v > this.params(PARAM_RAMP_HIGH_THRESHOLD)) {
        x(i) = this.params(PARAM_RAMP_HIGH)
      }
      else {
        x(i) = if (v >= 0) slope * v else -0.01 * v
      }

      i += 1
    }
  }

  override def clone = new ActivationLeakingRelu(
    this.params(PARAM_RAMP_HIGH_THRESHOLD),
    this.params(PARAM_RAMP_LOW_THRESHOLD),
    this.params(PARAM_RAMP_HIGH),
    this.params(PARAM_RAMP_LOW),
    this.params(PARAM_RAMP_LEAK)
  )

  override def derivativeFunction(b: Double, a: Double): Double = if (b >= 0) 1.0D else params(PARAM_RAMP_LEAK)

  override def getParamNames: Array[String] = Array[String]("thresholdHigh", "thresholdLow", "high", "low", "leak")

  override def getParams: Array[Double] = this.params

  override def hasDerivative = true

  override def setParam(index: Int, value: Double): Unit = {
    this.params(index) = value
  }

  override def getFactoryCode: String = ActivationUtil.generateActivationFactory("leakingrelu", this)
}
