package org.akoshterek.backgammon.agent

import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.board.PositionClass
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.move.Move
import java.nio.file.Path
import java.nio.file.Paths

object AbsAgent {
    private val AGENTS_SUBFOLDER: String = "bin/agents"
}

abstract class AbsAgent(override val fullName: String, override val path: Path) extends Agent {
    protected val _path: Path = Paths.get(path.toString, AbsAgent.AGENTS_SUBFOLDER)
    protected val _fullName: String = fullName

    override def clone: AbsAgent = {
        try {
            super.clone.asInstanceOf[AbsAgent]
        }
        catch {
            case e: CloneNotSupportedException => throw new RuntimeException(e)
        }
    }

    def scoreMoves(moves: Array[Move], count: Int) {
        moves.slice(0, count).foreach(scoreMove)
    }

    def scoreMove(pm: Move): Reward = {
        val anBoardTemp: Board = Board.positionFromKey(pm.auch)
        anBoardTemp.swapSides()
        pm.pc = Evaluator.getInstance.classifyPosition(anBoardTemp)
        var arEval: Reward = evaluatePositionFull(anBoardTemp, pm.pc)
        if (needsInvertedEval) {
            //TODO why?
            arEval = arEval.invert
        }
        else if (PositionClass.isExact(pm.pc)) {
            if (pm.pc == PositionClass.CLASS_OVER || supportsBearoff) {
                arEval = arEval.invert
            }
        }
        pm.arEvalMove = arEval
        pm.rScore = arEval.equity
        arEval
    }

    private def evaluatePositionFull(anBoard: Board, pc: PositionClass): Reward = {
        val reward: Reward = new Reward(evaluatePosition(anBoard, pc))
        if (!PositionClass.isExact(pc) && supportsSanityCheck && !isLearnMode) {
            Evaluator.getInstance.sanityCheck(anBoard, reward)
        }
        reward
    }
}