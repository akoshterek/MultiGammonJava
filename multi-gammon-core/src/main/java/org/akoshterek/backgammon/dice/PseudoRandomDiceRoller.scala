package org.akoshterek.backgammon.dice

import org.apache.commons.math3.distribution.UniformIntegerDistribution
import org.apache.commons.math3.random.Well19937c

class PseudoRandomDiceRoller(val seed: Long) extends DiceRoller {
  private val threadLocalRng = ThreadLocal.withInitial(() => {
    new Well19937c(seed)
  })

  private val threadLocalDist = ThreadLocal.withInitial(() =>
    new UniformIntegerDistribution(threadLocalRng.get(), 1, 6)
  )

  private def nextDice: Int = threadLocalDist.get().sample

  override def roll(): (Int, Int) = (nextDice, nextDice)
}

object PseudoRandomDiceRoller {
  // Provide a convenient factory method
  def apply(seed: Long = 16000000L): PseudoRandomDiceRoller = {
    new PseudoRandomDiceRoller(seed)
  }
}
