package org.akoshterek.backgammon.agent.raw

import java.nio.file.Path

import com.google.common.base.CaseFormat
import org.akoshterek.backgammon.agent.AbsFlexAgent
import org.akoshterek.backgammon.agent.fa.SimpleEncogFA
import org.akoshterek.backgammon.agent.inputrepresentation._
import org.akoshterek.backgammon.board.Board
import org.akoshterek.backgammon.eval.Reward

class RawBatch(override val path: Path,
               override val contactRepresentation: InputRepresentation,
               override val crashedRepresentation: InputRepresentation,
               override val raceRepresentation: InputRepresentation,
               contactName: String,
               crashedName: String,
               raceName: String) extends AbsFlexAgent("RawBatch", path) {

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
    tokens(1) match {
      case "best" => createBest(path)
      case "worst" => createWorst(path)
      case _ =>
        require(tokens.length == 4, "Agent name should consist of for tokens")
        require(tokens(0) == "raw", "Agent name should begin with 'raw'")
        val representation = new RawRepresentation(createCodec(tokens(1)))
        val hidden = tokens(2).toInt
        val activation = tokens(3)
        val prefix = "Raw-" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, tokens(1)) + "-" + hidden + "-" + activation
        new RawBatch(path, representation, representation, representation,
          prefix + "-contact", prefix + "-crashed", prefix + "-race")
    }
  }

  def apply(path: Path, contact: String, crashed: String, race: String): RawBatch = {
    new RawBatch(path,
      new RawRepresentation(createCodec(contact.toLowerCase.split('-')(1))),
      new RawRepresentation(createCodec(crashed.toLowerCase.split('-')(1))),
      new RawRepresentation(createCodec(race.toLowerCase.split('-')(1))),
      contact, crashed, race)
  }

  private def createBest(path: Path): RawBatch = {
    val contact = RawStat.stat.minBy(x => x.errorContact).name + "-contact"
    val crashed = RawStat.stat.minBy(x => x.errorCrashed).name + "-crashed"
    val race = RawStat.stat.minBy(x => x.errorRace).name + "-race"
    RawBatch(path, contact, crashed, race)
  }

  private def createWorst(path: Path): RawBatch = {
    val contact = RawStat.stat.maxBy(x => x.errorContact).name + "-contact"
    val crashed = RawStat.stat.maxBy(x => x.errorCrashed).name + "-crashed"
    val race = RawStat.stat.maxBy(x => x.errorRace).name + "-race"
    RawBatch(path, contact, crashed, race)
  }

  private def createCodec(codecName: String): PointCodec = codecName match {
    case "sutton" => SuttonCodec
    case "tesauro89" => Tesauro89Codec
    case "tesauro92" => Tesauro92Codec
    case "gnubg" => GnuBgCodec
    case _ => throw new IllegalArgumentException("Unknown point codec " + codecName)
  }
}