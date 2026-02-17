package org.akoshterek.backgammon.dispatch

import java.nio.file.{Files, Path, Paths}
import org.akoshterek.backgammon.License
import org.akoshterek.backgammon.agent.{Agent, AgentFactory, CopyableAgent, OpponentConfig, OpponentSelector}
import org.akoshterek.backgammon.agent.raw.RawTd40
import org.akoshterek.backgammon.dice.PseudoRandomDiceRoller
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
      val currentPath: Path = {
        val basePath = Paths.get("").toAbsolutePath.normalize
        val experimentPath = if (options.experimentPath.isBlank) basePath else basePath.resolve(options.experimentPath)
        Files.createDirectories(experimentPath)
        experimentPath
      }
      Evaluator.diceRoller = PseudoRandomDiceRoller(16000000L)
      Evaluator.basePath = currentPath
      true
    }
  }

  def run(): Unit = {
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
                                benchmarkGames: Int, benchmarkPeriod: Int): Unit = {
    val agent1: Agent = AgentFactory.createAgent(agentName, options)
    val benchAgent: Agent = AgentFactory.createAgent(benchAgentName, options)

    assert(agent1 != null && benchAgent != null)

    // Parse opponent configuration
    val opponentConfig = OpponentConfig.parse(
      options.trainingOpponents,
      options.benchmarkOpponents
    )

    // Create OpponentSelector for diverse training if agent is copyable
    val opponentSelector = agent1 match {
      case copyableAgent: CopyableAgent[Agent] =>
        Some(new OpponentSelector(copyableAgent, opponentConfig, Evaluator.basePath, 16000001L))
      case _ => None
    }

    val agent2: Agent = if (opponentSelector.isEmpty) {
      agent1 match {
        case copyableAgent: CopyableAgent[_] =>
          copyableAgent.copyAgent()
        case _ =>
          AgentFactory.createAgent(agentName, options)
      }
    } else {
      null  // Will use OpponentSelector instead
    }

    runIteration(agent1, benchAgent, agent2, opponentSelector, trainingGames, benchmarkGames, benchmarkPeriod)
  }

  private def runIteration(agent1: Agent, benchAgent: Agent, agent2: Agent, 
                          opponentSelector: Option[OpponentSelector],
                          trainGames: Int, benchmarkGames: Int, benchmarkPeriod: Int): Unit = {
    val gameDispatcher: GameDispatcher = new GameDispatcher(agent1, agent2, opponentSelector)
    gameDispatcher.showLog = options.isVerbose

    val dir = Paths.get(gameDispatcher.agent1.path.toString)
    Files.createDirectories(dir)

    if (trainGames > 0) {
      for (_ <- 0 until trainGames by benchmarkPeriod) {
        //training
        gameDispatcher.playGames(Math.min(benchmarkPeriod, trainGames), learn = true)
        agent1.save()

        // Run benchmarks against all configured opponents
        opponentSelector match {
          case Some(selector) =>
            selector.getBenchmarkOpponents().foreach { benchOpponent =>
              benchmark(agent1, benchOpponent, benchmarkGames)
            }
          case None =>
            // Fallback to old behavior
            benchmark(agent1, benchAgent, benchmarkGames)
        }
      }
    }
    else {
      benchmark(agent1, benchAgent, benchmarkGames)
    }
  }

  private def benchmark(agent1: Agent, benchAgent: Agent, benchmarkGames: Int): Unit = {
    val benchDispatcher: GameDispatcher = new GameDispatcher(agent1, benchAgent)
    benchDispatcher.showLog = options.isVerbose
    benchDispatcher.playGames(benchmarkGames, learn = false)
    benchDispatcher.printStatistics(Evaluator.basePath, options.experimentRunTag)
    
    // Save to benchmarks CSV if agent supports it
    agent1 match {
      case rawAgent: RawTd40 =>
        val totalGames = benchDispatcher.getAgent1WonGames + benchDispatcher.getAgent2WonGames
        val ppg = if (totalGames > 0) {
          benchDispatcher.getAgent1WonPoints.toFloat / totalGames.toFloat
        } else 0f
        val wins = benchDispatcher.getAgent1WonGames
        val losses = benchDispatcher.getAgent2WonGames
        val points = benchDispatcher.getAgent1WonPoints
        rawAgent.saveBenchmarkResult(benchAgent.fullName, ppg, wins, losses, points)
      case _ => // Other agents don't have benchmark tracking
    }
  }

  private def formatTime(millis: Long): String = {
    DurationFormatUtils.formatDuration(millis, "HH:mm:ss")
  }
}
