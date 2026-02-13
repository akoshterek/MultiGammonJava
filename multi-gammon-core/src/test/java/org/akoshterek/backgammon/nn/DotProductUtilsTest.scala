package org.akoshterek.backgammon.nn

import org.junit.Assert.assertTrue
import org.junit.Test


class DotProductUtilsTest {
  @Test def testSimd(): Unit = {
    val a = Array(1.0f, 2.0f, 3.0f, 4.0f)
    val b = Array(4.0f, 3.0f, 2.0f, 1.0f)

    val scalar = DotProductUtils.dotProduct(a, b, useSIMD = false)
    val simd = DotProductUtils.dotProduct(a, b, useSIMD = true)

    assertTrue("SIMD and scalar results differ!", math.abs(scalar - simd) < 1e-5)
  }
}
