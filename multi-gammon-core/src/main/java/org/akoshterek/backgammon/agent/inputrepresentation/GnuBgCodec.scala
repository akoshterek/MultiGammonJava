package org.akoshterek.backgammon.agent.inputrepresentation

case object GnuBgCodec extends PointCodec {
  override def point(men: Int, index: Int): Float = index match {
    case 0 => if (men == 1) 1.0f else 0.0f
    case 1 => if (men == 2) 1.0f else 0.0f
    case 2 => if (men >= 3) 1.0f else 0.0f
    case 3 => if (men >= 4) (men - 3) / 12.0f else 0.0f
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}