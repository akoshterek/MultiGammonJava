package org.akoshterek.backgammon.eval

import java.nio.file.Path

import org.akoshterek.backgammon.bearoff.{Bearoff, BearoffContext}
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.apache.commons.math3.distribution.UniformIntegerDistribution
import org.apache.commons.math3.random.Well19937c

/**
  * Created by Alex on 18-06-17.
  */
object Evaluator {
  private val rng = new Well19937c
  private val distribution = new UniformIntegerDistribution(rng, 1, 6)
  var basePath: Path = _

  var pbc1: BearoffContext = BearoffContext("/org/akoshterek/backgammon/agent/gnubg/gnubg_os0.bd")
  var pbc2: BearoffContext = BearoffContext("/org/akoshterek/backgammon/agent/gnubg/gnubg_ts0.bd")

  def nextDice: Int = distribution.sample

  def setSeed(seed: Long): Unit = {
    rng.setSeed(seed)
  }

  def classifyPosition(anBoard: Board): PositionClass = PositionClassificator.classifyPosition(anBoard)

  def evalOver(anBoard: Board): Reward = EvalOver.apply(anBoard)

  def evalBearoff2(anBoard: Board): Reward = Bearoff.bearoffEval(pbc2, anBoard)

  def evalBearoff1(anBoard: Board): Reward = Bearoff.bearoffEval(pbc1, anBoard)

  def sanityCheck(board: Board, reward: Reward): Reward = SanityCheck.apply(board, reward, pbc1)

  def raceBGprob(anBoard: Board , side: Int): Float = RaceBgProb.apply(anBoard, side, pbc1)
}
