package org.akoshterek.backgammon.board;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alex
 *         date 25.07.2015.
 */
public class TestBoard {
    @Test
    public void testInit() {
        Board board = new Board();
        board.initBoard();
        Board other = Board.positionFromID(board.positionID());
        Assert.assertEquals(board, other);
    }
}
