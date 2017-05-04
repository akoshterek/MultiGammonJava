package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.agent.fa.FunctionApproximator
import org.akoshterek.backgammon.agent.inputrepresentation.InputRepresentation
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path

abstract class AbsFlexAgent(override val fullName: String, override val path: Path) extends AbsAgent(fullName, path) {
    protected var contactRepresentation: InputRepresentation = _
    protected var raceRepresentation: InputRepresentation = _
    protected var crashedRepresentation: InputRepresentation = _
    protected var contactFa: FunctionApproximator = _
    protected var raceFa: FunctionApproximator = _
    protected var crashedFa: FunctionApproximator = _

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