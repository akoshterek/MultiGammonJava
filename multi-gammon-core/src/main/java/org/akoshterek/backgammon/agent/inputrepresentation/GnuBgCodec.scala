package org.akoshterek.backgammon.agent.inputrepresentation

class GnuBgCodec extends PointCodec {
    def setPoint(men: Byte, inputs: Array[Double], offset: Int) {
        inputs(offset) = if (men == 1) 1.0f else 0.0f
        inputs(offset + 1) = if (men == 2) 1.0f else 0.0f
        inputs(offset + 2) = if (men >= 3) 1.0f else 0.0f
        inputs(offset + 3) = if (men >= 4) (men - 3) / 12.0f else 0.0f
    }
}