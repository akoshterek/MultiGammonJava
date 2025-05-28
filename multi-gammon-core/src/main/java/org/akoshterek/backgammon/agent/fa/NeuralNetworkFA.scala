package org.akoshterek.backgammon.agent.fa

import java.nio.file.Path

/**
  * @author Alex
  *         date 26.09.2015.
  */
trait NeuralNetworkFA extends FunctionApproximator {
    def saveNN(path: Path): Unit
}