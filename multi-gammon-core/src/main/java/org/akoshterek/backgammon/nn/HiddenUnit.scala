package org.akoshterek.backgammon.nn

import java.util.concurrent.ThreadLocalRandom

object HiddenUnit {
  // the minimal and maximal initial values
  private val MIN_INITIAL_WEIGHT: Double = -0.1
  private val MAX_INITIAL_WEIGHT: Double = 0.1

  /**
    * Implements the sigmoid function to provide the non-linearity
    * to this function.  Simply returns
    * <p>
    * 1 / (1 + e&#94;-x)
    *
    */
  private val sigmoid : Double => Double = {
    x => 1 / (1 + Math.exp(-x))
  }
}

/**
  * Builds a hidden unit taking the provided number of
  * inputs.
  *
  * @param units  The input units to this unit
  */
class HiddenUnit(var units: Vector[Neuron]) extends Neuron {
  // the array of input weights
  private val _weights: Array[Double] = Array[Double](units.length)
  def weights: Array[Double] = _weights
  randomizeWeights()

  /**
    * Builds a hidden unit taking the provided number of
    * inputs.  Sets the initial weights to be a copy of
    * the provided weights
    *
    * @param units   The input units to this unit
    * @param weights The weights to use
    */
  def this(units: Vector[Neuron], weights: Vector[Double]) {
    this(units)
    Array.copy(weights, 0, this._weights, 0, weights.length)
  }

  /**
    * Generates a new weight
    */
  def randomizeWeights(): Unit = {
    ThreadLocalRandom.current().nextDouble(HiddenUnit.MIN_INITIAL_WEIGHT, HiddenUnit.MAX_INITIAL_WEIGHT)
    _weights.transform(
      _ => ThreadLocalRandom.current().nextDouble(HiddenUnit.MIN_INITIAL_WEIGHT, HiddenUnit.MAX_INITIAL_WEIGHT)
    )
  }

  /**
    * Returns the sum of all of the inputs and weights
    *
    * @return the sum
    */
  def sum: Double = {
    _weights.zip(units).map { case (w, u) => w * u.value }.sum
  }

  /**
    * Recomputes the value of this hidden unit, querying it's
    * prior inputs.
    */
  override def recompute(): Unit = {
    _value = HiddenUnit.sigmoid(sum)
  }

  def gradient: Double = value * (1.0 - value)
}
