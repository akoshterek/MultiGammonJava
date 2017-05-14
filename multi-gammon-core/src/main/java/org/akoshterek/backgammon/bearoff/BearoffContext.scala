package org.akoshterek.backgammon.bearoff

import java.io.{IOException, InputStream}

import com.google.common.io.LittleEndianDataInputStream
import org.apache.commons.io.IOUtils
import resource.managed

/**
  * @author Alex
  *         date 31.08.2015.
  * @param isTwoSided type of bearoff database
  * @param points number of points covered by database
  * @param chequers number of chequers for one-sided database
  * @param isWithGammonProbs gammon probabilities included
  * @param isCubeful cubeful equities included
  * @param isCompressed is database compressed?
  * @param isNormalDistribution normal distribution instead of exact dist?
  * @param data pointer to data in memory
  */
final class BearoffContext private(val isTwoSided: Boolean,
                             val points: Int,
                             val chequers: Int,
                             val isWithGammonProbs: Boolean,
                             val isCubeful: Boolean,
                             val isCompressed: Boolean,
                             val isNormalDistribution: Boolean,
                             private val data: Array[Byte]) {

    def readBearoffData(offset: Int, nBytes: Int): Array[Byte] = {
      data.slice(offset, offset + nBytes)
    }
}

object BearoffContext {
  def apply(szFilename: String): BearoffContext = {
    managed(new LittleEndianDataInputStream(classOf[BearoffContext].getResourceAsStream(szFilename))).acquireAndGet(inputStream => {
      loadBearoffDatabase(inputStream)
    })
  }

  @throws[IOException]
  private def loadBearoffDatabase(inputStream: InputStream): BearoffContext = {
    // read header
    val data = IOUtils.toByteArray(inputStream)
    // detect bearoff program
    require("gnubg".equals(new String(data, 0, 5, "UTF-8")), "Unknown bearoff database")

    // one sided or two sided?
    val _type = new String(data, 6, 2, "UTF-8")
    val isTwoSided = _type match {
      case "TS" => true
      case "OS" => false
      case _ => throw new IllegalArgumentException("Illegal bearoff type " + _type)
    }

    // number of points
    val pointsStr = new String(data, 9, 2, "UTF-8")
    val nPoints = Integer.valueOf(pointsStr)
    require (nPoints >= 1 || nPoints < 24, "illegal number of points " + nPoints)

    // number of chequers
    val nChequersStr = new String(data, 12, 2, "UTF-8")
    val nChequers = Integer.valueOf(nChequersStr)
    require(nChequers >= 1 && nChequers <= 15, "illegal number of chequers " + nChequers)

    val fCubeful = if (isTwoSided) Integer.valueOf(new String(data, 15, 1, "UTF-8")) != 0 else false
    val fGammon = if (isTwoSided) false else Integer.valueOf(new String(data, 15, 1, "UTF-8")) != 0
    val fCompressed = if (isTwoSided) false else Integer.valueOf(new String(data, 17, 1, "UTF-8")) != 0
    val fND = if (isTwoSided) false else Integer.valueOf(new String(data, 19, 1, "UTF-8")) != 0

    new BearoffContext(isTwoSided, nPoints, nChequers, fGammon, fCubeful, fCompressed, fND, data)
  }
}
