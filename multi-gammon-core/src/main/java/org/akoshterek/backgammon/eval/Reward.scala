package org.akoshterek.backgammon.eval

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

        require(data.length == NUM_OUTPUTS)
        Array.copy(data, 0, this.data, 0, this.data.length)
    }

    def this(reward: Reward) {
        this(reward.data)
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

    def invert: Reward = {
        val res = new Reward()
        var r: Double = .0
        res.data(OUTPUT_WIN) = 1.0 - data(OUTPUT_WIN)
        r = data(OUTPUT_WINGAMMON)
        res.data(OUTPUT_WINGAMMON) = data(OUTPUT_LOSEGAMMON)
        res.data(OUTPUT_LOSEGAMMON) = r
        r = data(OUTPUT_WINBACKGAMMON)
        res.data(OUTPUT_WINBACKGAMMON) = data(OUTPUT_LOSEBACKGAMMON)
        res.data(OUTPUT_LOSEBACKGAMMON) = r
        res
    }

    def clamp(): Reward = {
        def crop(minVal: Double, maxVal: Double, value: Double): Double = {
            require (minVal <= maxVal, "min is greater than max")
            maxVal.min(minVal.max(value))
        }

        new Reward (data.map(x => crop(0, 1, x)))
    }

    def *(value: Double): Reward = {
        new Reward(data.map(_ * value))
    }

    def +(that: Reward): Reward = {
        new Reward(data.zip(that.data).map( {case (x, y) => x + y}))
    }

    override def toString: String = {
        data.mkString(", ")
    }
}