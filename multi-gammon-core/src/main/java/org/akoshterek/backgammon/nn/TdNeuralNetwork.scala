package org.akoshterek.backgammon.nn

// The core TD(λ) network class
class TdNeuralNetwork(inputSize: Int,
                      hiddenSize: Int,
                      outputSize: Int,
                      val alpha: Float = 0.01f,
                      val lambda: Float = 0.7f,
                      val gamma: Float = 1.0f,
                      val hiddenActivation: Activation = LeakyReLU,
                      val outputActivation: Activation = Sigmoid
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

  def createEligibilityTrace() = new EligibilityTrace2D(inputSize, hiddenSize, outputSize)

  // Forward pass (updates internal hiddenRaw and hiddenActivated)
  def forward(input: Array[Float], output: Array[Float]): Unit = {
    computeHiddenLayer(input)
    computeOutputLayer(output)
    Array.copy(output, 0, lastOutput, 0, output.length);
  }

  private def computeHiddenLayer(input: Array[Float]): Unit = {
    var h = 0
    while (h < hiddenSize) {
      var sum = 0f
      var i = 0
      while (i < inputSize) {
        sum += wInputHidden(h)(i) * input(i)
        i += 1
      }
      sum += bHidden(h)
      hiddenRaw(h) = sum
      hiddenActivated(h) = hiddenActivation.f(sum)
      h += 1
    }
  }

  private def computeOutputLayer(output: Array[Float]): Unit = {
    var o = 0
    while (o < outputSize) {
      var sum = 0f
      var h = 0
      while (h < hiddenSize) {
        sum += wHiddenOutput(o)(h) * hiddenActivated(h)
        h += 1
      }
      sum += bOutput(o)
      output(o) = outputActivation.f(sum)
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
    val error = Array.ofDim[Float](outputSize)
    for (o <- 0 until outputSize) {
      error(o) = target(o) - clippedOutput(o)
    }
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
      // Bias update for output layer
      bOutput(o) += alpha * error(o) * gradOut(o)
      o += 1
    }
  }

  // Update weights and eligibility traces for input-hidden layer
  private def updateInputHiddenWeights(eligibilityTrace: EligibilityTrace2D): Unit = {
    var h = 0
    while (h < hiddenSize) {
      // Sum over outputs for backpropagation once per hidden neuron
      var sum = 0f
      var o = 0
      while (o < outputSize) {
        sum += error(o) * wHiddenOutput(o)(h)
        o += 1
      }

      // Update weights and eligibility traces for all inputs of this hidden neuron
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
