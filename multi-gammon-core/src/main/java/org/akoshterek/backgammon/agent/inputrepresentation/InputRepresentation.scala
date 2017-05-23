package org.akoshterek.backgammon.agent.inputrepresentation

import org.akoshterek.backgammon.board.Board

trait InputRepresentation {
  val raceInputsCount: Int
  val crashedInputsCount: Int
  val contactInputsCount: Int

  def calculateRaceInputs(anBoard: Board): Array[Float]
  def calculateCrashedInputs(anBoard: Board): Array[Float]
  def calculateContactInputs(anBoard: Board): Array[Float]
}