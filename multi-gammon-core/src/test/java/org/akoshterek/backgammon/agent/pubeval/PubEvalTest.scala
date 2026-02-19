package org.akoshterek.backgammon.agent.pubeval

import org.junit.Assert._
import org.junit.Test

class PubEvalTest {
  
  /**
   * Test that vectorized implementation produces same results as manual loop
   * for contact positions
   */
  @Test def testContactEvaluation(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    // Starting position
    val startPos = Array(
      0,  // opponent bar
      -2, 0, 0, 0, 0, 5,   // points 1-6
      0, 3, 0, 0, 0, -5,   // points 7-12
      5, 0, 0, 0, -3, 0,   // points 13-18
      -5, 0, 0, 0, 0, 2,   // points 19-24
      0,  // player bar
      0,  // player off
      0   // opponent off
    )
    
    val score = pubeval.evaluate(0, startPos)
    
    // Verify it produces a reasonable score (not 99999999)
    assertTrue("Score should be reasonable", score < 10.0 && score > -10.0)
    
    // Manually compute expected score to verify correctness
    val expected = manualEvaluate(startPos, pubeval.contactWeights)
    assertEquals("Vectorized result should match manual calculation", 
                 expected, score, 1e-5)
  }
  
  /**
   * Test that vectorized implementation produces same results for race positions
   */
  @Test def testRaceEvaluation(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    // Simple race position (no contact)
    val racePos = Array(
      0,  // opponent bar
      0, 0, 0, 0, 0, 0,    // points 1-6
      0, 0, 0, 0, 0, 0,    // points 7-12
      0, 0, 0, 3, 4, 2,    // points 13-18
      3, 2, 1, 0, 0, 0,    // points 19-24
      0,  // player bar
      0,  // player off
      -15 // opponent off (all opponent checkers off)
    )
    
    val score = pubeval.evaluate(1, racePos) // race=1
    
    assertTrue("Score should be reasonable", score < 10.0 && score > -10.0)
    
    val expected = manualEvaluate(racePos, pubeval.raceWeights)
    assertEquals("Vectorized result should match manual calculation", 
                 expected, score, 1e-5)
  }
  
  /**
   * Test special case: all checkers off
   */
  @Test def testAllCheckersOff(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    val allOffPos = Array.fill(28)(0)
    allOffPos(26) = 15  // all player checkers off
    
    val score = pubeval.evaluate(1, allOffPos)
    
    assertEquals("All off should return maximum score", 99999999.0, score, 0.0)
  }
  
  /**
   * Test with empty board (edge case)
   */
  @Test def testEmptyBoard(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    val emptyPos = Array.fill(28)(0)
    
    val contactScore = pubeval.evaluate(0, emptyPos)
    val raceScore = pubeval.evaluate(1, emptyPos)
    
    assertEquals("Empty board contact score should be 0", 0.0, contactScore, 1e-5)
    assertEquals("Empty board race score should be 0", 0.0, raceScore, 1e-5)
  }
  
  /**
   * Test with multiple positions to ensure consistency
   */
  @Test def testMultiplePositions(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    // Test 10 different random-ish positions
    val testPositions = generateTestPositions(10)
    
    for ((pos, isRace) <- testPositions) {
      val score = pubeval.evaluate(isRace, pos)
      val expected = manualEvaluate(pos, 
        if (isRace == 1) pubeval.raceWeights else pubeval.contactWeights)
      
      assertEquals(s"Position evaluation mismatch (race=$isRace)", 
                   expected, score, 1e-4)
    }
  }
  
  /**
   * Test that SIMD and scalar paths produce identical results
   */
  @Test def testSIMDvsScalar(): Unit = {
    val pubeval = new PubEval(
      PubEvalDefaultWeights.contactWeights,
      PubEvalDefaultWeights.raceWeights
    )
    
    val testPos = Array(
      0, -2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, -5,
      5, 0, 0, 0, -3, 0, -5, 0, 0, 0, 0, 2, 0, 0, 0
    )
    
    // Get vectorized (SIMD) result from PubEval
    val simdScore = pubeval.evaluate(0, testPos)
    
    // Compute scalar result manually (reference implementation)
    val scalarScore = manualEvaluate(testPos, pubeval.contactWeights)
    
    assertEquals("SIMD and scalar results should match", simdScore, scalarScore, 1e-5)
    
    // Also test with race weights
    val racePos = Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 2, 3, 4, 3, 2, 1, 0, 0, -15
    )
    
    val simdRaceScore = pubeval.evaluate(1, racePos)
    val scalarRaceScore = manualEvaluate(racePos, pubeval.raceWeights)
    
    assertEquals("SIMD and scalar race results should match", simdRaceScore, scalarRaceScore, 1e-5)
  }
  
  // Helper methods
  
  /**
   * Manual implementation of PubEval scoring (pre-vectorization reference)
   */
  private def manualEvaluate(pos: Array[Int], weights: Array[Float]): Double = {
    if (pos(26) == 15) {
      return 99999999.0
    }
    
    val x = new Array[Float](122)
    
    // Encode position (same as setx in PubEval)
    for (j <- 1 to 24) {
      val jm1 = j - 1
      val n = pos(25 - j)
      if (n != 0) {
        if (n == -1) x(5 * jm1) = 1.0f
        if (n == 1) x(5 * jm1 + 1) = 1.0f
        if (n >= 2) x(5 * jm1 + 2) = 1.0f
        if (n == 3) x(5 * jm1 + 3) = 1.0f
        if (n >= 4) x(5 * jm1 + 4) = (n - 3) / 2.0f
      }
    }
    x(120) = -pos(0).toFloat / 2.0f
    x(121) = pos(26).toFloat / 15.0f
    
    // Manual dot product
    var score = 0.0f
    var i = 0
    while (i < 122) {
      score += x(i) * weights(i)
      i += 1
    }
    score
  }
  
  /**
   * Generate test positions with varying characteristics
   */
  private def generateTestPositions(count: Int): Seq[(Array[Int], Int)] = {
    val positions = scala.collection.mutable.ArrayBuffer[(Array[Int], Int)]()
    
    // Position 1: Heavy contact
    positions += ((Array(
      0, -5, 0, 0, 0, 0, 3, 0, 3, 0, 0, 0, -5,
      5, 0, 0, 0, -3, 0, -2, 0, 0, 0, 0, 2, 0, 0, 0
    ), 0))
    
    // Position 2: Late race
    positions += ((Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 2, 3, 4, 3, 2, 1, 0, 0, -15
    ), 1))
    
    // Position 3: Opponent on bar
    positions += ((Array(
      -3, -2, 0, 0, 0, 0, 5, 0, 3, 0, 0, 0, 0,
      5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0
    ), 0))
    
    // Position 4: Player ahead in race
    positions += ((Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 10, 0, 0, -15
    ), 1))
    
    // Position 5: Balanced mid-game
    positions += ((Array(
      0, -1, 0, -2, 0, 0, 4, 0, 2, 0, 0, 0, -4,
      4, 0, 0, 0, -3, 0, -3, 0, 0, 0, 0, 3, 0, 1, -1
    ), 0))
    
    // Position 6: Nearly complete race
    positions += ((Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 1, 2, 2, 0, 0, 0, 10, -15
    ), 1))
    
    // Position 7: Heavy blitz
    positions += ((Array(
      -2, -3, 0, 0, 0, 0, 6, 0, 4, 0, 0, 0, -5,
      3, 0, 0, 0, -2, 0, -3, 0, 0, 0, 0, 2, 0, 0, 0
    ), 0))
    
    // Position 8: Bearing off
    positions += ((Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 3, 4, 5, 2, 1, 0, 0, 0, -10
    ), 1))
    
    // Position 9: Complex contact
    positions += ((Array(
      -1, -2, -1, 0, 0, 0, 3, 0, 2, 0, 0, -2, -3,
      4, 0, 0, -1, -2, 0, -2, 0, 0, 0, 1, 3, 0, 2, -1
    ), 0))
    
    // Position 10: Close race
    positions += ((Array(
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 1, 2, 3, 4, 3, 2, 0, 0, 0, -13
    ), 1))
    
    positions.take(count).toSeq
  }
}
