package org.akoshterek.backgammon.util

class OptionsBean {
  var isHelp: Boolean = false
  var isWarranty: Boolean = false
  var isLicense: Boolean = false
  var agentNames: Array[String] = null
  var benchmarkAgentName: String = null
  var trainingGames: Int = 0
  var benchmarkGames: Int = 0
  var benchmarkPeriod: Int = 0
  var isVerbose: Boolean = false
}