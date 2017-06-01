package org.akoshterek.backgammon.agent.raw

import java.nio.file.Path

import com.google.common.base.CaseFormat
import org.akoshterek.backgammon.agent.AbsFlexAgent
import org.akoshterek.backgammon.agent.fa.SimpleEncogFA
import org.akoshterek.backgammon.agent.inputrepresentation._
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

class RawBatch(override val path: Path,
               representation: InputRepresentation,
               contactName: String,
               crashedName: String,
               raceName: String) extends AbsFlexAgent("RawBatch", path) {
  contactRepresentation = representation
  raceRepresentation = representation
  crashedRepresentation = representation

  contactFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource(s"org/akoshterek/backgammon/agent/raw/$contactName.eg"))
  raceFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource(s"org/akoshterek/backgammon/agent/raw/$crashedName.eg"))
  crashedFa = new SimpleEncogFA(SimpleEncogFA.loadNNFromResource(s"org/akoshterek/backgammon/agent/raw/$raceName.eg"))

  override def evalContact(board: Board): Reward = {
    super.evalContact(board)
  }

  override def evalRace(board: Board): Reward = {
    super.evalRace(board)
  }

  override def evalCrashed(board: Board): Reward = {
    super.evalCrashed(board)
  }
}

object RawBatch {
  def apply(path: Path, fullName: String): RawBatch = {
    val tokens = fullName.toLowerCase.split('-')
    //Raw-Sutton-40-elu
    require(tokens.length == 4, "Agent name should consist of for tokens")
    require(tokens(0) == "raw", "Agent name should begin with 'raw'")
    val representation = new RawRepresentation(createCodec(tokens(1)))
    val hidden = tokens(2).toInt
    val activation = tokens(3)
    val prefix = "Raw-" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, tokens(1)) + "-" + hidden + "-" + activation
    new RawBatch(path, representation, prefix + "-contact", prefix + "-crashed", prefix + "-race")
  }

  private def createCodec(codecName: String): PointCodec = codecName match {
    case "sutton" => SuttonCodec
    case "tesauro89" => Tesauro89Codec
    case "tesauro92" => Tesauro92Codec
    case "gnubg" => GnuBgCodec
    case _ => throw new IllegalArgumentException("Unknown point codec " + codecName)
  }
}