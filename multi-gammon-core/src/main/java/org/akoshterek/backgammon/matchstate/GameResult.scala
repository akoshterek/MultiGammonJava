package org.akoshterek.backgammon.matchstate

/**
  * Created by Alex on 29-04-17.
  */
sealed trait GameResult {
  def value: Int
}

object GameResult {
  case object PLAYING extends GameResult { val value = 0 }
  case object SINGLE extends GameResult { val value = 1 }
  case object GAMMON extends GameResult { val value = 2 }
  case object BACKGAMMON extends GameResult { val value = 3 }
}
