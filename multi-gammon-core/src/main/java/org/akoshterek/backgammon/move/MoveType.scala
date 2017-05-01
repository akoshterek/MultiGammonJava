package org.akoshterek.backgammon.move

/**
 * @author Alex
 *         date 20.07.2015.
 */
sealed trait MoveType {
  def value: Int
}

object MoveType {
  case object MOVE_INVALID extends MoveType {val value = -1}
  case object MOVE_GAMEINFO extends MoveType {val value = 0}
  case object MOVE_NORMAL extends MoveType {val value = 1}
  case object MOVE_SETBOARD extends MoveType {val value = 6}
  case object MOVE_SETDICE extends MoveType {val value = 7}
}
