package org.akoshterek.backgammon.agent.raw

import org.akoshterek.backgammon.Constants
import org.akoshterek.backgammon.agent.AbsFlexAgent
import org.akoshterek.backgammon.agent.fa.SimpleEncogFA
import org.akoshterek.backgammon.agent.inputrepresentation.GnuBgCodec
import org.akoshterek.backgammon.agent.inputrepresentation.Tesauro89Codec
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward
import org.akoshterek.backgammon.util.Normalizer
import java.nio.file.Path

class RawBatch40(override val path: Path) extends AbsFlexAgent("RawBatch40", path) {
  contactRepresentation = new RawRepresentation(new Tesauro89Codec)
  raceRepresentation = new RawRepresentation(new GnuBgCodec)
  crashedRepresentation = new RawRepresentation(new GnuBgCodec)

  contactFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-Tesauro89-40-contact.eg"))
  raceFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-GnuBg-40-race.eg"))
  crashedFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource("org/akoshterek/backgammon/agent/raw/Raw-GnuBg-40-crashed.eg"))

  override def evalContact(board: Board): Reward = {
    new Reward(Normalizer.fromSmallerSigmoid(super.evalContact(board).data, Constants.NUM_OUTPUTS))
  }

  override def evalRace(board: Board): Reward = {
    new Reward(Normalizer.fromSmallerSigmoid(super.evalRace(board).data, Constants.NUM_OUTPUTS))
  }

  override def evalCrashed(board: Board): Reward = {
    new Reward(Normalizer.fromSmallerSigmoid(super.evalCrashed(board).data, Constants.NUM_OUTPUTS))
  }
}
