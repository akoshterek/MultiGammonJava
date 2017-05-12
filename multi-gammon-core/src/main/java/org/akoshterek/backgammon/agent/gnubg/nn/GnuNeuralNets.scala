package org.akoshterek.backgammon.agent.gnubg.nn

import java.io.{DataInput, IOException}

import com.google.common.io.LittleEndianDataInputStream
import org.akoshterek.backgammon.agent.gnubg.GnubgAgent
import resource.managed

/**
  * Created by oleksii on 12-May-17.
  */
object GnuNeuralNets {
  private var _nnContact: NeuralNet = _
  private var _nnRace: NeuralNet = _
  private var _nnCrashed: NeuralNet = _

  def nnContact: NeuralNet = _nnContact
  def nnRace: NeuralNet = _nnRace
  def nnCrashed: NeuralNet = _nnCrashed

  managed(new LittleEndianDataInputStream(classOf[GnubgAgent].getResourceAsStream("/org/akoshterek/backgammon/agent/gnubg/gnubg.wd"))).acquireAndGet(is => {
    checkBinaryWeights(is)
    _nnContact = NeuralNet.loadBinary(is)
    _nnRace = NeuralNet.loadBinary(is)
    _nnCrashed = NeuralNet.loadBinary(is)
  })

  @throws[IOException]
  @throws[IllegalArgumentException]("Invalid weights file")
  private def checkBinaryWeights(is: DataInput) {
    val magic: Float = is.readFloat
    val version: Float = is.readFloat
    if (magic != 472.3782f || version != 1.0f) {
      throw new IllegalArgumentException("Invalid weights file")
    }
  }
}
