package org.akoshterek.backgammon.util

object Normalizer {
  def toSmallerSigmoid(data: Array[Double]) : Array[Double] = {
    toSmallerSigmoid(data, data.length)
  }

  def toSmallerSigmoid(data: Array[Double], length: Int) : Array[Double] = {
    data.map(x => x * 0.98 + 0.01).take(length)
  }

  def fromSmallerSigmoid(data: Array[Double]) : Array[Double] = {
    fromSmallerSigmoid(data, data.length)
  }

  def fromSmallerSigmoid(data: Array[Double], length: Int) :  Array[Double] = {
    data.map(x => (x - 0.01) / 0.98).take(length)
  }
}