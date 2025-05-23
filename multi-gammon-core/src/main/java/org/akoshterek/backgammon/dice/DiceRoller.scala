package org.akoshterek.backgammon.dice

trait DiceRoller {
  def roll(): (Int, Int)
}
