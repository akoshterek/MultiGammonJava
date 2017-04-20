package org.akoshterek.backgammon.bearoff

import com.google.common.io.LittleEndianDataInputStream
import org.apache.commons.io.IOUtils

import resource.managed

/**
 * @author Alex
 *         date 31.08.2015.
 */
class BearoffContext {
    var isTwoSided: Boolean = _   // type of bearoff database
    var nPoints: Int = _          // number of points covered by database
    var nChequers: Int = _        // number of chequers for one-sided database
    var szFilename: String = _    // filename

    // one sided dbs
    var fCompressed: Boolean = _  // is database compressed?
    var fGammon: Boolean = _      // gammon probs included

    var fND: Boolean = _          // normal distibution instead of exact dist?
    /* two sided dbs */
    var fCubeful: Boolean = _     // cubeful equities included
    private var data: Array[Byte] = _ // pointer to data in memory

    def readBearoffData(offset: Int, buf: Array[Byte], nBytes: Int): Unit = {
      Array.copy(data, offset, buf, 0, nBytes)
    }

  def getnChequers: Int = nChequers

  def getnPoints: Int = nPoints

  def isfCubeful: Boolean = fCubeful

  def isfGammon: Boolean = fGammon

  def isfND: Boolean = fND

  def isfCompressed: Boolean = fCompressed

  def getSzFilename: String = szFilename
}

object BearoffContext {
  def bearoffInit(szFilename: String): BearoffContext = {
    val pbc = new BearoffContext()
    val sz = Array.ofDim[Byte](40)

    pbc.szFilename = szFilename

    managed(new LittleEndianDataInputStream(classOf[BearoffContext].getResourceAsStream(szFilename))).acquireAndGet(inputStream => {
      // read header
      inputStream.readFully(sz)
      // detect bearoff program
      val header = new String(sz, 0, 5, "UTF-8")
      if(!"gnubg".equals(header)) {
        throw new IllegalArgumentException("Unknown bearoff database")
      }

      // one sided or two sided?
      val _type = new String(sz, 6, 2, "UTF-8")
      _type match {
        case "TS" => pbc.isTwoSided = true
        case "OS" => pbc.isTwoSided = false
        case _ => throw new IllegalArgumentException("Illegal bearoff type " + _type)
      }

      // number of points
      val pointsStr = new String(sz, 9, 2, "UTF-8")
      pbc.nPoints = Integer.valueOf(pointsStr)
      if (pbc.nPoints < 1 || pbc.nPoints >= 24) {
        throw new IllegalArgumentException("illegal number of points " + pbc.nPoints)
      }

      // number of chequers
      val nChequersStr = new String(sz, 12, 2, "UTF-8")
      pbc.nChequers = Integer.valueOf(nChequersStr)
      if (pbc.nChequers < 1 || pbc.nChequers > 15) {
        throw new IllegalArgumentException("illegal number of chequers " + pbc.nChequers)
      }

      if (pbc.isTwoSided) {
        // options for two-sided dbs
        pbc.fCubeful = Integer.valueOf(new String(sz, 15, 1, "UTF-8")) != 0
      } else {
        // options for one-sided dbs
        pbc.fGammon = Integer.valueOf(new String(sz, 15, 1, "UTF-8")) != 0
        pbc.fCompressed = Integer.valueOf(new String(sz, 17, 1, "UTF-8")) != 0
        pbc.fND = Integer.valueOf(new String(sz, 19, 1, "UTF-8")) != 0
      }
    })

    pbc.data = BearoffContext.readBinaryData(szFilename)
    pbc
  }

  private def readBinaryData(szFilename: String): Array[Byte] = {
    managed(classOf[BearoffContext].getResourceAsStream(szFilename)).acquireAndGet(inputStream => {
      IOUtils.toByteArray(inputStream)
    })
  }
}
