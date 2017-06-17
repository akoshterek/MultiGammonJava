package org.akoshterek.backgammon.nn

import java.util.concurrent.ThreadLocalRandom

/**
  * Builds a hidden unit taking the provided number of
  * inputs.
  *
  * @param units  The input units to this unit
  */
class HiddenUnit(val units: Array[Neuron],
                 val weights: Array[Float],
                 val activation: Activation) extends Neuron {

  /**
    * Builds a hidden unit taking the provided number of
    * inputs.  Sets the initial weights to be a copy of
    * the provided weights
    *
    * @param units   The input units to this unit
    */
  def this(units: Array[Neuron], activation: Activation) {
    this(units, Array.ofDim(units.length), activation)
    randomizeWeights()
  }

  /**
    * Generates a new weight
    */
  def randomizeWeights(): Unit = {
    var i = 0
    while (i < weights.length) {
      weights(i) = ThreadLocalRandom.current().nextDouble(-0.1, 0.1).toFloat
      i += 1
    }
  }

  /**
    * Returns the sum of all of the inputs and weights
    *
    * @return the sum
    */
  private def sum: Float = {
    var i = 0
    var accum = 0.0f
    while (i < weights.length) {
      accum += weights(i) * units(i).value
      i += 1
    }
    accum
  }

  /**
    * Recomputes the value of this hidden unit, querying it's
    * prior inputs.
    */
  override def recompute(): Unit = {
    _value = activation.f(sum)
  }

  def gradient: Float = activation.gradient(value)
}
