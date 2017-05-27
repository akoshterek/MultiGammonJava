package org.akoshterek.backgammon.agent.gnubg

import com.google.common.io.LittleEndianDataInputStream
import resource.managed

/**
  * Created by Alex on 03-05-17.
  */
object Escapes {
  private val anEscapes0: Array[Int] = loadTable("/org/akoshterek/backgammon/agent/gnubg/escapes0.dat")
  private val anEscapes1: Array[Int] = loadTable("/org/akoshterek/backgammon/agent/gnubg/escapes1.dat")

  private def loadTable(resource: String): Array[Int] = {
    managed(new LittleEndianDataInputStream(
      classOf[GnubgAgent].getResourceAsStream(resource)))
    .acquireAndGet(is => {
      for(_ <- 0 until 0x1000) yield is.readInt
    }).toArray
  }

  def escapes0(anBoard: Array[Int], n: Int): Int = {
    escapes(anBoard, n, anEscapes0)
  }

  def escapes1(anBoard: Array[Int], n: Int): Int = {
    escapes(anBoard, n, anEscapes1)
  }

  def escapes(anBoard: Array[Int], n: Int, anEscapes: Array[Int]): Int = {
    val m = if (n < 12) n else 12
    var af = 0
    var i = 0
    while (i < m) {
      if (anBoard(24 + i - n) > 1) {
        af |= (1 << i)
      }
      i += 1
    }

    anEscapes(af)
  }
}
