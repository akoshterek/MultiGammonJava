package org.akoshterek.backgammon.nn

case class WeightStatistics(
                             mean: Float,
                             stdDev: Float,
                             maxAbs: Float,
                             nearZeroCount: Int,
                             largeCount: Int,
                             totalCount: Int
                           ) {
  def prettyPrint: String = {
    val nearZeroPct = nearZeroCount.toFloat / totalCount * 100
    val largePct = largeCount.toFloat / totalCount * 100

    f"""Weight Statistics:
       |  Mean: $mean%.4f, StdDev: $stdDev%.4f, MaxAbs: $maxAbs%.4f
       |  Near-zero (<0.01): $nearZeroCount ($nearZeroPct%.1f%%)
       |  Large (>5.0): $largeCount ($largePct%.1f%%)""".stripMargin
  }

  def healthWarnings: List[String] = {
    var warnings = List.empty[String]
    if (maxAbs > 10.0f) warnings = warnings :+ "⚠️ WEIGHTS GROWING LARGE - possible instability"
    if (maxAbs < 0.5f) warnings = warnings :+ "⚠️ WEIGHTS TOO SMALL - possibly stuck in minima"
    if (nearZeroCount.toFloat / totalCount > 0.5f) warnings = warnings :+ "⚠️ >50% weights near-zero - network may be dying"
    warnings
  }
}
