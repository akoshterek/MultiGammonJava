package org.akoshterek.backgammon.agent.inputrepresentation

sealed trait PointCodec {
  /**
    * @return number of inputs per point
    */
  def inputsPerPoint: Int = 4

  /**
    * @param men   number of men on the point
    * @param index in range [0, inputsPerPoint)
    * @return encoded input
    */
  def point(men: Int, index: Int): Float

  val self: this.type = this
}

object SuttonCodec extends PointCodec {
  override def point(men: Int, index: Int): Float = index match {
    case 0 => if (men >= 1) 1.0f else 0.0f
    case 1 => if (men >= 2) 1.0f else 0.0f
    case 2 => if (men >= 3) 1.0f else 0.0f
    case 3 => if (men >= 4) (men - 3) / 12.0f else 0.0f
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}

object Tesauro89Codec extends PointCodec {
  override def point(men: Int, index: Int): Float = index match {
    case 0 => if (men == 1) 1.0f else 0.0f
    case 1 => if (men == 2) 1.0f else 0.0f
    case 2 => if (men == 3) 1.0f else 0.0f
    case 3 => if (men >= 4) (men - 3) / 12.0f else 0.0f
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}

object Tesauro92Codec extends PointCodec {
  override def point(men: Int, index: Int): Float = index match {
    case 0 => if (men == 1) 1.0f else 0.0f
    case 1 => if (men >= 2) 1.0f else 0.0f
    case 2 => if (men == 3) 1.0f else 0.0f
    case 3 => if (men >= 4) (men - 3) / 12.0f else 0.0f
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}

object GnuBgCodec extends PointCodec {
  override def point(men: Int, index: Int): Float = index match {
    case 0 => if (men == 1) 1.0f else 0.0f
    case 1 => if (men == 2) 1.0f else 0.0f
    case 2 => if (men >= 3) 1.0f else 0.0f
    case 3 => if (men >= 4) (men - 3) / 12.0f else 0.0f
    case _ => throw new IllegalArgumentException("Invalid index")
  }
}
