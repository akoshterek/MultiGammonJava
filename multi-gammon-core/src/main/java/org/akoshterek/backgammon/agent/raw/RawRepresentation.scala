package org.akoshterek.backgammon.agent.raw

import org.akoshterek.backgammon.agent.inputrepresentation.{InputRepresentation, PointCodec}
import org.akoshterek.backgammon.board.Board

/**
  * @author Alex
  *         date 21.09.2015.
  */
class RawRepresentation(val codec: PointCodec) extends InputRepresentation {
  val contactInputsCount: Int = 2 * (codec.inputsPerPoint * 24 + 2)
  val raceInputsCount: Int = contactInputsCount
  val crashedInputsCount: Int = contactInputsCount

  def calculateRaceInputs(anBoard: Board): Array[Double] = {
    calculateContactInputs(anBoard)
  }

  def calculateCrashedInputs(anBoard: Board): Array[Double] = {
    calculateContactInputs(anBoard)
  }

  def calculateContactInputs(anBoard: Board): Array[Double] = {
    val inputs: Array[Double] = new Array[Double](contactInputsCount)
    calculateHalfBoard(anBoard.anBoard(0), inputs, 0)
    calculateHalfBoard(anBoard.anBoard(1), inputs, inputs.length / 2)
    inputs
  }

  private def calculateHalfBoard(halfBoard: Array[Int], inputs: Array[Double], offset: Int) {
    var point = 0
    while (point < Board.HALF_BOARD_SIZE - 1) {
      var i = 0
      while (i < codec.inputsPerPoint) {
        inputs(offset + point * codec.inputsPerPoint + i) = codec.point(halfBoard(point), i)
        i += 1
      }
      point += 1
    }

    inputs(offset + 96) = halfBoard(Board.BAR) / 15.0f
    val home = Board.TOTAL_MEN - halfBoard.sum
    inputs(offset + 97) = home / 15.0f
  }
}