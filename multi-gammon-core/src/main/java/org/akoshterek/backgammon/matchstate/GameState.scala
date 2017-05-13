package org.akoshterek.backgammon.matchstate

/**
  * Created by Alex on 29-04-17.
  */
sealed trait GameState {
    val value: Int
}

object GameState {
    case object GAME_NONE extends GameState { val value = 0 }
    case object GAME_PLAYING extends GameState { val value = 1 }
    case object GAME_OVER extends GameState { val value = 2 }
}