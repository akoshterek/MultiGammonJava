package org.akoshterek.backgammon.nn

import java.io.Serializable

trait Neuron extends Serializable {

  protected var _value: Float = 0.0f
  def value: Float = _value
  /**
   * Recomputes the value of this hidden unit, querying it's
   * prior inputs.
   */
  def recompute(): Unit = {}
}