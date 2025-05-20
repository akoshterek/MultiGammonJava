package org.akoshterek.backgammon.eval

import org.akoshterek.backgammon.Constants._

import scala.reflect.ClassTag

/**
  * @author Alex
  *         date 19.07.2015.
  *
  *         backgammon reward
  */
final class Reward(input: Array[Float]) {
  def data: Array[Float] = input

  require(data.length == NUM_OUTPUTS)

  def this(reward: Reward) {
    this(reward.data)
  }

  def this(reward: Seq[Float]) {
    this(reward.toArray)
  }

  def this() {
    this(Array.fill[Float](NUM_OUTPUTS)(0))
  }

  def apply(index: Int): Float = data.apply(index)

  /**
    * Move evaluation
    * lets keep things simple as I don't want to go into cube handling
    * At least for now
    *
    * @return money equity
    */
  def equity: Float = {
    (data(OUTPUT_WIN) * 2.0f - 1.0f
      +(data(OUTPUT_WINGAMMON) - data(OUTPUT_LOSEGAMMON))
      +(data(OUTPUT_WINBACKGAMMON) - data(OUTPUT_LOSEBACKGAMMON)))
  }

  def invert: Reward = {
    val res = Reward.rewardArray[Float]
    var r: Float = 0.0f
    res(OUTPUT_WIN) = 1.0f - data(OUTPUT_WIN)
    r = data(OUTPUT_WINGAMMON)
    res(OUTPUT_WINGAMMON) = data(OUTPUT_LOSEGAMMON)
    res(OUTPUT_LOSEGAMMON) = r
    r = data(OUTPUT_WINBACKGAMMON)
    res(OUTPUT_WINBACKGAMMON) = data(OUTPUT_LOSEBACKGAMMON)
    res(OUTPUT_LOSEBACKGAMMON) = r
    new Reward(res)
  }

  def clamp(): Reward = {
    def crop(minVal: Float, maxVal: Float, value: Float): Float = {
      require(minVal <= maxVal, "min is greater than max")
      maxVal.min(minVal.max(value))
    }

    new Reward(data.map(x => crop(0, 1, x)))
  }

  def *(value: Float): Reward = {
    new Reward(data.map(_ * value))
  }

  def +(that: Reward): Reward = {
    new Reward(Array.tabulate[Float](NUM_OUTPUTS)(i => data(i) + that.data(i)))
  }

  override def toString: String = {
    data.mkString(", ")
  }

  def toArray: Array[Float] = data

  def toDoubleArray: Array[Double] = data.map(_.toDouble)

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
  def apply(input: Array[Float]): Reward = new Reward(input)

  def apply(input: Array[Double]): Reward = new Reward(input.map(_.toFloat))

  def rewardArray[T: ClassTag]: Array[T] = Array.ofDim[T](NUM_OUTPUTS)
}
