package org.akoshterek.backgammon.agent.gnubg

/**
  * Created by Alex on 03-05-17.
  *  One way to hit
  */
class HitIntermediate(val fAll: Int, val anIntermediate: Array[Int], val nFaces: Int, val nPips: Int) {
  /** if true, all intermediate points (if any) are required;
    * if false, one of two intermediate points are required.
    * Set to true for a direct hit, but that can be checked with
    * nFaces == 1,
    */
  //val fAll = 0

  /** Intermediate points required */
  //val anIntermediate = null

  /** Number of faces used in hit (1 to 4) */
  //val nFaces = 0

  /** Number of pips used to hit */
  //val nPips = 0

}

object HitIntermediate {
  /* aanCombination[n] -
     How many ways to hit from a distance of n pips.
     Each number is an index into aIntermediate below.
  */
  val aanCombination: Array[Array[Int]] = Array[Array[Int]](
    Array(  0, -1, -1, -1, -1 ), /*  1 */
    Array(  1,  2, -1, -1, -1 ), /*  2 */
    Array(  3,  4,  5, -1, -1 ), /*  3 */
    Array(  6,  7,  8,  9, -1 ), /*  4 */
    Array( 10, 11, 12, -1, -1 ), /*  5 */
    Array( 13, 14, 15, 16, 17 ), /*  6 */
    Array( 18, 19, 20, -1, -1 ), /*  7 */
    Array( 21, 22, 23, 24, -1 ), /*  8 */
    Array( 25, 26, 27, -1, -1 ), /*  9 */
    Array( 28, 29, -1, -1, -1 ), /* 10 */
    Array( 30, -1, -1, -1, -1 ), /* 11 */
    Array( 31, 32, 33, -1, -1 ), /* 12 */
    Array( -1, -1, -1, -1, -1 ), /* 13 */
    Array( -1, -1, -1, -1, -1 ), /* 14 */
    Array( 34, -1, -1, -1, -1 ), /* 15 */
    Array( 35, -1, -1, -1, -1 ), /* 16 */
    Array( -1, -1, -1, -1, -1 ), /* 17 */
    Array( 36, -1, -1, -1, -1 ), /* 18 */
    Array( -1, -1, -1, -1, -1 ), /* 19 */
    Array( 37, -1, -1, -1, -1 ), /* 20 */
    Array( -1, -1, -1, -1, -1 ), /* 21 */
    Array( -1, -1, -1, -1, -1 ), /* 22 */
    Array( -1, -1, -1, -1, -1 ), /* 23 */
    Array( 38, -1, -1, -1, -1 )  /* 24 */
  )
  
  /* All ways to hit */ 
  val aIntermediate: Array[HitIntermediate] = Array[HitIntermediate](
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 1), /*  0: 1x hits 1 */ 
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 2), /*  1: 2x hits 2 */ 
    HitIntermediate(1, Array[Int](1, 0, 0), 2, 2), /*  2: 11 hits 2 */ 
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 3), /*  3: 3x hits 3 */ 
    HitIntermediate(0, Array[Int](1, 2, 0), 2, 3), /*  4: 21 hits 3 */ 
    HitIntermediate(1, Array[Int](1, 2, 0), 3, 3), /*  5: 11 hits 3 */ 
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 4), /*  6: 4x hits 4 */ 
    HitIntermediate(0, Array[Int](1, 3, 0), 2, 4), /*  7: 31 hits 4 */ 
    HitIntermediate(1, Array[Int](2, 0, 0), 2, 4), /*  8: 22 hits 4 */ 
    HitIntermediate(1, Array[Int](1, 2, 3), 4, 4), /*  9: 11 hits 4 */ 
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 5), /* 10: 5x hits 5 */ 
    HitIntermediate(0, Array[Int](1, 4, 0), 2, 5), /* 11: 41 hits 5 */ 
    HitIntermediate(0, Array[Int](2, 3, 0), 2, 5), /* 12: 32 hits 5 */ 
    HitIntermediate(1, Array[Int](0, 0, 0), 1, 6), /* 13: 6x hits 6 */ 
    HitIntermediate(0, Array[Int](1, 5, 0), 2, 6), /* 14: 51 hits 6 */ 
    HitIntermediate(0, Array[Int](2, 4, 0), 2, 6), /* 15: 42 hits 6 */ 
    HitIntermediate(1, Array[Int](3, 0, 0), 2, 6), /* 16: 33 hits 6 */ 
    HitIntermediate(1, Array[Int](2, 4, 0), 3, 6), /* 17: 22 hits 6 */ 
    HitIntermediate(0, Array[Int](1, 6, 0), 2, 7), /* 18: 61 hits 7 */ 
    HitIntermediate(0, Array[Int](2, 5, 0), 2, 7), /* 19: 52 hits 7 */ 
    HitIntermediate(0, Array[Int](3, 4, 0), 2, 7), /* 20: 43 hits 7 */ 
    HitIntermediate(0, Array[Int](2, 6, 0), 2, 8), /* 21: 62 hits 8 */ 
    HitIntermediate(0, Array[Int](3, 5, 0), 2, 8), /* 22: 53 hits 8 */ 
    HitIntermediate(1, Array[Int](4, 0, 0), 2, 8), /* 23: 44 hits 8 */ 
    HitIntermediate(1, Array[Int](2, 4, 6), 4, 8), /* 24: 22 hits 8 */ 
    HitIntermediate(0, Array[Int](3, 6, 0), 2, 9), /* 25: 63 hits 9 */ 
    HitIntermediate(0, Array[Int](4, 5, 0), 2, 9), /* 26: 54 hits 9 */ 
    HitIntermediate(1, Array[Int](3, 6, 0), 3, 9), /* 27: 33 hits 9 */ 
    HitIntermediate(0, Array[Int](4, 6, 0), 2, 10), /* 28: 64 hits 10 */ 
    HitIntermediate(1, Array[Int](5, 0, 0), 2, 10), /* 29: 55 hits 10 */ 
    HitIntermediate(0, Array[Int](5, 6, 0), 2, 11), /* 30: 65 hits 11 */ 
    HitIntermediate(1, Array[Int](6, 0, 0), 2, 12), /* 31: 66 hits 12 */ 
    HitIntermediate(1, Array[Int](4, 8, 0), 3, 12), /* 32: 44 hits 12 */ 
    HitIntermediate(1, Array[Int](3, 6, 9), 4, 12), /* 33: 33 hits 12 */ 
    HitIntermediate(1, Array[Int](5, 10, 0), 3, 15), /* 34: 55 hits 15 */ 
    HitIntermediate(1, Array[Int](4, 8, 12), 4, 16), /* 35: 44 hits 16 */ 
    HitIntermediate(1, Array[Int](6, 12, 0), 3, 18), /* 36: 66 hits 18 */ 
    HitIntermediate(1, Array[Int](5, 10, 15), 4, 20), /* 37: 55 hits 20 */ 
    HitIntermediate(1, Array[Int](6, 12, 18), 4, 24) /* 38: 66 hits 24 */
  )

  /** aaRoll[n] - All ways to hit with the n'th roll
    * Each entry is an index into aIntermediate above.
    */
  val aaRoll: Array[Array[Int]] = Array[Array[Int]](
    Array(0, 2, 5, 9), /* 11 */ 
    Array(0, 1, 4, -1), /* 21 */ 
    Array(1, 8, 17, 24), /* 22 */ 
    Array(0, 3, 7, -1), /* 31 */ 
    Array(1, 3, 12, -1), /* 32 */ 
    Array(3, 16, 27, 33), /* 33 */ 
    Array(0, 6, 11, -1), /* 41 */ 
    Array(1, 6, 15, -1), /* 42 */ 
    Array(3, 6, 20, -1), /* 43 */ 
    Array(6, 23, 32, 35), /* 44 */ 
    Array(0, 10, 14, -1), /* 51 */ 
    Array(1, 10, 19, -1), /* 52 */ 
    Array(3, 10, 22, -1), /* 53 */ 
    Array(6, 10, 26, -1), /* 54 */ 
    Array(10, 29, 34, 37), /* 55 */ 
    Array(0, 13, 18, -1), /* 61 */ 
    Array(1, 13, 21, -1), /* 62 */ 
    Array(3, 13, 25, -1), /* 63 */ 
    Array(6, 13, 28, -1), /* 64 */ 
    Array(10, 13, 30, -1), /* 65 */ 
    Array(13, 31, 36, 38) /* 66 */
  )

  def apply(fAll: Int, anIntermediate: Array[Int], nFaces: Int, nPips: Int): HitIntermediate = {
    new HitIntermediate(fAll, anIntermediate, nFaces, nPips)
  }
}