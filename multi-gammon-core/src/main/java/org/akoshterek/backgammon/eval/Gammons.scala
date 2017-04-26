package org.akoshterek.backgammon.eval

object Gammons {
  /* gammon possible by side on roll */
  val G_POSSIBLE: Int = 0x1
  /* backgammon possible by side on roll */
  val BG_POSSIBLE: Int = 0x2
  /* gammon possible by side not on roll */
  val OG_POSSIBLE: Int = 0x4
  /* backgammon possible by side not on roll */
  val OBG_POSSIBLE: Int = 0x8
}