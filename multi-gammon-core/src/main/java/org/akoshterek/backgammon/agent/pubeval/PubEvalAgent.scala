package org.akoshterek.backgammon.agent.pubeval

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Reward
import java.nio.file.Path

class PubEvalAgent(val path: Path) extends AbsAgent(path) {
    fullName = "PubEval"
    final private val eval: PubEval = new PubEval

    def evalContact(board: Board): Reward = {
        val reward: Reward = new Reward
        val pos: Array[Int] = new Array[Int](28)
        preparePos(board, pos)

        val race: Int = if (curPC.getValue <= PositionClass.CLASS_RACE.getValue) 1 else 0
        reward.data(Constants.OUTPUT_WIN) = eval.pubeval(race, pos)
        reward
    }

    private def preparePos(board: Board, pos: Array[Int]) {
        val tmpBoard: Board = new Board(board)
        tmpBoard.swapSides()
        val (opponent, self) = tmpBoard.chequersCount

        for (i <- 0 until 24) {
            pos(i + 1) = tmpBoard.anBoard(Board.SELF)(i)
            if (tmpBoard.anBoard(Board.OPPONENT)(23 - i) != 0) {
                pos(i + 1) = -tmpBoard.anBoard(Board.OPPONENT)(23 - i)
            }
        }

        pos(25) = tmpBoard.anBoard(Board.SELF)(Board.BAR)
        pos(0) = -tmpBoard.anBoard(Board.OPPONENT)(Board.BAR)
        pos(26) = 15 - self
        pos(27) = -(15 - opponent)
    }
}