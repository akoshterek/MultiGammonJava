package org.akoshterek.backgammon.train

import org.akoshterek.backgammon.agent.inputrepresentation.RepresentationFactory
import org.akoshterek.backgammon.board.PositionClass
import org.encog.Encog

object AgentTrainer extends App {
  val agentName: String = args(0)

  val settings: AgentSettings = new AgentSettings
  settings.representation = RepresentationFactory.createInputRepresentation(agentName)
  settings.agentName = agentName
  settings.hiddenNeuronCount = getHiddenNeuronsCount(agentName)

  trainContact(settings)
  trainCrashed(settings)
  trainRace(settings)
  Encog.getInstance.shutdown()

  private def trainContact(settings: AgentSettings) {
    System.out.println("Started contact network training")
    trainNetwork(settings, PositionClass.CLASS_CONTACT)
    System.out.println("Finished contact network training")
  }

  private def trainCrashed(settings: AgentSettings) {
    System.out.println("Started crashed network training")
    trainNetwork(settings, PositionClass.CLASS_CRASHED)
    System.out.println("Finished crashed network training")
  }

  private def trainRace(settings: AgentSettings) {
    System.out.println("Started race network training")
    trainNetwork(settings, PositionClass.CLASS_RACE)
    System.out.println("Finished race network training")
  }

  private def trainNetwork(settings: AgentSettings, networkType: PositionClass) {
    val trainer: NetworkTrainer = new NetworkTrainer(settings, networkType)
    trainer.trainNetwork
  }

  private def getHiddenNeuronsCount(agentName: String): Int = {
    val tokens: Array[String] = agentName.split("-")
    tokens(2).toInt
  }
}