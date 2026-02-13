package org.akoshterek.backgammon.nn

// Utility class to handle SIMD and scalar dot product
object DotProductUtils {

  import jdk.incubator.vector._

  private val FLOAT_SPECIES = FloatVector.SPECIES_PREFERRED

  def dotProduct(a: Array[Float], b: Array[Float], useSIMD: Boolean = true): Float = {
    if (useSIMD) {
      simdDot(a, b)
    } else {
      var sum = 0f
      var i = 0
      while (i < a.length) {
        sum += a(i) * b(i)
        i += 1
      }
      sum
    }
  }

  private def simdDot(a: Array[Float], b: Array[Float]): Float = {
    var i = 0
    var sumVec = FloatVector.zero(FLOAT_SPECIES)

    val upperBound = FLOAT_SPECIES.loopBound(a.length)

    while (i < upperBound) {
      val va = FloatVector.fromArray(FLOAT_SPECIES, a, i)
      val vb = FloatVector.fromArray(FLOAT_SPECIES, b, i)
      sumVec = sumVec.add(va.mul(vb))
      i += FLOAT_SPECIES.length()
    }

    var sum = sumVec.reduceLanes(VectorOperators.ADD)

    while (i < a.length) {
      sum += a(i) * b(i)
      i += 1
    }

    sum
  }
}