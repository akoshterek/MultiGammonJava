package org.akoshterek.backgammon.agent

import java.nio.file.{Path, Paths}

import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.eval.{Evaluator, Reward}
import org.akoshterek.backgammon.move.Move

object AbsAgent {
  private val AGENTS_SUBFOLDER: String = "bin/agents"
}

abstract class AbsAgent(override val fullName: String, override val path: Path) extends Agent {
  protected val _path: Path = Paths.get(path.toString, AbsAgent.AGENTS_SUBFOLDER)
  protected val _fullName: String = fullName

  override def clone: AbsAgent = {
    super.clone.asInstanceOf[AbsAgent]
  }

  def scoreMove(pm: Move): Reward = {
    val board: Board = Board.positionFromKey(pm.auch)
    pm.pc = Evaluator.getInstance.classifyPosition(board)
    evaluatePositionFull(board, pm.pc)
  }

  private def evaluatePositionFull(anBoard: Board, pc: PositionClass): Reward = {
    val reward = new Reward(evaluatePosition(anBoard, pc))
    applySanityCheck(anBoard, reward, pc)
  }

  private def applySanityCheck(anBoard: Board, reward: Reward, pc: PositionClass): Reward = {
    if (!PositionClass.isExact(pc) && supportsSanityCheck && !isLearnMode) {
      Evaluator.getInstance.sanityCheck(anBoard, reward)
    } else {
      reward
    }
  }
}