package org.akoshterek.backgammon.agent.inputrepresentation

import org.akoshterek.backgammon.board.Board

trait InputRepresentation {
    def getRaceInputsCouns: Int
    def getCrashedInputsCount: Int
    def getContactInputsCount: Int

    def calculateRaceInputs(anBoard: Board): Array[Double]
    def calculateCrashedInputs(anBoard: Board): Array[Double]
    def calculateContactInputs(anBoard: Board): Array[Double]
}