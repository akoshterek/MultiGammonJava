package org.akoshterek.backgammon.agent.gnubg.nn

import java.io.{DataInput, IOException}

// While loops are much faster than for loops
object NeuralNet {
  @throws[IOException]
  def loadBinary(inputStream: DataInput): NeuralNet = {
    val cInput: Int = inputStream.readInt
    val cHidden: Int = inputStream.readInt
    val cOutput: Int = inputStream.readInt
    val nTrained: Int = inputStream.readInt
    val rBetaHidden: Float = inputStream.readFloat
    val rBetaOutput: Float = inputStream.readFloat
    require(!(cInput < 1 || cHidden < 1 || cOutput < 1 || nTrained < 0 || rBetaHidden <= 0.0 || rBetaOutput <= 0.0), "Invalid NN file")

    val nn: NeuralNet = new NeuralNet(cInput, cHidden, cOutput, rBetaHidden, rBetaOutput)
    readArray(nn.arHiddenWeight, inputStream)
    readArray(nn.arOutputWeight, inputStream)
    readArray(nn.arHiddenThreshold, inputStream)
    readArray(nn.arOutputThreshold, inputStream)
    nn
  }

  @throws[IOException]
  private def readArray(arr: Array[Float], inputStream: DataInput): Unit = {
    var i: Int = 0
    while (i < arr.length) {
      arr(i) = inputStream.readFloat
      i += 1
    }
  }
}

final class NeuralNet private(val cInput: Int, val cHidden: Int, val cOutput: Int, val rBetaHidden: Float, val rBetaOutput: Float) {
  private val arHiddenWeight: Array[Float] = new Array[Float](cHidden * cInput)
  private val arOutputWeight: Array[Float] = new Array[Float](cOutput * cHidden)
  private val arHiddenThreshold: Array[Float] = new Array[Float](cHidden)
  private val arOutputThreshold: Array[Float] = new Array[Float](cOutput)

  def evaluate(arInput: Array[Float], arOutput: Array[Float]): Unit = {
    val ar: Array[Float] = arHiddenThreshold.clone
    evaluateHiddenNodes(ar, arInput)
    evaluateOutputNodes(ar, arOutput)
  }

  /**
    * Calculates activity at hidden nodes
    *
    * @param ar      internal results
    * @param arInput input array
    */
  def evaluateHiddenNodes(ar: Array[Float], arInput: Array[Float]): Unit = {
    var prWeight: Int = 0

    //arHiddenWeight
    {
      var i = 0
      while (i < cInput) {
        val ari = arInput(i)
        if (ari != 0) {
          var prIndex: Int = 0
          if (ari == 1.0f) {
            var j = 0
            while (j < cHidden) {
              ar(prIndex) += arHiddenWeight(prWeight)
              prIndex += 1
              prWeight += 1
              j += 1
            }
          }
          else {
            var j = 0
            while (j < cHidden) {
              ar(prIndex) += arHiddenWeight(prWeight) * ari
              prIndex += 1
              prWeight += 1
              j += 1
            }
          }
        }
        else {
          prWeight += cHidden
        }

        i += 1
      }
    }

    {
      var i = 0
      while (i < cHidden) {
        ar(i) = Sigmoid.sigmoid(-rBetaHidden * ar(i))
        i += 1
      }
    }
  }

  /**
    * Calculates activity at output nodes
    *
    * @param ar       internal results
    * @param arOutput output activity
    */
  def evaluateOutputNodes(ar: Array[Float], arOutput: Array[Float]): Unit = {

    var prWeight = 0
    //>arOutputWeight;
    var i = 0
    while (i < cOutput) {
      var r: Float = arOutputThreshold(i)

      var j: Int = 0
      while (j < cHidden) {
        r += ar(j) * arOutputWeight(prWeight)
        prWeight += 1
        j += 1
      }

      arOutput(i) = Sigmoid.sigmoid(-rBetaOutput * r)
      i += 1
    }
  }
}