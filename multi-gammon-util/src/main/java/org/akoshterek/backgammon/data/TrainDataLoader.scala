package org.akoshterek.backgammon.data

import org.akoshterek.backgammon.Constants
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util
import java.util.zip.GZIPInputStream

import resource.managed

/**
  * @author Alex
  *         date 21.09.2015.
  */
class TrainDataLoader {

}

object TrainDataLoader {
    private def loadData(is: InputStream): util.List[TrainEntry] = {
        managed(new BufferedReader(new InputStreamReader(is))).acquireAndGet(reader => {
            try {
                val data: util.List[TrainEntry] = new util.ArrayList[TrainEntry]
                var line: String = null
                while ({line = reader.readLine(); line} != null) {
                    {
                        val tokens: Array[String] = line.split("\\s")
                        val entry: TrainEntry = new TrainEntry
                        entry.positionId_$eq(tokens(0))
                        var i: Int = 0
                        for (i <- 0 until Constants.NUM_OUTPUTS) {
                            entry.reward(i) = tokens(i + 1).toDouble
                        }
                        data.add(entry)
                    }
                }
                data
            }
            catch {
                case e: Exception => throw new RuntimeException(e)
            }
        })
    }

    def loadGzipResourceData(resource: String): util.List[TrainEntry] = {
        managed(new GZIPInputStream(classOf[TrainDataLoader].getResourceAsStream(resource)))
            .acquireAndGet(is => {
            try {
                return loadData(is)
            }
            catch {
                case e: Exception =>
                    throw new RuntimeException(e)
            }
        })
    }
}