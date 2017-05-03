package org.akoshterek.backgammon.agent.raw

import org.akoshterek.backgammon.agent.inputrepresentation.{InputRepresentation, PointCodec}
import org.akoshterek.backgammon.board.Board

/**
  * @author Alex
  *         date 21.09.2015.
  */
class RawRepresentation(val codec: PointCodec) extends InputRepresentation {
    def getRaceInputsCount: Int = {
        getContactInputsCount
    }

    def getCrashedInputsCount: Int = {
        getContactInputsCount
    }

    def getContactInputsCount: Int = {
        2 * (codec.getInputsPerPoint * 24 + 2)
    }

    def calculateRaceInputs(anBoard: Board): Array[Double] = {
        calculateContactInputs(anBoard)
    }

    def calculateCrashedInputs(anBoard: Board): Array[Double] = {
        calculateContactInputs(anBoard)
    }

    def calculateContactInputs(anBoard: Board): Array[Double] = {
        val inputs: Array[Double] = new Array[Double](getContactInputsCount)
        calculateHalfBoard(anBoard.anBoard(0), inputs, 0)
        calculateHalfBoard(anBoard.anBoard(1), inputs, inputs.length / 2)
        //Normalizer.sigmoidToTanhNormalizer(inputs);
        inputs
    }

    private def calculateHalfBoard(halfBoard: Array[Int], inputs: Array[Double], offset: Int) {
        halfBoard.dropRight(1).indices.foreach(i => codec.setPoint(halfBoard(i), inputs, offset + i * codec.getInputsPerPoint))
        inputs(offset + 96) = halfBoard(Board.BAR) / 15.0f
        val home = Board.TOTAL_MEN - halfBoard.dropRight(1).sum
        inputs(offset + 97) = home / 15.0f
    }
}