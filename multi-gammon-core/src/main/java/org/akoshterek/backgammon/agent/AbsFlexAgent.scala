package org.akoshterek.backgammon.agent

import java.nio.file.Path

import org.akoshterek.backgammon.agent.fa.FunctionApproximator
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

abstract class AbsFlexAgent(override val fullName: String, override val path: Path) extends AbsAgent(fullName, path) {
    protected val contactRepresentation: InputRepresentation
    protected val raceRepresentation: InputRepresentation
    protected val crashedRepresentation: InputRepresentation
    protected var contactFa: FunctionApproximator = _
    protected var raceFa: FunctionApproximator = _
    protected var crashedFa: FunctionApproximator = _

    def evalContact(board: Board): Reward = {
        val inputs: Array[Float] = contactRepresentation.calculateContactInputs(board)
        contactFa.calculateReward(inputs)
    }

    override def evalRace(board: Board): Reward = {
        val inputs: Array[Float] = raceRepresentation.calculateRaceInputs(board)
        raceFa.calculateReward(inputs)
    }

    override def evalCrashed(board: Board): Reward = {
        val inputs: Array[Float] = crashedRepresentation.calculateCrashedInputs(board)
        crashedFa.calculateReward(inputs)
    }
}