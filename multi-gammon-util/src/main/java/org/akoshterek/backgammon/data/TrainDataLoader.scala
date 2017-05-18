package org.akoshterek.backgammon.data

import java.util.zip.GZIPInputStream

import org.akoshterek.backgammon.Constants
import resource.managed

import scala.io.Source

/**
  * @author Alex
  *         date 21.09.2015.
  */
object TrainDataLoader {
  def loadGzipResourceData(resource: String): List[TrainEntry] = {
    managed(Source.fromInputStream(new GZIPInputStream(classOf[TrainEntry].getResourceAsStream(resource))))
      .acquireAndGet(source => {
        var data: List[TrainEntry] = List()
        for (line <- source.getLines()) {
          val tokens: Array[String] = line.split("\\s")
          val reward = for (i <- 0 until Constants.NUM_OUTPUTS) yield tokens(i + 1).toDouble
          val entry: TrainEntry = new TrainEntry(tokens(0), reward.toArray)
          data = entry :: data
        }
        data
      })
  }
}