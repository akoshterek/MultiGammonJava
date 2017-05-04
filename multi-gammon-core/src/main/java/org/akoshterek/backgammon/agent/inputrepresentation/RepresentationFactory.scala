package org.akoshterek.backgammon.agent.inputrepresentation

import org.akoshterek.backgammon.agent.raw.RawRepresentation

object RepresentationFactory {
  def createInputRepresentation(fullName: String): InputRepresentation = {
    val fullNameLower: String = fullName.toLowerCase
    val tokens: Array[String] = fullNameLower.split("-")
    val representationName: String = tokens(0)
    val codecName: String = tokens(1)
    val codec: PointCodec = createCodec(codecName)
    representationName match {
      case "raw" => new RawRepresentation(codec)
      case _ => throw new IllegalArgumentException("Unknown input representation " + representationName)
    }
  }

  private def createCodec(codecName: String): PointCodec = {
    codecName match {
      case "sutton" => SuttonCodec
      case "tesauro89" => Tesauro89Codec
      case "tesauro92" => Tesauro92Codec
      case "gnubg" => GnuBgCodec
      case _ => throw new IllegalArgumentException("Unknown point codec " + codecName)
    }
  }
}