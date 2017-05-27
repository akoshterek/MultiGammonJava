package org.akoshterek.backgammon.move

object AuchKey {
  def fromNnPosition(positionId: String): AuchKey = {
    require(positionId != null && positionId.length == 20, "Illegal position string " + positionId)

    val key: AuchKey = new AuchKey

    for(i <- 0 until 10) {
      val i2_0: Char = positionId.charAt(2 * i)
      val i2_1: Char = positionId.charAt(2 * i + 1)
      if (i2_0 >= 'A' && i2_0 <= 'P' && i2_1 >= 'A' && i2_1 <= 'P') {
        key.key(i) = (((i2_0 - 'A') << 4) + (i2_1 - 'A')).toByte
      }
      else {
        throw new IllegalArgumentException("Illegal position string " + positionId)
      }
    }

    key
  }
}

class AuchKey() {
  final val key: Array[Byte] = new Array[Byte](10)

  def this(src: AuchKey) {
    this()
    System.arraycopy(src.key, 0, key, 0, 10)
  }

  def intKey(index: Int): Int = {
    if (key(index) < 0) {
      256 + key(index)
    }
    else {
      key(index)
    }
  }

  override def equals(that: Any): Boolean = {
    that match {
      case that: AuchKey => sameElements(that.key)
      case _ => false
    }
  }

  private def sameElements (thatKey: Array[Byte]): Boolean = {
    if (this.key eq thatKey) {
      true
    } else if (this.key.length != thatKey.length) {
      false
    } else {
      var i = 0
      while (i < this.key.length && this.key(i) == thatKey(i)) i += 1
      i == this.key.length
    }
  }

  override def hashCode: Int = {
    31 + key.toSeq.hashCode()
  }

  override def toString: String = {
    new String(key, "UTF-8")
  }
}