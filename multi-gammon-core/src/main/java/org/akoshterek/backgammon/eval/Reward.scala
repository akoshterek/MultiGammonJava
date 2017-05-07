package org.akoshterek.backgammon.eval

import org.akoshterek.backgammon.Constants._

/**
  * @author Alex
  *         date 19.07.2015.
  *
  *         backgammon reward
  */
final class Reward(input: Array[Double]) {
  def data: Array[Double] = input

  require(data.length == NUM_OUTPUTS)

  def this(reward: Reward) {
    this(reward.data)
  }

  def this(reward: Seq[Double]) {
    this(reward.toArray)
  }

  def this() {
    this(Array.fill[Double](NUM_OUTPUTS)(0))
  }

  def apply(index: Int): Double = data.apply(index)

  /**
    * Move evaluation
    * let's keep things simple as I don't want to go into cube handling
    * At least for now
    *
    * @return money equity
    */
  def equity: Double = {
    (data(OUTPUT_WIN) * 2.0 - 1.0
      +(data(OUTPUT_WINGAMMON) - data(OUTPUT_LOSEGAMMON))
      +(data(OUTPUT_WINBACKGAMMON) - data(OUTPUT_LOSEBACKGAMMON)))
  }

  def invert: Reward = {
    val res = Reward.rewardArray
    var r: Double = .0
    res(OUTPUT_WIN) = 1.0 - data(OUTPUT_WIN)
    r = data(OUTPUT_WINGAMMON)
    res(OUTPUT_WINGAMMON) = data(OUTPUT_LOSEGAMMON)
    res(OUTPUT_LOSEGAMMON) = r
    r = data(OUTPUT_WINBACKGAMMON)
    res(OUTPUT_WINBACKGAMMON) = data(OUTPUT_LOSEBACKGAMMON)
    res(OUTPUT_LOSEBACKGAMMON) = r
    new Reward(res)
  }

  def clamp(): Reward = {
    def crop(minVal: Double, maxVal: Double, value: Double): Double = {
      require(minVal <= maxVal, "min is greater than max")
      maxVal.min(minVal.max(value))
    }

    new Reward(data.map(x => crop(0, 1, x)))
  }

  def *(value: Double): Reward = {
    new Reward(data.map(_ * value))
  }

  def +(that: Reward): Reward = {
    new Reward(Array.tabulate[Double](NUM_OUTPUTS)(i => data(i) + that.data(i)))
  }

  override def toString: String = {
    data.mkString(", ")
  }

  def toArray : Array[Double] = data

  override def equals(that: Any): Boolean = {
    that match {
      case that: Reward => this.data.sameElements(that.data)
      case _ => false
    }
  }

  override def hashCode: Int = {
    31 + data.toSeq.hashCode()
  }
}

object Reward {
  def apply(input: Array[Double]): Reward = new Reward(input)

  def rewardArray: Array[Double] = Array.ofDim[Double](NUM_OUTPUTS)
}
