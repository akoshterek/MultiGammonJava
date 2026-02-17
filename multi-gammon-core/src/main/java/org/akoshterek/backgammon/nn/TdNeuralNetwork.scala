package org.akoshterek.backgammon.nn

// The core TD(λ) network class
class TdNeuralNetwork(inputSize: Int,
                      hiddenSize: Int,
                      outputSize: Int,
                      var alpha: Float = 0.01f,  // Changed to var to allow annealing
                      val lambda: Float = 0.7f,
                      val gamma: Float = 1.0f,
                      val hiddenActivation: Activation = LeakyReLU,
                      val outputActivation: Activation = Sigmoid,
                      var biasAlphaRatio: Float = 0.5f,  // Made var for adaptive learning
                      val gradientClipThreshold: Float = 5.0f,  // Clip extreme TD errors
                      val useOutputBias: Boolean = true  // Set to false to disable output bias (prevent drift)
                     ) {
  // Weight matrices
  val wInputHidden: Array[Array[Float]] = Array.fill(hiddenSize, inputSize)(NNUtils.heInit(inputSize))
  val wHiddenOutput: Array[Array[Float]] = Array.fill(outputSize, hiddenSize)(NNUtils.xavierInit(hiddenSize, outputSize))

  // Biases
  val bHidden: Array[Float] = Array.fill(hiddenSize)(0f)
  val bOutput: Array[Float] = Array.fill(outputSize)(0f)

  // Forward pass cache
  private val lastInput: Array[Float] = Array.ofDim[Float](inputSize)
  private val lastOutput: Array[Float] = Array.ofDim[Float](outputSize)
  private val clippedOutput: Array[Float] = Array.ofDim[Float](outputSize)

  private val hiddenRaw = Array.ofDim[Float](hiddenSize)
  private val hiddenActivated = Array.ofDim[Float](hiddenSize)

  // Learning pass cache
  private val error = Array.ofDim[Float](outputSize)
  private val gradOut = Array.ofDim[Float](outputSize)
  private val gradHidden = Array.ofDim[Float](hiddenSize)

  // TD(λ) learning statistics
  private var cumulativeTDError: Float = 0f
  private var tdErrorCount: Int = 0

  def createEligibilityTrace() = new EligibilityTrace2D(inputSize, hiddenSize, outputSize)

  def weightsCopy: Weights2D = new Weights2D(inputSize, hiddenSize, outputSize, wInputHidden, wHiddenOutput)

  def getAverageTDError(reset: Boolean = false): Float = {
    val avg = if (tdErrorCount > 0) cumulativeTDError / tdErrorCount else 0f
    if (reset) {
      cumulativeTDError = 0f
      tdErrorCount = 0
    }
    avg
  }

  def analyzeWeights(): WeightStatistics = {
    val allWeights = wInputHidden.flatten ++ wHiddenOutput.flatten
    val allBiases = bHidden ++ bOutput

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

  def saveWeights(): NetworkWeights = {
    // Deep copy arrays to prevent external modification
    val wInputHiddenCopy = wInputHidden.map(_.clone())
    val wHiddenOutputCopy = wHiddenOutput.map(_.clone())
    val bHiddenCopy = bHidden.clone()
    val bOutputCopy = bOutput.clone()

    NetworkWeights(wInputHiddenCopy, wHiddenOutputCopy, bHiddenCopy, bOutputCopy)
  }

  def loadWeights(weights: NetworkWeights): Unit = {
    // Validate dimensions
    if (weights.wInputHidden.length != hiddenSize ||
        weights.wInputHidden(0).length != inputSize) {
      throw new IllegalArgumentException(
        s"wInputHidden dimension mismatch: expected ($hiddenSize, $inputSize), " +
        s"got (${weights.wInputHidden.length}, ${weights.wInputHidden(0).length})"
      )
    }

    if (weights.wHiddenOutput.length != outputSize ||
        weights.wHiddenOutput(0).length != hiddenSize) {
      throw new IllegalArgumentException(
        s"wHiddenOutput dimension mismatch: expected ($outputSize, $hiddenSize), " +
        s"got (${weights.wHiddenOutput.length}, ${weights.wHiddenOutput(0).length})"
      )
    }

    if (weights.bHidden.length != hiddenSize) {
      throw new IllegalArgumentException(
        s"bHidden dimension mismatch: expected $hiddenSize, got ${weights.bHidden.length}"
      )
    }

    if (weights.bOutput.length != outputSize) {
      throw new IllegalArgumentException(
        s"bOutput dimension mismatch: expected $outputSize, got ${weights.bOutput.length}"
      )
    }

    // Copy weights into network
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

  // Forward pass (updates internal hiddenRaw and hiddenActivated)
  def forward(input: Array[Float], output: Array[Float]): Unit = {
    // Cache input for use in train() method
    Array.copy(input, 0, lastInput, 0, input.length.min(lastInput.length))
    computeHiddenLayer(input)
    computeOutputLayer(output)
    Array.copy(output, 0, lastOutput, 0, output.length.min(lastOutput.length));
  }

  private def computeHiddenLayer(input: Array[Float]): Unit = {
    var h = 0
    while (h < hiddenSize) {
      var sum = DotProductUtils.dotProduct(wInputHidden(h), input, useSIMD = true)
      hiddenRaw(h) = sum + bHidden(h)
      hiddenActivated(h) = hiddenActivation.f(hiddenRaw(h))
      h += 1
    }
  }

  private def computeOutputLayer(output: Array[Float]): Unit = {
    var o = 0
    while (o < outputSize) {
      val sum = DotProductUtils.dotProduct(wHiddenOutput(o), hiddenActivated, useSIMD = true)
      val biasedSum = if (useOutputBias) sum + bOutput(o) else sum
      output(o) = outputActivation.f(biasedSum)
      o += 1
    }
  }

  /**
   * Train the network using TD(λ)
   * Call forward() before calling this method
   * @param target the target value for the output layer
   * @param eligibilityTrace eligibility trace
   */
  def train(target: Array[Float], eligibilityTrace: EligibilityTrace2D): Unit = {
    // Step 1: Clip output values
    clipOutput()

    // Step 2: Compute error
    computeError(target)

    // Step 3: Precompute gradients for output and hidden layers to avoid duplication
    computeOutputGradients()
    computeHiddenGradients()

    //  Step 4: Update weights and eligibility traces for hidden-output layer
    updateHiddenOutputWeights(eligibilityTrace)
    updateInputHiddenWeights(eligibilityTrace)
  }

  // Error = TD error (target - output)
  private def computeError(target: Array[Float]): Unit = {
    var tdErrorSum = 0f
    for (o <- 0 until outputSize) {
      // No clipping - let TD learning proceed naturally
      error(o) = target(o) - clippedOutput(o)
      tdErrorSum += error(o).abs
    }

    // Compute TD error and accumulate absolute error for progress tracking
    cumulativeTDError += tdErrorSum
    tdErrorCount += 1
  }

  // Clip output values to avoid exact 0 or 1 (for sigmoid stability)
  private def clipOutput(): Unit = {
    val epsilon = 1e-6f

    outputActivation match {
      case Sigmoid =>
        var o = 0
        while (o < outputSize) {
          val raw = lastOutput(o)
          clippedOutput(o) = math.max(epsilon, math.min(1.0f - epsilon, raw))
          o += 1
        }
      case _ =>
        // Just copy values if no clipping is needed
        System.arraycopy(lastOutput, 0, clippedOutput, 0, outputSize)
    }
  }


  // Compute gradients for the output layer
  private def computeOutputGradients(): Unit = {
    var o = 0
    while (o < outputSize) {
      gradOut(o) = outputActivation.gradient(0f, clippedOutput(o))
      o += 1
    }
  }

  // Compute gradients for the hidden layer
  private def computeHiddenGradients(): Unit = {
    var h = 0
    while (h < hiddenSize) {
      gradHidden(h) = hiddenActivation.gradient(hiddenRaw(h), hiddenActivated(h))
      h += 1
    }
  }

  // Update weights and eligibility traces for hidden-output layer
  private def updateHiddenOutputWeights(eligibilityTrace: EligibilityTrace2D): Unit = {
    var o = 0
    while (o < outputSize) {
      var h = 0
      while (h < hiddenSize) {
        val delta = gradOut(o) * hiddenActivated(h)
        eligibilityTrace.eHiddenOutput(o)(h) = gamma * lambda * eligibilityTrace.eHiddenOutput(o)(h) + delta
        wHiddenOutput(o)(h) += alpha * error(o) * eligibilityTrace.eHiddenOutput(o)(h)
        h += 1
      }

      // Bias update for output layer with slower learning rate
      // Only update if output bias is enabled
      if (useOutputBias) {
        val biasAlpha = alpha * biasAlphaRatio
        bOutput(o) += biasAlpha * error(o) * gradOut(o)
      }

      o += 1
    }
  }

  // Update weights and eligibility traces for input-hidden layer
  private def updateInputHiddenWeights(eligibilityTrace: EligibilityTrace2D): Unit = {
    var h = 0
    while (h < hiddenSize) {
      // Extract column of wHiddenOutput for this h
      val column = Array.ofDim[Float](outputSize)
      var o = 0
      while (o < outputSize) {
        column(o) = wHiddenOutput(o)(h)
        o += 1
      }

      // Use SIMD to compute sum = dot(error, column)
      val sum = DotProductUtils.dotProduct(error, column, useSIMD = true)

      // Update weights and traces for all inputs of this hidden neuron
      var i = 0
      while (i < inputSize) {
        val delta = gradHidden(h) * lastInput(i)
        eligibilityTrace.eInputHidden(h)(i) = gamma * lambda * eligibilityTrace.eInputHidden(h)(i) + delta
        wInputHidden(h)(i) += alpha * sum * eligibilityTrace.eInputHidden(h)(i)
        i += 1
      }

      // Bias update for hidden neuron
      bHidden(h) += alpha * sum * gradHidden(h)

      h += 1
    }
  }
}

case class WeightStatistics(
  mean: Float,
  stdDev: Float,
  maxAbs: Float,
  nearZeroCount: Int,
  largeCount: Int,
  totalCount: Int
) {
  def prettyPrint: String = {
    val nearZeroPct = nearZeroCount.toFloat / totalCount * 100
    val largePct = largeCount.toFloat / totalCount * 100

    f"""Weight Statistics:
       |  Mean: $mean%.4f, StdDev: $stdDev%.4f, MaxAbs: $maxAbs%.4f
       |  Near-zero (<0.01): $nearZeroCount ($nearZeroPct%.1f%%)
       |  Large (>5.0): $largeCount ($largePct%.1f%%)""".stripMargin
  }

  def healthWarnings: List[String] = {
    var warnings = List.empty[String]
    if (maxAbs > 10.0f) warnings = warnings :+ "⚠️ WEIGHTS GROWING LARGE - possible instability"
    if (maxAbs < 0.5f) warnings = warnings :+ "⚠️ WEIGHTS TOO SMALL - possibly stuck in minima"
    if (nearZeroCount.toFloat / totalCount > 0.5f) warnings = warnings :+ "⚠️ >50% weights near-zero - network may be dying"
    warnings
  }
}
