package org.akoshterek.backgammon.agent.fa

import org.akoshterek.backgammon.eval.Reward

/**
  * @author Alex
  *         date 02.08.2015.
  */
trait FunctionApproximator {
    def calculateReward(input: Array[Float]): Reward

    def setReward(input: Array[Float], reward: Reward): Unit

    def updateAddToReward(input: Array[Float], deltaReward: Reward): Unit = {
        val currentReward: Reward = calculateReward(input)
        setReward(input, (currentReward + deltaReward).clamp())
    }
}