package org.akoshterek.backgammon.board

import org.junit.Assert
import org.junit.Test

/**
  * @author Alex
  *         date 25.07.2015.
  */
class TestBoard {
  @Test def testInit(): Unit = {
    val board = Board.initialPosition
    val other = Board.positionFromID(board.positionID)
    Assert.assertEquals(board, other)
  }
}