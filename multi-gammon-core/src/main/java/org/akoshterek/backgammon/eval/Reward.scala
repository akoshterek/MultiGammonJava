package org.akoshterek.backgammon.eval

import java.util

import org.akoshterek.backgammon.Constants._

/**
  * @author Alex
  *         date 19.07.2015.
  *
  *         backgammon reward
  */
final class Reward {
    val data: Array[Double] = new Array[Double](NUM_OUTPUTS)

    def this(data: Array[Double]) {
        this()
        System.arraycopy(data, 0, this.data, 0, this.data.length)
    }

    def this(reward: Reward) {
        this(reward.data)
    }

    def reset() {
        util.Arrays.fill(data, 0)
    }

    /**
      * Move evaluation
      * let's keep things simple as I don't want to go into cube handling
      * At least for now
      *
      * @return money equity
      */
    def equity: Double = {
        data(OUTPUT_WIN) * 2.0 - 1.0
            + (data(OUTPUT_WINGAMMON) - data(OUTPUT_LOSEGAMMON))
            + (data(OUTPUT_WINBACKGAMMON) - data(OUTPUT_LOSEBACKGAMMON))
    }

    def invert() {
        var r: Double = .0
        data(OUTPUT_WIN) = 1.0 - data(OUTPUT_WIN)
        r = data(OUTPUT_WINGAMMON)
        data(OUTPUT_WINGAMMON) = data(OUTPUT_LOSEGAMMON)
        data(OUTPUT_LOSEGAMMON) = r
        r = data(OUTPUT_WINBACKGAMMON)
        data(OUTPUT_WINBACKGAMMON) = data(OUTPUT_LOSEBACKGAMMON)
        data(OUTPUT_LOSEBACKGAMMON) = r
    }

    def clamp: Reward = {
        val res: Reward = new Reward
        for (i <- res.data.indices) {
            res.data(i) = crop(0, 1, res.data(i))
        }
        res
    }

    def *(value: Double): Reward = {
        val res: Reward = new Reward
        for (i <- res.data.indices) {
            res.data(i) = this.data(i) * value
        }
        res
    }

    def +(that: Reward): Reward = {
        val res: Reward = new Reward
        for (i <- res.data.indices) {
            res.data(i) = this.data(i) + that.data(i)
        }
        res
    }

    private def crop(minVal: Double, maxVal: Double, value: Double): Double = {
        if(minVal > maxVal) throw new IllegalArgumentException ("min is greater than max")
        maxVal.min(minVal.max(value))
    }

    override def toString: String = {
        util.Arrays.toString(data)
    }
}