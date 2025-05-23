package org.akoshterek.backgammon.dice

import org.apache.commons.math3.distribution.UniformIntegerDistribution
import org.apache.commons.math3.random.Well19937c

class PseudoRandomDiceRoller(val seed: Long) extends DiceRoller {
  private val rng = new Well19937c(seed)
  private val distribution = new UniformIntegerDistribution(rng, 1, 6)

  private def nextDice: Int = distribution.sample

  override def roll(): (Int, Int) = (nextDice, nextDice)
}

object PseudoRandomDiceRoller {
  // Provide a convenient factory method
  def apply(seed: Long = 16000000L): PseudoRandomDiceRoller = {
    new PseudoRandomDiceRoller(seed)
  }
}
