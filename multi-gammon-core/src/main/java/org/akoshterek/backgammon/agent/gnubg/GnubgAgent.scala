package org.akoshterek.backgammon.agent.gnubg

import java.nio.file.Path

import org.akoshterek.backgammon.Constants._
import org.akoshterek.backgammon.agent.AbsAgent
import org.akoshterek.backgammon.agent.gnubg.nn.GnuNeuralNets
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Gammons._
import org.akoshterek.backgammon.eval.{Evaluator, Reward}

/**
  * @author Alex
  *         date 12.09.2015.
  */
class GnubgAgent(override val path: Path) extends AbsAgent("Gnubg", path) {
  private val representation = new GnuBgRepresentation()
  override val supportsSanityCheck = true
  override val supportsBearoff = true
  override val needsInvertedEval = true

  private def evalWithInvert(eval: (Board) => Reward)(board: Board): Reward = eval(board.swapSides).invert

  override def evalRace(board: Board): Reward = evalWithInvert(evalRaceInternal)(board)

  override def evalCrashed(board: Board): Reward = evalWithInvert(evalCrashedInternal)(board)

  override def evalContact(board: Board): Reward = evalWithInvert(evalContactInternal)(board)

  private def evalRaceInternal(board: Board): Reward = {
    val inputs = representation.calculateRaceInputs(board)
    val reward = Reward.rewardArray[Float]
    GnuNeuralNets.nnRace.evaluate(inputs, reward)
    val (totMen0, totMen1) = board.chequersCount

    // a set flag for every possible outcome
    var any: Int = 0
    if (totMen1 == 15) {
      any |= OG_POSSIBLE
    }

    if (totMen0 == 15) {
      any |= G_POSSIBLE
    }

    any = GnubgAgent.calculateBackgammonPossibility(board, any)
    GnubgAgent.evaluatePossibleBackgammon(board, any, reward)
    // sanity check will take care of rest
    Reward(reward)
  }

  private def evalCrashedInternal(board: Board): Reward = {
    val inputs = representation.calculateCrashedInputs(board)
    val reward = Reward.rewardArray[Float]
    GnuNeuralNets.nnCrashed.evaluate(inputs, reward)
    Reward(reward)
  }

  private def evalContactInternal(board: Board): Reward = {
    val inputs = representation.calculateContactInputs(board)
    val reward = Reward.rewardArray[Float]
    GnuNeuralNets.nnContact.evaluate(inputs, reward)
    Reward(reward)
  }
}

object GnubgAgent {
  private[gnubg] def evaluatePossibleBackgammon(board: Board, gammonsFlag: Int, reward: Array[Float]): Unit = {
    val reward = Reward.rewardArray[Float]
    if ((gammonsFlag & (BG_POSSIBLE | OBG_POSSIBLE)) != 0) {
      // side that can have the backgammon 
      val side = if ((gammonsFlag & BG_POSSIBLE) != 0) 1 else 0
      val pr = Evaluator.raceBGprob(board, side)

      if (pr > 0.0) {
        if (side == 1) {
          reward(OUTPUT_WINBACKGAMMON) = pr

          if (reward(OUTPUT_WINGAMMON) < reward(OUTPUT_WINBACKGAMMON))
            reward(OUTPUT_WINGAMMON) = reward(OUTPUT_WINBACKGAMMON)
        } else {
          reward(OUTPUT_LOSEBACKGAMMON) = pr

          if (reward(OUTPUT_LOSEGAMMON) < reward(OUTPUT_LOSEBACKGAMMON))
            reward(OUTPUT_LOSEGAMMON) = reward(OUTPUT_LOSEBACKGAMMON)
        }
      } else {
        if (side == 1)
          reward(OUTPUT_WINBACKGAMMON) = 0.0f
        else
          reward(OUTPUT_LOSEBACKGAMMON) = 0.0f
      }
    }
  }

  private[gnubg] def calculateBackgammonPossibility(board: Board, gammonsFlag: Int): Int = {
    var any = gammonsFlag

    if ((any & OG_POSSIBLE) != 0 && board(Board.SELF).slice(18, 18 + 6).lastIndexWhere(_ > 0) >= 0) {
      any |= OBG_POSSIBLE
    }

    if ((any & G_POSSIBLE) != 0 && board(Board.OPPONENT).slice(18, 18 + 6).lastIndexWhere(_ > 0) >= 0) {
      any |= BG_POSSIBLE
    }

    any
  }
}