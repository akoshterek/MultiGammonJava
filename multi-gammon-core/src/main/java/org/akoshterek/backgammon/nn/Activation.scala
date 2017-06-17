package org.akoshterek.backgammon.nn

/**
  * Created by Alex on 06-06-17.
  */
sealed trait Activation {
  def f(x: Float): Float
  def gradient(x: Float): Float
}

object Linear extends Activation {
  override def f(x: Float): Float = x
  override def gradient(x: Float): Float = 1.0f
}

object Sigmoid extends Activation {
  override def f(x: Float): Float = 1.0f / (1.0f + Math.exp(-x).toFloat)
  override def gradient(x: Float): Float = {
    val a = f(x)
    a * (1.0f - a)
  }
}

object Elliott extends Activation {
  override def f(x: Float): Float = x / (1.0f + Math.abs(x))
  override def gradient(x: Float): Float = {
    val d = 1.0f + Math.abs(x)
    1.0f / (d * d)
  }
}