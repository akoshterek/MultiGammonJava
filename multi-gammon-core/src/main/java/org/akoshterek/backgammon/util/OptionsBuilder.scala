package org.akoshterek.backgammon.util

import org.apache.commons.cli._

object OptionsBuilder {
  private val HELP_OPTION: String = "help"
  private val WARRANTY_OPTION: String = "warranty"
  private val LICENSE_OPTION: String = "copying"
  private val VERBOSE_OPTION: String = "verbose"
  private val AGENT_OPTION: String = "agent"
  private val BENCH_AGENT_OPTION: String = "bench-agent"
  private val DEFAULT_BENCH_AGENT: String = "Heuristic"
  private val TRAIN_GAMES_OPTION: String = "train-games"
  private val BENCH_GAMES_OPTION: String = "bench-games"
  private val BENCH_PERIOD_OPTION: String = "bench-period"
  private val options: Options = new Options
  private var commandLine: CommandLine = null

  def build() {
    options.addOption("h", HELP_OPTION, false, "produce help message")
    options.addOption("c", LICENSE_OPTION, false, "show license")
    options.addOption("w", WARRANTY_OPTION, false, "show warranty")
    options.addOption("v", VERBOSE_OPTION, false, "show game log")
    options.addOption(Option.builder("A").longOpt(AGENT_OPTION).argName("agent").desc("agent(s) to train").hasArg.build)
    options.addOption(Option.builder("B").longOpt(BENCH_AGENT_OPTION).argName("benchmark agent").desc("benchmark agent").hasArg.build)
    options.addOption(Option.builder("T").longOpt(TRAIN_GAMES_OPTION).argName("training games").desc("number of games for training").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder("G").longOpt(BENCH_GAMES_OPTION).argName("benchmark games").desc("number of games for benchmark").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder("P").longOpt(BENCH_PERIOD_OPTION).argName("benchmark period").desc("benchmark every n games").hasArg.`type`(classOf[Number]).build)
  }

  def parse(args: Array[String]): OptionsBean = {
    val parser: CommandLineParser = new DefaultParser
    val bean: OptionsBean = new OptionsBean
    try {
      commandLine = parser.parse(options, args)
      bean.isHelp = commandLine.hasOption(HELP_OPTION)
      bean.isWarranty = commandLine.hasOption(WARRANTY_OPTION)
      bean.isLicense = commandLine.hasOption(LICENSE_OPTION)
      bean.isVerbose  = commandLine.hasOption(VERBOSE_OPTION)
      bean.agentNames = commandLine.getOptionValues(AGENT_OPTION)
      bean.benchmarkAgentName = commandLine.getOptionValue(BENCH_AGENT_OPTION, DEFAULT_BENCH_AGENT)
      bean.trainingGames = getIntOption(TRAIN_GAMES_OPTION, 0)
      bean.benchmarkGames = getIntOption(BENCH_GAMES_OPTION, 1000)
      bean.benchmarkPeriod = getIntOption(BENCH_PERIOD_OPTION, 10000)
    }
    catch {
      case e: ParseException =>
        throw new RuntimeException(e)
    }
    bean
  }

  def printHelp(cmdLineSyntax: String) {
    val formatter: HelpFormatter = new HelpFormatter
    formatter.printHelp(cmdLineSyntax, options, true)
  }

  private def getIntOption(optionName: String, defaultValue: Int): Int = {
    var number: Number = null
    try {
      number = commandLine.getParsedOptionValue(optionName).asInstanceOf[Number]
    }
    catch {
      case e: ParseException =>
        throw new RuntimeException(e)
    }
    if (number == null)
      defaultValue
    else
      number.intValue
  }
}