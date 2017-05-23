package org.akoshterek.backgammon.bearoff

import java.nio.{ByteBuffer, ByteOrder}
import java.util

import com.google.common.primitives.{Ints, Shorts}
import org.akoshterek.backgammon.Constants._
import org.akoshterek.backgammon.board.{Board, PositionId}
import org.akoshterek.backgammon.eval.Reward

/**
  * Created by Alex on 20-05-17.
  */
object Bearoff {
  def isBearoff(pbc: BearoffContext, board: Board): Boolean = {
    val anBoard = board.anBoard

    val nOppBack = Math.max(0, board.backChequerIndex(Board.OPPONENT))
    val nBack = Math.max(0, board.backChequerIndex(Board.SELF))
    if (board(Board.OPPONENT)(nOppBack) == 0 || anBoard(Board.SELF)(nBack) == 0) {
      // the game is over
      false
    } else if (nBack + nOppBack > 22) {
      // contact position
      false
    } else {
      val (nOpp, n) = board.chequersCount

      n <= pbc.chequers && nOpp <= pbc.chequers && nBack < pbc.points && nOppBack < pbc.points
    }
  }

  def bearoffEval(pbc: BearoffContext, anBoard: Board): Reward = {
    if (pbc.isTwoSided)
      bearoffEvalTwoSided(pbc, anBoard)
    else
      bearoffEvalOneSided(pbc, anBoard)
  }

  private def bearoffEvalTwoSided(pbc: BearoffContext, anBoard: Board) = {
    val nUs = PositionId.positionBearoff(anBoard.anBoard(1), pbc.points, pbc.chequers)
    val nThem = PositionId.positionBearoff(anBoard.anBoard(0), pbc.points, pbc.chequers)
    val n = PositionId.combination(pbc.points + pbc.chequers, pbc.points)
    val iPos = nUs * n + nThem
    val ar = new Array[Float](4)

    ReadTwoSidedBearoff(pbc, iPos, ar)
    val reward = Reward.rewardArray[Float]
    reward(OUTPUT_WIN) = ar(0) / 2.0f + 0.5f
    Reward(reward)
  }

  /**
    * BEAROFF_GNUBG: read two sided bearoff database
    **/
  private def ReadTwoSidedBearoff(pbc: BearoffContext, iPos: Int, ar: Array[Float]) = {
    val k = if (pbc.isCubeful) 4 else 1
    var us = 0

    val ac = pbc.readBearoffData(40 + 2 * iPos * k, k * 2)
    // add to cache

    var i = 0
    while (i < k) {
      val ac1 = ac(2 * i).toInt & 0xff
      val ac2 = ac(2 * i + 1).toInt & 0xff
      us = ac1 | ac2 << 8
      ar(i) = us / 32767.5f - 1.0f
      i += 1
    }
  }

  private def bearoffEvalOneSided(pbc: BearoffContext, anBoard: Board) = {
    val aarProb = Array.ofDim[Float](2, 32)
    val aarGammonProb = Array.ofDim[Float](2, 32)
    val anOn = new Array[Int](2)
    val an = new Array[Int](2)
    val ar = Array.ofDim[Float](2, 4)

    // get bearoff probabilities
    for (i <- 0 until 2) {
      an(i) = PositionId.positionBearoff(anBoard.anBoard(i), pbc.points, pbc.chequers)
      bearoffDist(pbc, an(i), aarProb(i), aarGammonProb(i), ar(i), null)
    }

    // calculate winning chance
    var r: Float = 0
    for (i <- 0 until 32;
         j <- i until 32) {
        r += aarProb(1)(i) * aarProb(0)(j)
    }

    val arOutput = Reward.rewardArray[Float]
    arOutput(OUTPUT_WIN) = r

    // calculate gammon chances
    anOn(Board.OPPONENT) = anBoard.chequersCount(Board.OPPONENT)
    anOn(Board.SELF) = anBoard.chequersCount(Board.SELF)

    if (anOn(0) == 15 || anOn(1) == 15) {
      if (pbc.isWithGammonProbs) {
        // my gammon chance: I'm out in i rolls and my opponent isn't inside
        //home quadrant in less than i rolls
        var r: Float = 0
        for (i <- 0 until 32;
             j <- i until 32) {
          r += aarProb(1)(i) * aarGammonProb(0)(j)
        }

        arOutput(OUTPUT_WINGAMMON) = r

        // opp gammon chance
        r = 0
        for (i <- 0 until 32;
             j <- i + 1 until 32) {
            r += aarProb(0)(i) * aarGammonProb(1)(j)
        }

        arOutput(OUTPUT_LOSEGAMMON) = r
      }
      else {
        throw new IllegalArgumentException("Invalid bearoff database")
      }
    }
    else { // no gammons possible
      arOutput(OUTPUT_WINGAMMON) = 0
      arOutput(OUTPUT_LOSEGAMMON) = 0
    }

    // no backgammons possible
    arOutput(OUTPUT_LOSEBACKGAMMON) = 0
    arOutput(OUTPUT_WINBACKGAMMON) = 0
    Reward(arOutput)
  }

  def bearoffDist(pbc: BearoffContext,
                  nPosID: Int,
                  arProb: Array[Float],
                  arGammonProb: Array[Float],
                  ar: Array[Float],
                  ausProb: Array[Short]): Unit = {
    require (!pbc.isTwoSided, "Invalid bearoff database")
    if (pbc.isNormalDistribution) {
      readBearoffOneSidedND(pbc, nPosID, arProb, arGammonProb, ar, ausProb)
    }
    else {
      readBearoffOneSidedExact(pbc, nPosID, arProb, arGammonProb, ar, ausProb)
    }
  }

  private def fnd(x: Float, mu: Float, sigma: Float) = {
    val epsilon = 1.0e-7f
    if (sigma <= epsilon) {
      // dirac delta function
      if (Math.abs(mu - x) < epsilon) 1.0f else 0.0f
    }
    else {
      val xm = (x - mu) / sigma
      1.0f / ((sigma * Math.sqrt(2.0 * Math.PI).toFloat) * Math.exp(-xm * xm / 2.0).toFloat)
    }
  }

  private def readBearoffOneSidedND(pbc: BearoffContext,
                                    nPosID: Int,
                                    arProb: Array[Float],
                                    arGammonProb: Array[Float],
                                    ar: Array[Float],
                                    ausProb: Array[Short]) = {
    val ac = pbc.readBearoffData(40 + nPosID * 16, 16)
    val bb = ByteBuffer.wrap(ac)
    bb.order(ByteOrder.LITTLE_ENDIAN)
    val arx = bb.asFloatBuffer.array

    if (arProb != null || ausProb != null) {
      (0 until 32).foreach {i =>
        val r = fnd(1.0f * i, arx(0), arx(1))
        if (arProb != null) arProb(i) = r
        if (ausProb != null) ausProb(i) = ((r * 65535.0f).toInt & 0xffff).toShort
      }
    }


    if (arGammonProb != null) {
      for (i <- 0 until 32) {
        val r = fnd(1.0f * i, arx(2), arx(3))
        arGammonProb(i) = r
      }
    }

    if (ar != null) System.arraycopy(arx, 0, ar, 0, 4)
  }

  private def readBearoffOneSidedExact(pbc: BearoffContext,
                                       nPosID: Int,
                                       arProb: Array[Float],
                                       arGammonProb: Array[Float],
                                       ar: Array[Float],
                                       ausProb: Array[Short]) = {
    val aus = new Array[Short](64)
    // get distribution
    if (pbc.isCompressed) {
      readDistCompressed(aus, pbc, nPosID)
    }
    else {
      readDistUncompressed(aus, pbc, nPosID)
    }

    assignOneSided(arProb, arGammonProb, ar, ausProb, aus)
  }

  private def assignOneSided(arProb: Array[Float], arGammonProb: Array[Float], ar: Array[Float], ausProb: Array[Short], ausProbx: Array[Short]) = {
    val arx = new Array[Float](64)
    if (ausProb != null) {
      System.arraycopy(ausProbx, 0, ausProb, 0, 32)
    }

    if (ar != null || arProb != null || arGammonProb != null) {
      (0 until 64).foreach(i => arx(i) = (ausProbx(i) & 0xffff) / 65535.0f)

      if (arProb != null) {
        System.arraycopy(arx, 0, arProb, 0, 32)
      }
      if (arGammonProb != null) {
        System.arraycopy(arx, 32, arGammonProb, 0, 32)
      }
      if (ar != null) {
        averageRolls(arx, 0, ar, 0)
        averageRolls(arx, 32, ar, 2)
      }
    }
  }

  private def averageRolls(arProb: Array[Float], probOffset: Int, ar: Array[Float], arrOffset: Int) = {
    var sx: Float = 0
    var sx2: Float = 0
    (1 until 32).foreach(i => {
      val p = i * arProb(probOffset + i)
      sx += p
      sx2 += i * p
    })

    ar(arrOffset) = sx
    ar(arrOffset + 1) = Math.sqrt(sx2 - sx * sx).toFloat
  }

  private def readDistCompressed(aus: Array[Short], pbc: BearoffContext, nPosID: Int) = {
    var iOffset = 0
    var nBytes = 0
    var ioff = 0
    var ioffg = 0
    var nzg = 0
    val nPos = PositionId.combination(pbc.points + pbc.chequers, pbc.points)
    val index_entry_size = if (pbc.isWithGammonProbs) 8 else 6

    // find offsets and no. of non-zero elements
    var ac = pbc.readBearoffData(40 + nPosID * index_entry_size, index_entry_size)

    // find offset (LE byte order)
    iOffset = Ints.fromBytes(ac(3), ac(2), ac(1), ac(0))

    val nz = ac(4)
    ioff = ac(5)
    if (pbc.isWithGammonProbs) {
      nzg = ac(6)
      ioffg = ac(7)
    }

    // Sanity checks
    require (!((iOffset > 64 * nPos && 64 * nPos > 0)
      || nz > 32 || ioff > 32 || nzg > 32 || ioffg > 32),
      "The bearoff database is likely to be corrupted.")

    // read prob + gammon probs
    iOffset = (40 // the header
      + nPos * index_entry_size // the offset data
      + 2 * iOffset // offset to current position
      )

    // read values
    nBytes = 2 * (nz + nzg)

    // get distribution
    ac = pbc.readBearoffData(iOffset, nBytes)
    copyBytes(aus, ac, nz, ioff, nzg, ioffg)
  }

  private def readDistUncompressed(aus: Array[Short], pbc: BearoffContext, nPosID: Int): Unit = {
    // read from buffer
    val iOffset = 40 + 64 * nPosID * (if (pbc.isWithGammonProbs) 2 else 1)
    val ac = pbc.readBearoffData(iOffset, if (pbc.isWithGammonProbs) 128 else 64)
    copyBytes(aus, ac, 32, 0, 32, 0)
  }

  private def copyBytes(aus: Array[Short],
                        ac: Array[Byte],
                        nz: Int, ioff:
                        Int, nzg: Int,
                        ioffg: Int): Unit = {
    var i: Int = 0
    util.Arrays.fill(aus, 0, 64, 0.toShort)

    var j: Int = 0
    while (j < nz) {
      aus(ioff + j) = Shorts.fromBytes(ac(i + 1), ac(i))
      j += 1
      i += 2
    }

    j = 0
    while (j < nzg) {
      aus(32 + ioffg + j) = Shorts.fromBytes(ac(i + 1), ac(i))
      j += 1
      i += 2
    }
  }
}
