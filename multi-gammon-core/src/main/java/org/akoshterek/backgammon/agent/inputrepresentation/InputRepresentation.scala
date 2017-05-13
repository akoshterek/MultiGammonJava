package org.akoshterek.backgammon.agent.inputrepresentation

import org.akoshterek.backgammon.board.Board

trait InputRepresentation {
  val raceInputsCount: Int
  val crashedInputsCount: Int
  val contactInputsCount: Int

  def calculateRaceInputs(anBoard: Board): Array[Double]
  def calculateCrashedInputs(anBoard: Board): Array[Double]
  def calculateContactInputs(anBoard: Board): Array[Double]
}