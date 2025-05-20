package org.akoshterek.backgammon.train

import org.akoshterek.backgammon.agent.inputrepresentation.{EncogActivationFunctionFactory, RepresentationFactory}
import org.akoshterek.backgammon.board.PositionClass
import org.encog.Encog
import org.encog.engine.network.activation.ActivationFunction

object AgentTrainer extends App {
  val agentName: String = args(0)

  val settings: AgentSettings = new AgentSettings(
    RepresentationFactory.createInputRepresentation(agentName),
    getHiddenNeuronsCount(agentName),
    agentName,
    getActivationFunction(agentName))

  trainContact(settings)
  trainCrashed(settings)
  trainRace(settings)
  Encog.getInstance.shutdown()

  private def trainContact(settings: AgentSettings): Unit = {
    System.out.println("Started contact network training")
    trainNetwork(settings, PositionClass.CLASS_CONTACT)
    System.out.println("Finished contact network training")
  }

  private def trainCrashed(settings: AgentSettings): Unit = {
    System.out.println("Started crashed network training")
    trainNetwork(settings, PositionClass.CLASS_CRASHED)
    System.out.println("Finished crashed network training")
  }

  private def trainRace(settings: AgentSettings): Unit = {
    System.out.println("Started race network training")
    trainNetwork(settings, PositionClass.CLASS_RACE)
    System.out.println("Finished race network training")
  }

  private def trainNetwork(settings: AgentSettings, networkType: PositionClass): Unit = {
    val trainer: NetworkTrainer = new NetworkTrainer(settings, networkType)
    trainer.trainNetwork
  }

  private def getHiddenNeuronsCount(agentName: String): Int = {
    val tokens: Array[String] = agentName.split("-")
    tokens(2).toInt
  }

  private def getActivationFunction(agentName: String): ActivationFunction = {
    val tokens: Array[String] = agentName.split("-")
    EncogActivationFunctionFactory(tokens(3))
  }
}