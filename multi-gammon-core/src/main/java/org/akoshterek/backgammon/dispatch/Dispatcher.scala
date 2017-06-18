package org.akoshterek.backgammon.dispatch

import java.nio.file.{Path, Paths}

import org.akoshterek.backgammon.License
import org.akoshterek.backgammon.agent.{AbsAgent, Agent, AgentFactory}
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.util.{OptionsBean, OptionsBuilder}
import org.apache.commons.lang3.time.DurationFormatUtils

class Dispatcher {
  OptionsBuilder.build()
  private var options: OptionsBean = _

  def init(args: Array[String]): Boolean = {
    License.printBanner()
    options = OptionsBuilder.parse(args)
    if (options.isHelp) {
      OptionsBuilder.printHelp("multigammon")
      false
    }
    else if (options.isLicense) {
      License.printLicense()
      false
    }
    else if (options.isWarranty) {
      License.printWarranty()
      false
    } else {
      val currentPath: Path = Paths.get("").toAbsolutePath.normalize
      Evaluator.setSeed(16000000L)
      Evaluator.basePath = currentPath
      true
    }
  }

  def run() {
    val elapsedTime: Long = time {
      val agentNames: Vector[String] = options.agentNames
      val benchAgenName: String = options.benchmarkAgentName

      agentNames.foreach { agentName =>
        runAgentIteration(agentName,
          benchAgenName,
          options.trainingGames,
          options.benchmarkGames,
          options.benchmarkPeriod
        )
      }
    }

    System.out.println("Elapsed time " + formatTime(elapsedTime))
  }

  private def time(f: => Unit): Long = {
    val s = System.currentTimeMillis
    f
    System.currentTimeMillis - s
  }


  private def runAgentIteration(agentName: String, benchAgentName: String, trainingGames: Int,
                                benchmarkGames: Int, benchmarkPeriod: Int) {
    val agent1: Agent = AgentFactory.createAgent(agentName)
    val benchAgent: Agent = AgentFactory.createAgent(benchAgentName)

    assert(agent1 != null && benchAgent != null)

    var agent2: Agent = null
    if (agent1.isInstanceOf[Cloneable]) {
      agent2 = agent1.asInstanceOf[AbsAgent].clone
    }
    else {
      agent2 = AgentFactory.createAgent(agentName)
    }

    runIteration(agent1, benchAgent, agent2, trainingGames, benchmarkGames, benchmarkPeriod)
  }

  private def runIteration(agent1: Agent, benchAgent: Agent, agent2: Agent, trainGames: Int, benchmarkGames: Int, benchmarkPeriod: Int) {
    val gameDispatcher: GameDispatcher = new GameDispatcher(agent1, agent2)
    gameDispatcher.showLog = options.isVerbose

    val dir = Paths.get(gameDispatcher.agent1.path.toString, "log").toFile
    if (!dir.exists()) {
      dir.mkdirs()
    }

    if (trainGames > 0) {
      for (_ <- 0 until trainGames by benchmarkPeriod) {
        //training
        gameDispatcher.playGames(Math.min(benchmarkPeriod, trainGames), learn = true)
        agent1.save()

        //benchmark
        val benchDispatcher: GameDispatcher = new GameDispatcher(agent1, benchAgent)
        benchDispatcher.showLog = options.isVerbose
        benchDispatcher.playGames(benchmarkGames, learn = false)
        benchDispatcher.printStatistics()
      }
    }
    else {
      //benchmark
      val benchDispatcher: GameDispatcher = new GameDispatcher(agent1, benchAgent)
      benchDispatcher.showLog = options.isVerbose
      benchDispatcher.playGames(benchmarkGames, learn = false)
      benchDispatcher.printStatistics()
    }
  }

  private def formatTime(millis: Long): String = {
    DurationFormatUtils.formatDuration(millis, "HH:mm:ss")
  }
}