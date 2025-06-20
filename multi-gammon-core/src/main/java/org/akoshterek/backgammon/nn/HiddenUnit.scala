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

  private var _sum = 0.0f
  /**
    * Builds a hidden unit taking the provided number of
    * inputs.  Sets the initial weights to be a copy of
    * the provided weights
    *
    * @param units   The input units to this unit
    */
  def this(units: Array[Neuron], activation: Activation) = {
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
    var accumulator = 0.0f
    while (i < weights.length) {
      accumulator += weights(i) * units(i).value
      i += 1
    }
    accumulator
  }

  /**
    * Recomputes the value of this hidden unit, querying it's
    * prior inputs.
    */
  override def recompute(): Unit = {
    _sum = sum
    _value = activation.f(_sum)
  }

  def gradient: Float = activation.gradient(_sum, _value)
}
