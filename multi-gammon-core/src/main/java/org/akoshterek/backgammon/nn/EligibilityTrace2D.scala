package org.akoshterek.backgammon.nn

// Eligibility traces
class EligibilityTrace2D(inputSize: Int,
                         hiddenSize: Int,
                         outputSize: Int) {
  val eInputHidden: Array[Array[Float]] = Array.ofDim[Float](hiddenSize, inputSize)
  val eHiddenOutput: Array[Array[Float]] = Array.ofDim[Float](outputSize, hiddenSize)

  def reset(): Unit = {
    for (h <- eInputHidden.indices; i <- eInputHidden(h).indices)
      eInputHidden(h)(i) = 0f

    for (o <- eHiddenOutput.indices; h <- eHiddenOutput(o).indices)
      eHiddenOutput(o)(h) = 0f
  }
}
