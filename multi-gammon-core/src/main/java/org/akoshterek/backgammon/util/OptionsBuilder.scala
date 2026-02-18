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
  // New options
  private val ALPHA_OPTION: String = "alpha"
  private val LAMBDA_OPTION: String = "lambda"
  private val GAMMA_OPTION: String = "gamma"
  private val EXPERIMENT_PATH_OPTION: String = "experiment-path"
  private val EXPERIMENT_TAG_OPTION: String = "experiment-tag"
  private val TRAINING_OPPONENTS_OPTION: String = "training-opponents"
  private val BENCHMARK_OPPONENTS_OPTION: String = "benchmark-opponents"
  private val USE_OUTPUT_BIAS_OPTION: String = "use-output-bias"

  private val GA_TRAINING_OPTION: String = "ga-train"
  private val GA_POPULATION_OPTION: String = "ga-population"
  private val GA_GENERATIONS_OPTION: String = "ga-generations"
  private val GA_ELITE_OPTION: String = "ga-elite"
  private val GA_MUTATION_RATE_OPTION: String = "ga-mutation-rate"
  private val GA_MUTATION_STRENGTH_OPTION: String = "ga-mutation-strength"
  private val options: Options = new Options
  private var commandLine: CommandLine = _

  def build(): Unit = {
    options.addOption("h", HELP_OPTION, false, "produce help message")
    options.addOption("c", LICENSE_OPTION, false, "show license")
    options.addOption("w", WARRANTY_OPTION, false, "show warranty")
    options.addOption("v", VERBOSE_OPTION, false, "show game log")
    options.addOption(Option.builder("A").longOpt(AGENT_OPTION).argName("agent").desc("agent(s) to train").hasArg.build)
    options.addOption(Option.builder("B").longOpt(BENCH_AGENT_OPTION).argName("benchmark agent").desc("benchmark agent").hasArg.build)
    options.addOption(Option.builder("T").longOpt(TRAIN_GAMES_OPTION).argName("training games").desc("number of games for training").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder("G").longOpt(BENCH_GAMES_OPTION).argName("benchmark games").desc("number of games for benchmark").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder("P").longOpt(BENCH_PERIOD_OPTION).argName("benchmark period").desc("benchmark every n games").hasArg.`type`(classOf[Number]).build)
    // New options
    options.addOption(Option.builder().longOpt(ALPHA_OPTION).argName("alpha").desc("learning rate alpha").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(LAMBDA_OPTION).argName("lambda").desc("TD eligibility trace lambda").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(GAMMA_OPTION).argName("gamma").desc("TD discount factor gamma").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(EXPERIMENT_PATH_OPTION).argName("experiment path").desc("experiment output path").hasArg.build)
    options.addOption(Option.builder().longOpt(EXPERIMENT_TAG_OPTION).argName("experiment run tag").desc("experiment run tag").hasArg.build)
    options.addOption(Option.builder().longOpt(TRAINING_OPPONENTS_OPTION).argName("training opponents").desc("training opponent mix (e.g. 'self:50,SimpleHeuristic:25,Random:25')").hasArg.build)
    options.addOption(Option.builder().longOpt(BENCHMARK_OPPONENTS_OPTION).argName("benchmark opponents").desc("comma-separated benchmark opponents (e.g. 'Random,SimpleHeuristic,Heuristic')").hasArg.build)
    options.addOption(Option.builder().longOpt(USE_OUTPUT_BIAS_OPTION).argName("use output bias").desc("enable output bias (default true, set to false for diverse opponents)").hasArg.build)

    options.addOption(Option.builder().longOpt(GA_TRAINING_OPTION).desc("use genetic algorithm training instead of TD learning").build)
    options.addOption(Option.builder().longOpt(GA_POPULATION_OPTION).argName("population size").desc("GA population size (default 20)").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(GA_GENERATIONS_OPTION).argName("generations").desc("GA number of generations (default 10)").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(GA_ELITE_OPTION).argName("elite count").desc("GA elite count (default 4)").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(GA_MUTATION_RATE_OPTION).argName("mutation rate").desc("GA mutation rate 0.0-1.0 (default 0.05)").hasArg.`type`(classOf[Number]).build)
    options.addOption(Option.builder().longOpt(GA_MUTATION_STRENGTH_OPTION).argName("mutation strength").desc("GA mutation strength (default 0.1)").hasArg.`type`(classOf[Number]).build)
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
      // New options
      bean.alpha = getFloatOption(ALPHA_OPTION, 0.01f)
      bean.lambda = getFloatOption(LAMBDA_OPTION, 0.7f)
      bean.gamma = getFloatOption(GAMMA_OPTION, 1.0f)
      bean.experimentPath = commandLine.getOptionValue(EXPERIMENT_PATH_OPTION, "")
      bean.experimentRunTag = commandLine.getOptionValue(EXPERIMENT_TAG_OPTION, "")
      bean.trainingOpponents = commandLine.getOptionValue(TRAINING_OPPONENTS_OPTION, "self:100")
      bean.benchmarkOpponents = commandLine.getOptionValue(BENCHMARK_OPPONENTS_OPTION, "SimpleHeuristic")
      bean.useOutputBias = commandLine.getOptionValue(USE_OUTPUT_BIAS_OPTION, "true").toBoolean

      bean.gaTraining = commandLine.hasOption(GA_TRAINING_OPTION)
      bean.gaPopulationSize = getIntOption(GA_POPULATION_OPTION, 20)
      bean.gaGenerations = getIntOption(GA_GENERATIONS_OPTION, 10)
      bean.gaEliteCount = getIntOption(GA_ELITE_OPTION, 4)
      bean.gaMutationRate = getFloatOption(GA_MUTATION_RATE_OPTION, 0.05f)
      bean.gaMutationStrength = getFloatOption(GA_MUTATION_STRENGTH_OPTION, 0.1f)
    }
    catch {
      case e: ParseException =>
        throw new RuntimeException(e)
    }
    bean
  }

  def printHelp(cmdLineSyntax: String): Unit = {
    val formatter: HelpFormatter = new HelpFormatter
    formatter.printHelp(cmdLineSyntax, options, true)
  }

  private def getNumberOption[T](optionName: String, defaultValue: T, converter: Number => T): T = {
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
      converter(number)
  }

  private def getIntOption(optionName: String, defaultValue: Int): Int = {
    getNumberOption(optionName, defaultValue, _.intValue)
  }

  private def getFloatOption(optionName: String, defaultValue: Float): Float = {
    getNumberOption(optionName, defaultValue, _.floatValue)
  }
}
