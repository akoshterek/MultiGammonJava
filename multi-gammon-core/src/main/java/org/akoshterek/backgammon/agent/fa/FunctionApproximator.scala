package org.akoshterek.backgammon.agent.fa

import org.akoshterek.backgammon.eval.Reward

/**
  * @author Alex
  *         date 02.08.2015.
  */
trait FunctionApproximator {
    def calculateReward(input: Array[Double]): Reward

    def setReward(input: Array[Double], reward: Reward)

    def updateAddToReward(input: Array[Double], deltaReward: Reward) {
        val currentReward: Reward = calculateReward(input)
        setReward(input, (currentReward + deltaReward).clamp())
    }
}