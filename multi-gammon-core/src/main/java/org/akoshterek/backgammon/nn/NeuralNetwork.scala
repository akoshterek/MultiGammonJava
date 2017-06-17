package org.akoshterek.backgammon.nn

import java.io._
import java.nio.file.Path

import resource.managed

object NeuralNetwork {
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
class NeuralNetwork(input: Int, hidden: Int, output: Int,
                    val hiddenActivation: Activation,
                    val outputActivation: Activation) extends Serializable with Cloneable {
  val alpha = 0.1f
  val beta = 0.1f
  val lambda = 0.7f
  val et: EligibilityTrace = createEligibilityTrace

  // the layers of the network
  // Input layer
  val _input: Array[InputUnit] = Array.fill[InputUnit](input)(new InputUnit)

  // Hidden layers
  val _hidden: Array[Array[HiddenUnit]] = Array.ofDim[Array[HiddenUnit]](2)
  _hidden(0) = Array.fill[HiddenUnit](hidden)(new HiddenUnit(this._input.toVector, hiddenActivation))

  //Output
  _hidden(1) = Array.fill[HiddenUnit](output)(new HiddenUnit(this._hidden(0).toVector, outputActivation))

  /**
    * Builds a neural network based on the provided network and
    * copies the weights of the provided network into the new one.
    *
    * @param net The network to base it off of
    */
  def this(net: NeuralNetwork) {
    this(net._input.length, net._hidden(0).length, net._hidden(1).length,
      net.hiddenActivation, net.outputActivation)

    for (i <- net._hidden.indices;
         j <- net._hidden(i).indices) {
      j match {
        case 0 => this._hidden(i)(j) = new HiddenUnit(
          this._input.toVector,
          net._hidden(i)(j).weights,
          net.hiddenActivation)
        case _ => this._hidden(i)(j) = new HiddenUnit(
          this._hidden(i - 1).toVector,
          net._hidden(i)(j).weights,
          net.outputActivation)
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
  def calculate(input: Array[Float]): Array[Float] = {
    require(_input.length == input.length, "Wrong input size")

    Array.copy(input, 0, _input, 0, input.length)

    // Calculate hidden output
    var l = 0
    while (l < _hidden.length) {
      val layer = _hidden(l)
      var h = 0
      while (h < layer.length) {
        layer(h).recompute()
        h += 1
      }
      l += 1
    }

    // Output
    val out = Array[Float](_hidden.last.length)
    var j = 0
    while (j < _hidden.last.length) {
      out(j) = _hidden.last(j).value
      j += 1
    }
    out
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

  private def createEligibilityTrace: EligibilityTrace = {
    new EligibilityTrace(_input.length, _hidden(0).length, _hidden(1).length)
  }

  def backpropWithEtraces(in: Array[Float], out: Array[Float], expected: Array[Float]): Unit = {
    require(this.input == in.length && this.output == out.length && this.output == expected.length, "Wrong dimensions")

    computeEligibilityTraces(in, _hidden(0).length, _hidden(1).length)
    val error = Array.tabulate[Float](output)(k => expected(k) - out(k))

    var j = 0
    while (j < _hidden(0).length) {
      var k = 0
      while (k < out.length) {
        // weight from j to k, shown with learning parameter BETA
        _hidden(1)(k).weights(j) += beta * error(k) * et.Ew(j)(k)

        var i = 0
        while (i < in.length) {
          // weight from i to j, shown with learning parameter ALPHA
          _hidden(0)(j).weights(i) += alpha * error(k) * et.Ev(i)(j)(k)
          i += 1
        }

        k += 1
      }

      j += 1
    }
  }

  private def computeEligibilityTraces(in: Array[Float], hidden: Int, output: Int): Unit = {
    var j = 0
    while (j < hidden) {
      var k = 0
      while (k < output) {
        // ew[j][k] = (lambda * ew[j][k]) + (gradient(k) * hidden[0][j])
        et.Ew(j)(k) = (lambda * et.Ew(j)(k)) + (_hidden(1)(k).gradient * _hidden(0)(j).value)

        var i = 0
        while (i < in.length) {
          // ev[i][j][k] = (lambda * ev[i][j][k]) +
          // (gradient(k) * w[j][k] * gradient(j) * input[i])
          et.Ev(i)(j)(k) = (lambda * et.Ev(i)(j)(k)) +
            (_hidden(1)(k).gradient * _hidden(1)(k).weights(j) * _hidden(0)(j).gradient) * in(i)
          i += 1
        }

        k += 1
      }

      j += 1
    }
  }

  final class EligibilityTrace(input: Int, hidden: Int, output: Int) {
    val Ew: Array[Array[Float]] = Array.fill[Float](hidden, output)(0)
    var Ev: Array[Array[Array[Float]]] = Array.fill[Float](input, hidden, output)(0)
  }
}
