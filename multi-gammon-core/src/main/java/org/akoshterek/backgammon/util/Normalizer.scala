package org.akoshterek.backgammon.util

object Normalizer {
  def toSmallerSigmoid(data: Array[Double]) : Unit = {
    toSmallerSigmoid(data, data.length)
  }

  def toSmallerSigmoid(data: Array[Double], length: Int) : Unit = {
    for (i <- 0 until length) {
      data(i) = data(i) * 0.98 + 0.01
    }
  }

  def fromSmallerSigmoid(data: Array[Double]) : Unit = {
    fromSmallerSigmoid(data, data.length)
  }

  def fromSmallerSigmoid(data: Array[Double], length: Int) : Unit = {
    for (i <- 0 until length) {
      data(i) = (data(i) - 0.01) / 0.98
    }
  }
}