package org.akoshterek.backgammon.nn

sealed trait Activation {
  def f(x: Float): Float
  def gradient(before: Float, after: Float): Float
}

object LeakyReLU extends Activation {
  private val alpha = 0.01f // Standard leakage coefficient

  override def f(x: Float): Float = {
    if (x > 0) x else alpha * x
  }

  override def gradient(before: Float, after: Float): Float = {
    if (before > 0) 1.0f else alpha
  }
}

object Linear extends Activation {
  override def f(x: Float): Float = x
  override def gradient(before: Float, after: Float): Float = 1.0f
}

object Sigmoid extends Activation {
  override def f(x: Float): Float = 1.0f / (1.0f + Math.exp(-x).toFloat)
  override def gradient(before: Float, after: Float): Float = {
    after * (1.0f - after)
  }
}

object Elliott extends Activation {
  override def f(x: Float): Float = x / (1.0f + Math.abs(x))
  override def gradient(before: Float, after: Float): Float = {
    val d = 1.0f + Math.abs(before)
    1.0f / (d * d)
  }
}