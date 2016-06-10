package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.fa.FunctionApproximator
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path

abstract class AbsFlexAgent(override val fullName: String, override val path: Path) extends AbsAgent(fullName, path) {
    protected var contactRepresentation: InputRepresentation = null
    protected var raceRepresentation: InputRepresentation = null
    protected var crashedRepresentation: InputRepresentation = null
    protected var contactFa: FunctionApproximator = null
    protected var raceFa: FunctionApproximator = null
    protected var crashedFa: FunctionApproximator = null

    def evalContact(board: Board): Reward = {
        val inputs: Array[Double] = contactRepresentation.calculateContactInputs(board)
        contactFa.calculateReward(inputs)
    }

    override def evalRace(board: Board): Reward = {
        val inputs: Array[Double] = raceRepresentation.calculateRaceInputs(board)
        raceFa.calculateReward(inputs)
    }

    override def evalCrashed(board: Board): Reward = {
        val inputs: Array[Double] = crashedRepresentation.calculateCrashedInputs(board)
        crashedFa.calculateReward(inputs)
    }
}