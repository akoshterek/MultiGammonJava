package org.akoshterek.backgammon.agent

trait CopyableAgent[T <: Agent] {
  def copyAgent(): T
}
