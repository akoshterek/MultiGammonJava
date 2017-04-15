package org.akoshterek.backgammon.nn

import java.io.Serializable

trait Neuron extends Serializable {

  protected var _value: Double = _
  def value: Double = _value
  /**
   * Recomputes the value of this hidden unit, querying it's
   * prior inputs.
   */
  def recompute(): Double
}