package org.akoshterek.backgammon.genetic

import org.akoshterek.backgammon.nn._
import scala.util.Random

/**
 * Simplified neural network for genetic algorithm training.
 * Only forward pass, no backpropagation or eligibility traces.
 */
class SimpleNeuralNetwork(val inputSize: Int,
                          val hiddenSize: Int,
                          val outputSize: Int,
                          val hiddenActivation: Activation = LeakyReLU,
                          val outputActivation: Activation = Sigmoid) {
  
  // Weight matrices
  val wInputHidden: Array[Array[Float]] = Array.fill(hiddenSize, inputSize)(NNUtils.heInit(inputSize))
  val wHiddenOutput: Array[Array[Float]] = Array.fill(outputSize, hiddenSize)(NNUtils.xavierInit(hiddenSize, outputSize))
  
  // Biases
  val bHidden: Array[Float] = Array.fill(hiddenSize)(0f)
  val bOutput: Array[Float] = Array.fill(outputSize)(0f)
  
  // Cache for forward pass
  private val hiddenRaw = Array.ofDim[Float](hiddenSize)
  private val hiddenActivated = Array.ofDim[Float](hiddenSize)
  
  /**
   * Forward pass through the network
   */
  def forward(input: Array[Float], output: Array[Float]): Unit = {
    // Compute hidden layer
    var h = 0
    while (h < hiddenSize) {
      var sum = DotProductUtils.dotProduct(wInputHidden(h), input, useSIMD = true)
      hiddenRaw(h) = sum + bHidden(h)
      hiddenActivated(h) = hiddenActivation.f(hiddenRaw(h))
      h += 1
    }
    
    // Compute output layer
    var o = 0
    while (o < outputSize) {
      val sum = DotProductUtils.dotProduct(wHiddenOutput(o), hiddenActivated, useSIMD = true)
      output(o) = outputActivation.f(sum + bOutput(o))
      o += 1
    }
  }
  
  /**
   * Evaluate single position (convenience method)
   */
  def evaluate(input: Array[Float]): Float = {
    val output = Array.ofDim[Float](outputSize)
    forward(input, output)
    output(0)
  }
  
  /**
   * Save weights to NetworkWeights format (compatible with TdNeuralNetwork)
   */
  def saveWeights(): NetworkWeights = {
    val wInputHiddenCopy = wInputHidden.map(_.clone())
    val wHiddenOutputCopy = wHiddenOutput.map(_.clone())
    val bHiddenCopy = bHidden.clone()
    val bOutputCopy = bOutput.clone()
    
    NetworkWeights(wInputHiddenCopy, wHiddenOutputCopy, bHiddenCopy, bOutputCopy)
  }
  
  /**
   * Load weights from NetworkWeights format (compatible with TdNeuralNetwork)
   */
  def loadWeights(weights: NetworkWeights): Unit = {
    // Validate dimensions
    require(weights.wInputHidden.length == hiddenSize && weights.wInputHidden(0).length == inputSize,
      s"wInputHidden dimension mismatch: expected ($hiddenSize, $inputSize), got (${weights.wInputHidden.length}, ${weights.wInputHidden(0).length})")
    
    require(weights.wHiddenOutput.length == outputSize && weights.wHiddenOutput(0).length == hiddenSize,
      s"wHiddenOutput dimension mismatch: expected ($outputSize, $hiddenSize), got (${weights.wHiddenOutput.length}, ${weights.wHiddenOutput(0).length})")
    
    require(weights.bHidden.length == hiddenSize,
      s"bHidden dimension mismatch: expected $hiddenSize, got ${weights.bHidden.length}")
    
    require(weights.bOutput.length == outputSize,
      s"bOutput dimension mismatch: expected $outputSize, got ${weights.bOutput.length}")
    
    // Copy weights
    for (h <- 0 until hiddenSize; i <- 0 until inputSize) {
      wInputHidden(h)(i) = weights.wInputHidden(h)(i)
    }
    
    for (o <- 0 until outputSize; h <- 0 until hiddenSize) {
      wHiddenOutput(o)(h) = weights.wHiddenOutput(o)(h)
    }
    
    for (h <- 0 until hiddenSize) {
      bHidden(h) = weights.bHidden(h)
    }
    
    for (o <- 0 until outputSize) {
      bOutput(o) = weights.bOutput(o)
    }
  }
  
  /**
   * Clone this network (deep copy of weights)
   */
  def cloneNetwork(): SimpleNeuralNetwork = {
    val cloned = new SimpleNeuralNetwork(inputSize, hiddenSize, outputSize, hiddenActivation, outputActivation)
    cloned.loadWeights(saveWeights())
    cloned
  }
  
  /**
   * Get statistics about weights
   */
  def analyzeWeights(): WeightStatistics = {
    val allWeights = wInputHidden.flatten ++ wHiddenOutput.flatten
    
    if (allWeights.isEmpty) {
      return WeightStatistics(0f, 0f, 0f, 0, 0, allWeights.length)
    }
    
    val mean = allWeights.sum / allWeights.length
    val variance = allWeights.map(w => (w - mean) * (w - mean)).sum / allWeights.length
    val stdDev = math.sqrt(variance).toFloat
    val maxAbs = allWeights.map(_.abs).max
    
    val nearZero = allWeights.count(w => math.abs(w) < 0.01f)
    val large = allWeights.count(w => math.abs(w) > 5.0f)
    
    WeightStatistics(mean, stdDev, maxAbs, nearZero, large, allWeights.length)
  }
}
