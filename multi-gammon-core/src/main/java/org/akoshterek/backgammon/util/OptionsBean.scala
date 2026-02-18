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
  var alpha: Float = 0.01f
  var lambda: Float = 0.7f
  var gamma: Float = 1.0f
  var experimentPath: String = ""
  var experimentRunTag: String = ""
  var trainingOpponents: String = "self:100"
  var benchmarkOpponents: String = "SimpleHeuristic"
  var useOutputBias: Boolean = true

  var gaTraining: Boolean = false
  var gaPopulationSize: Int = 20
  var gaGenerations: Int = 10
  var gaEliteCount: Int = 4
  var gaMutationRate: Float = 0.05f
  var gaMutationStrength: Float = 0.1f

  def agentNames: Vector[String] = _agentNames
  def agentNames_= (agents: Array[String]): Unit = {
    agents match {
      case null => _agentNames = Vector()
      case _ => _agentNames = agents.toVector
    }
  }
}