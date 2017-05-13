package org.akoshterek.backgammon.data

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionId
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.AuchKey
import java.io._
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

import resource.managed

import scala.collection.{immutable, mutable}
import scala.io.Source

/**
  * @author Alex
  *         date 20.09.2015.
  */
object DataPrepare extends App {
  val inputFileName: String = args(0)
  val trainingData: Map[String, Reward] = loadTrainingData(inputFileName)
  val outputFileName: String = replaceLast(inputFileName, ".gz", "-processed.gz")
  saveTrainingData(trainingData, outputFileName)

  private def loadTrainingData(inputFileName: String): immutable.Map[String, Reward] = {
    val data: mutable.TreeMap[String, Reward] = mutable.TreeMap[String, Reward]()
    managed(new GZIPInputStream(new FileInputStream(inputFileName))).acquireAndGet(stream => {

      Source.fromInputStream(stream)
        .getLines()
        .filter(s => !s.startsWith("#"))
        .foreach(line => {
          val tokens: Array[String] = line.split("\\s")
          val positionId: String = PositionId.positionIDFromKey(AuchKey.fromNnPosition(tokens(0)))
          val reward: Array[Double] = Reward.rewardArray
          for (i <- 0 until Constants.NUM_OUTPUTS) {
            reward(i) = tokens(i + 1).toDouble
          }
          data += (positionId -> new Reward(reward))
        })
    })

    addMissedInversions(immutable.Map.empty ++ data)
  }

  private def saveTrainingData(data: immutable.Map[String, Reward], outputFileName: String): Unit = {
    managed(new PrintWriter(new GZIPOutputStream(new FileOutputStream(outputFileName)))).acquireAndGet(writer => {
      data.foreach { case (positionId, reward) =>
        writer.println(positionId + " " + reward.data.mkString(" "))
      }
    })
  }

  private def addMissedInversions(data: immutable.Map[String, Reward]): immutable.Map[String, Reward] = {
    val missed: mutable.Map[String, Reward] = new mutable.TreeMap[String, Reward]

    for (key <- data.keySet) {
      val board: Board = Board.positionFromID(key).swapSides
      val posRev: String = board.positionID
      if (!data.contains(posRev)) {
        val reward: Reward = data(key).invert
        missed += ((posRev, reward))
      }
    }
    data ++ missed
  }

  private def replaceLast(string: String, toReplace: String, replacement: String): String = {
    val pos: Int = string.lastIndexOf(toReplace)
    if (pos > -1) {
      string.substring(0, pos) + replacement + string.substring(pos + toReplace.length, string.length)
    }
    else {
      string
    }
  }
}