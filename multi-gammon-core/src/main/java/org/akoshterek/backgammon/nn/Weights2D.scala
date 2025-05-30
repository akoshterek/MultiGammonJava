package org.akoshterek.backgammon.nn

/**
 * Holds neural network weights with deep copies of arrays
 *
 * @param inputSize     The size of the input layer
 * @param hiddenSize    The size of the hidden layer
 * @param outputSize    The size of the output layer
 * @param wInputHidden  The input-to-hidden weights to copy
 * @param wHiddenOutput The hidden-to-output weights to copy
 */
class Weights2D(val inputSize: Int,
                val hiddenSize: Int,
                val outputSize: Int,
                wInputHidden: Array[Array[Float]],
                wHiddenOutput: Array[Array[Float]]) {

  // Validate dimensions
  require(wInputHidden.length == hiddenSize,
    s"Input-hidden weight rows (${wInputHidden.length}) must match hiddenSize ($hiddenSize)")

  require(wInputHidden.headOption.map(_.length).getOrElse(0) == inputSize,
    s"Input-hidden weight columns (${wInputHidden.headOption.map(_.length).getOrElse(0)}) must match inputSize ($inputSize)")

  require(wHiddenOutput.length == outputSize,
    s"Hidden-output weight rows (${wHiddenOutput.length}) must match outputSize ($outputSize)")

  require(wHiddenOutput.headOption.map(_.length).getOrElse(0) == hiddenSize,
    s"Hidden-output weight columns (${wHiddenOutput.headOption.map(_.length).getOrElse(0)}) must match hiddenSize ($hiddenSize)")

  // Create deep copies of the weight arrays
  val inputHiddenWeights: Array[Array[Float]] = Array.ofDim[Float](hiddenSize, inputSize)
  val hiddenOutputWeights: Array[Array[Float]] = Array.ofDim[Float](outputSize, hiddenSize)

  // Copy the array contents
  copyWeights()

  // Copy weights from source arrays to internal arrays
  private def copyWeights(): Unit = {
    var h = 0
    while (h < hiddenSize) {
      Array.copy(wInputHidden(h), 0, inputHiddenWeights(h), 0, inputSize)
      h += 1
    }

    var o = 0
    while (o < outputSize) {
      Array.copy(wHiddenOutput(o), 0, hiddenOutputWeights(o), 0, hiddenSize)
      o += 1
    }
  }

  // Create a new copy of this Weights2D object
  def copy(): Weights2D = {
    new Weights2D(inputSize, hiddenSize, outputSize, inputHiddenWeights, hiddenOutputWeights)
  }
}
