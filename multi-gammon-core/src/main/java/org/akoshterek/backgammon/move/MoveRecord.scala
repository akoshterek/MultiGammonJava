package org.akoshterek.backgammon.move

/**
  * @author Alex
  *         date 20.07.2015.
  */
class MoveRecord {
  // Common variables
  // type of the move
  var mt: MoveType = MoveType.MOVE_INVALID
  // annotation
  var sz: String = _
  // move record is for player
  var fPlayer: Int = 0

  // luck analysis (shared between MOVE_SETDICE and MOVE_NORMAL)
  // dice rolled
  var anDice: (Int, Int) = (0, 0)
  // evaluation of the moves
  var ml: MoveList = new MoveList

  // "private" data
  // game information
  var g: XMoveGameInfo = new XMoveGameInfo
  // chequerplay move
  var n: XMoveNormal = new XMoveNormal
  // setting up board
  var sb: XMoveSetBoard = new XMoveSetBoard
}