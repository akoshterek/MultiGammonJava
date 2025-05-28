package org.akoshterek.backgammon.agent

import java.nio.file.{Path, Paths}

object AbsAgent {
  private val AGENTS_SUBFOLDER: String = "bin/agents"
}

abstract class AbsAgent(override val fullName: String, basePath: Path) extends Agent with CopyableAgent[AbsAgent] {
  override val path: Path = Paths.get(basePath.toString, AbsAgent.AGENTS_SUBFOLDER)
}
