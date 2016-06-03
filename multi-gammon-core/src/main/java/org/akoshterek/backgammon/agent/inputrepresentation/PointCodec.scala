package org.akoshterek.backgammon.agent.inputrepresentation

trait PointCodec {
    def getInputsPerPoint: Int = 4

    def setPoint(men: Byte, inputs: Array[Double], offset: Int)
}