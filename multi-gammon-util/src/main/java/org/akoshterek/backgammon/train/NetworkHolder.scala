package org.akoshterek.backgammon.train

import java.io._

import org.akoshterek.backgammon.board.PositionClass
import org.apache.commons.lang3.SerializationUtils
import org.encog.neural.networks.BasicNetwork
import org.encog.neural.networks.training.propagation.TrainingContinuation
import org.encog.persist.EncogDirectoryPersistence
import resource.managed

/**
  * @author Alex
  *         date 22.09.2015.
  */
@SerialVersionUID(-7252112052540032945L)
object NetworkHolder {
  private val DIRECTORY: String = "bin/"

  def deserializeTrainedNetwork(agentSettings: AgentSettings, networkType: PositionClass): Option[BasicNetwork] = {
    val file: File = new File(getTrainedNetworkFileName(agentSettings, networkType))
    if (file.exists) {
      Some(EncogDirectoryPersistence.loadObject(file).asInstanceOf[BasicNetwork])
    }
    else {
      None
    }
  }

  def deserialize(template: NetworkHolder, agentSettings: AgentSettings): Option[NetworkHolder] = {
    val file = new File(getResumeFileName(agentSettings, template.networkType))
    if (!file.exists()) {
      None
    } else {
      Some(
        managed(new FileInputStream(file)).acquireFor(is => {
          SerializationUtils.deserialize[NetworkHolder](is)
        }).either.right.get
      )
    }
  }

  private def getResumeFileName(agentSettings: AgentSettings, networkType: PositionClass): String = {
    DIRECTORY + agentSettings.agentName + "-" + PositionClass.getNetworkType(networkType) + "-resume.egs"
  }

  private def getTrainedNetworkFileName(agentSettings: AgentSettings, networkType: PositionClass): String = {
    DIRECTORY + agentSettings.agentName + "-" + PositionClass.getNetworkType(networkType) + ".eg"
  }
}

@SerialVersionUID(-7252112052540032945L)
class NetworkHolder private[train](val network: BasicNetwork, val networkType: PositionClass) extends Serializable {
  private var _epoch: Int = 1
  private var _continuation: TrainingContinuation = _

  def serialize(agentSettings: AgentSettings): Unit = {
    val file: File = new File(NetworkHolder.getResumeFileName(agentSettings, networkType))
    if (!file.getParentFile.exists) {
      file.getParentFile.mkdirs
    }

    managed(new FileOutputStream(file)).acquireAndGet(os => {
      SerializationUtils.serialize(this, os)
    })
  }

  private[train] def serializeTrainedNetwork(agentSettings: AgentSettings) {
    val file: File = new File(NetworkHolder.getTrainedNetworkFileName(agentSettings, networkType))
    if (!file.getParentFile.exists) {
      file.getParentFile.mkdirs
    }
    EncogDirectoryPersistence.saveObject(file, network)
  }

  def epoch: Int = _epoch

  def incEpoch(): Unit = _epoch += 1

  def continuation: TrainingContinuation = _continuation
  def continuation_=(cont: TrainingContinuation): Unit = _continuation = cont
}