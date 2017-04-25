package org.akoshterek.backgammon.util

class OptionsBean {
  var isHelp: Boolean = false
  var isWarranty: Boolean = false
  var isLicense: Boolean = false
  private var _agentNames: Vector[String] = Vector()
  var benchmarkAgentName: String = ""
  var trainingGames: Int = 0
  var benchmarkGames: Int = 0
  var benchmarkPeriod: Int = 0
  var isVerbose: Boolean = false

  def agentNames = _agentNames
  def agentNames_= (agents: Array[String]): Unit = {
    agents match {
      case null => _agentNames = Vector()
      case _ => _agentNames = agents.toVector
    }
  }
}