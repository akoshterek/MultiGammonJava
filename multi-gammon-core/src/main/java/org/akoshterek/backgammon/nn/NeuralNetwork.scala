package org.akoshterek.backgammon.nn

import java.io._
import java.nio.file.Path

import scala.util.Random
import resource.managed

object NeuralNetwork {
  // the random number generator
  val random: Random = new Random()

  /**
    * Method which reads and returns a network from the given file
    *
    * @param path The file to read from
    */
  @throws(classOf[IOException])
  def readFrom(path: Path): NeuralNetwork = {
    managed(new ObjectInputStream(new FileInputStream(path.toFile)))
      .acquireAndGet(stream => {
        stream.readObject().asInstanceOf[NeuralNetwork]
      })
  }
}

/**
  * Builds a neural network with the given number of input
  * units, hidden units, and output units.  Thus, calling
  * <p>
  * new NeuralNetwork(10, new int[] {20, 5});
  * <p>
  * creates a neural network with 10 input units, a layer of
  * 20 hidden units, and then 5 output units.
  *
  * @param input  The number of input units
  * @param hidden The number of hidden units, as well as the number of layers
  */
class NeuralNetwork(input: Int, hidden: Array[Int]) extends Serializable with Cloneable {
  val alpha = 0.1
  val beta = 0.1
  val lambda = 0.7

  // the layers of the network
  // Input layer
  val _input: Array[InputUnit] = Array.fill[InputUnit](input)(new InputUnit)

  // Hidden layers
  val _hidden: Array[Array[HiddenUnit]] = Array.ofDim[Array[HiddenUnit]](hidden.length)
  for (layer <- hidden.indices) {
    val count = hidden(layer)
    this._hidden(layer) = layer match {
      case 0 => Array.fill[HiddenUnit](count)(new HiddenUnit(this._input.toVector, NeuralNetwork.random, HiddenUnit.sigmoid))
      case _ => Array.fill[HiddenUnit](count)(new HiddenUnit(this._hidden(layer - 1).toVector, NeuralNetwork.random, HiddenUnit.sigmoid))
    }
  }

  /**
    * Builds a neural network based on the provided network and
    * copies the weights of the provided network into the new one.
    *
    * @param net The network to base it off of
    */
  def this(net: NeuralNetwork) {
    this(net._input.length, for (layer <- net._hidden) yield layer.length)

    for (i <- net._hidden.indices;
         j <- net._hidden.indices) {
      j match {
        case 0 => this._hidden(i)(j) = new HiddenUnit(
          this._input.toVector,
          net._hidden(i)(j).weights.toVector,
          HiddenUnit.sigmoid)
        case _ => this._hidden(i)(j) = new HiddenUnit(
          this._hidden(i - 1).toVector,
          net._hidden(i)(j).weights.toVector,
          HiddenUnit.sigmoid)
      }
    }
  }

  def clone(that: NeuralNetwork): NeuralNetwork = new NeuralNetwork(that)

  /**
    * Calculates the network value given the provided input
    *
    * @param input The input to check
    * @return The network value from this input
    */
  def calculate(input: Array[Double]): Array[Double] = {
    require(_input.length == input.length, "Wrong input size")

    Array.copy(input, 0, _input, 0, input.length)

    // Calculate hidden output
    _hidden.foreach(
      l => l.foreach(h => h.recompute())
    )

    {
      for (j <- _hidden.last.indices) yield {
        _hidden.last(j).value
      }
    }.toArray
  }

  /**
    * Method which writes this network to the given file
    *
    * @param path The file to write to
    */
  @throws(classOf[IOException])
  def writeTo(path: Path) {
    managed(new ObjectOutputStream(new FileOutputStream(path.toFile)))
      .acquireAndGet(stream => {
        stream.writeObject(this)
        stream.flush()
      })
  }
}