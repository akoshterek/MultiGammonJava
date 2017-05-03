package org.akoshterek.backgammon.agent.gnubg

import com.google.common.io.LittleEndianDataInputStream
import resource.managed

/**
  * Created by Alex on 03-05-17.
  */
object Escapes {
  val anEscapes0: Vector[Int] = loadTable("/org/akoshterek/backgammon/gnu/escapes0.dat")
  val anEscapes1: Vector[Int] = loadTable("/org/akoshterek/backgammon/gnu/escapes1.dat")

  private def loadTable(resource: String): Vector[Int] = {
    managed(new LittleEndianDataInputStream(
      classOf[GnubgAgent].getResourceAsStream(resource)))
    .acquireAndGet(is => {
      for(_ <- 0 until 0x1000) yield is.readInt
    }).toVector
  }

  def escapes0(anBoard: Array[Int], n: Int): Int = {
    escapes(anBoard, n, anEscapes0)
  }

  def escapes1(anBoard: Array[Int], n: Int): Int = {
    escapes(anBoard, n, anEscapes1)
  }

  def escapes(anBoard: Array[Int], n: Int, anEscapes: Vector[Int]): Int = {
    val m = if (n < 12) n else 12

    var af = 0
    for (i <- 0 until m if anBoard(24 + i - n) > 1) {
      af |= (1 << i)
    }

    anEscapes(af)
  }
}
