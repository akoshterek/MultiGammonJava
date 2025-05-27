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
}
