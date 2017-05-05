package org.akoshterek.backgammon.data

import java.io.InputStream
import java.util.zip.GZIPInputStream

import org.akoshterek.backgammon.Constants
import resource.managed

import scala.collection.mutable.ListBuffer
import scala.io.Source

/**
  * @author Alex
  *         date 21.09.2015.
  */
object TrainDataLoader {
  private def loadData(is: InputStream): ListBuffer[TrainEntry] = {
    managed(Source.fromInputStream(is)).acquireAndGet(source => {
      val data: ListBuffer[TrainEntry] = ListBuffer()
      for (line <- source.getLines()) {
        val tokens: Array[String] = line.split("\\s")
        val reward = for (i <- 0 until Constants.NUM_OUTPUTS) yield tokens(i + 1).toDouble
        val entry: TrainEntry = new TrainEntry(tokens(0), reward.toArray)
        data += entry
      }
      data
    })
  }

  def loadGzipResourceData(resource: String): ListBuffer[TrainEntry] = {
    managed(new GZIPInputStream(classOf[TrainEntry].getResourceAsStream(resource)))
      .acquireAndGet(is => {
        loadData(is)
      })
  }
}