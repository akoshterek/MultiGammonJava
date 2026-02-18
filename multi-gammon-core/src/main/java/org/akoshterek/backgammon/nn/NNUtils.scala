package org.akoshterek.backgammon.nn

import scala.util.Random

object NNUtils {
  // He initialization
  def heInit(fanIn: Int): Float = {
    val stdDev = math.sqrt(2.0 / fanIn).toFloat
    (Random.nextGaussian() * stdDev).toFloat
  }

  // Xavier initialization
  def xavierInit(fanIn: Int, fanOut: Int): Float = {
    val limit = math.sqrt(6.0 / (fanIn + fanOut)).toFloat
    // Uniform between [-limit, limit]
    (Random.nextFloat() * 2 * limit) - limit
  }

  def analyzeWeights(allWeights: Array[Float]): WeightStatistics = {
    if (allWeights.isEmpty) {
      return WeightStatistics(0f, 0f, 0f, 0, 0, allWeights.length)
    }

    val mean = allWeights.sum / allWeights.length
    val variance = allWeights.map(w => (w - mean) * (w - mean)).sum / allWeights.length
    val stdDev = math.sqrt(variance).toFloat
    val maxAbs = allWeights.map(_.abs).max

    val nearZero = allWeights.count(w => math.abs(w) < 0.01f)
    val large = allWeights.count(w => math.abs(w) > 5.0f)

    WeightStatistics(mean, stdDev, maxAbs, nearZero, large, allWeights.length)
  }
}
