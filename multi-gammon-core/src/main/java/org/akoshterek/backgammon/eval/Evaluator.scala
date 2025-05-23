package org.akoshterek.backgammon.eval

import java.nio.file.Path
import org.akoshterek.backgammon.bearoff.{Bearoff, BearoffContext}
import org.akoshterek.backgammon.board.{Board, PositionClass}
import org.akoshterek.backgammon.dice.{DiceRoller, PseudoRandomDiceRoller}

/**
  * @author Alex
  *         date: 18-06-17.
  */
object Evaluator {
  var basePath: Path = _
  var diceRoller: DiceRoller = _

  var pbc1: BearoffContext = BearoffContext("/org/akoshterek/backgammon/agent/gnubg/gnubg_os0.bd")
  var pbc2: BearoffContext = BearoffContext("/org/akoshterek/backgammon/agent/gnubg/gnubg_ts0.bd")

  def classifyPosition(anBoard: Board): PositionClass = PositionClassificator.classifyPosition(anBoard)

  def evalOver(anBoard: Board): Reward = EvalOver.apply(anBoard)

  def evalBearoff2(anBoard: Board): Reward = Bearoff.bearoffEval(pbc2, anBoard)

  def evalBearoff1(anBoard: Board): Reward = Bearoff.bearoffEval(pbc1, anBoard)

  def sanityCheck(board: Board, reward: Reward): Reward = SanityCheck.apply(board, reward, pbc1)

  def raceBGprob(anBoard: Board , side: Int): Float = RaceBgProb.apply(anBoard, side, pbc1)
}
