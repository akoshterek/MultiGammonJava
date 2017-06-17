package org.akoshterek.backgammon.nn

class InputUnit extends Neuron {
  def value_=(v: Float): Unit = {
    _value = v
  }
}