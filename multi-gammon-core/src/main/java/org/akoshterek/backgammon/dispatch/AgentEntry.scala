package org.akoshterek.backgammon.dispatch

import org.akoshterek.backgammon.agent.Agent

/**
  * @author Alex
  *         On: 22.05.16
  */
class AgentEntry(_agent: Agent) {
  def agent: Agent = this._agent
  var wonGames: Int = 0
  var wonPoints: Int = 0
}
