package org.akoshterek.backgammon.agent.inputrepresentation

object Tesauro89Codec extends PointCodec {
  override def point(men: Int, index: Int): Double = index match {
    case 0 => if (men == 1) 1.0 else 0.0
    case 1 => if (men == 2) 1.0 else 0.0
    case 2 => if (men == 3) 1.0f else 0.0
    case 3 => if (men >= 4) (men - 3) / 12.0 else 0.0
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}