package org.akoshterek.backgammon.genetic

import java.nio.file.Path
import org.akoshterek.backgammon.agent.{Agent, CopyableAgent}
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro92Codec
import org.akoshterek.backgammon.agent.raw.RawRepresentation
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.Constants._

/**
 * Lightweight agent for genetic algorithm training.
 * Uses SimpleNeuralNetwork for evaluation only (no learning).
 */
class GeneticAgent(val path: Path, 
                   val network: SimpleNeuralNetwork,
                   val agentId: Int = 0) extends Agent with CopyableAgent[GeneticAgent] {
  
  private val representation = new RawRepresentation(Tesauro92Codec)
  
  override val fullName: String = s"GeneticAgent-$agentId"
  
  override def copyAgent(): GeneticAgent = {
    // Clone the network for self-play
    val clonedNetwork = network.cloneNetwork()
    val copy = new GeneticAgent(path, clonedNetwork, agentId)
    copy.setPlayedGames(this.playedGames)
    copy
  }
  
  override def evaluatePosition(board: Board, pc: PositionClass): Reward = {
    pc match {
      case PositionClass.CLASS_OVER => evalOver(board)
      case _ => evalContact(board)
    }
  }
  
  override def evalContact(board: Board): Reward = {
    val input = representation.calculateContactInputs(board)
    val output = network.evaluate(input)
    val rewardArray = Reward.rewardArray[Float]
    rewardArray(OUTPUT_WIN) = output
    new Reward(rewardArray)
  }
  
  override def evalOver(board: Board): Reward = {
    // Game is over, return 1.0 for win, 0.0 for loss
    // Check if current player has won by counting remaining pieces
    val opponentPieces = board.anBoard(Board.OPPONENT).sum
    val rewardArray = Reward.rewardArray[Float]
    rewardArray(OUTPUT_WIN) = if (opponentPieces == 0) 1.0f else 0.0f
    new Reward(rewardArray)
  }
}

/**
 * Factory for creating GeneticAgents
 */
object GeneticAgent {
  /**
   * Create a new agent with random weights
   */
  def createRandom(path: Path, agentId: Int = 0): GeneticAgent = {
    val representation = new RawRepresentation(Tesauro92Codec)
    val network = new SimpleNeuralNetwork(representation.contactInputsCount, 40, 1)
    new GeneticAgent(path, network, agentId)
  }
  
  /**
   * Create agent from existing weights
   */
  def fromWeights(path: Path, weights: org.akoshterek.backgammon.nn.NetworkWeights, agentId: Int = 0): GeneticAgent = {
    val representation = new RawRepresentation(Tesauro92Codec)
    val network = new SimpleNeuralNetwork(representation.contactInputsCount, 40, 1)
    network.loadWeights(weights)
    new GeneticAgent(path, network, agentId)
  }
}
