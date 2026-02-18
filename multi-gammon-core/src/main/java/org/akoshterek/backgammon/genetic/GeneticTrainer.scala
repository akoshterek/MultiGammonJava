package org.akoshterek.backgammon.genetic

import java.nio.file.{Files, Path}
import java.time.Instant
import java.util.Random
import org.akoshterek.backgammon.agent.Agent
import org.akoshterek.backgammon.dispatch.GameDispatcher

/**
 * Genetic algorithm trainer for evolving neural networks
 */
class GeneticTrainer(val basePath: Path,
                    val populationSize: Int = 20,
                    val eliteCount: Int = 4,
                    val mutationRate: Float = 0.05f,
                    val mutationStrength: Float = 0.1f,
                    val crossoverMethod: String = "uniform",
                    val gamesPerEvaluation: Int = 100,
                    val seed: Long = System.currentTimeMillis()) {
  
  require(eliteCount < populationSize, "Elite count must be less than population size")
  require(populationSize % 2 == 0, "Population size must be even")
  
  private val random = new Random(seed)
  private var population: Array[GeneticAgent] = Array.empty
  private var fitness: Array[Double] = Array.empty
  private var generation: Int = 0
  
  // Track best agent across all generations
  private var bestAgent: GeneticAgent = _
  private var bestFitness: Double = Double.MinValue
  private val metricsFile = basePath.resolve("genetic_metrics.csv").toFile
  private val checkpointTracker = new BestCheckpointTracker(basePath)
  if (!metricsFile.exists()) {
    Files.createDirectories(basePath)
    val writer = new java.io.PrintWriter(metricsFile)
    writer.println("generation,bestFitness,avgFitness,worstFitness,stdDev")
    writer.close()
  }
  
  /**
   * Initialize population with random weights
   */
  def initializePopulation(): Unit = {
    println(s"Initializing population of $populationSize agents...")
    population = Array.tabulate(populationSize)(i => GeneticAgent.createRandom(basePath, i))
    fitness = Array.fill(populationSize)(0.0)
    generation = 0
    println("Population initialized")
  }
  
  /**
   * Initialize population from seed network
   */
  def initializeFromSeed(seedWeights: org.akoshterek.backgammon.nn.NetworkWeights): Unit = {
    println(s"Initializing population of $populationSize agents from seed...")
    
    // First agent is exact copy of seed
    population = Array.tabulate(populationSize) { i =>
      val agent = GeneticAgent.fromWeights(basePath, seedWeights, i)
      
      // Mutate all except first agent (keep seed intact)
      if (i > 0) {
        WeightMutation.mutateGaussian(agent.network, mutationRate, mutationStrength, random)
      }
      
      agent
    }
    
    fitness = Array.fill(populationSize)(0.0)
    generation = 0
    println("Population initialized from seed")
  }
  
  /**
   * Evaluate fitness of entire population against opponents
   */
  def evaluatePopulation(opponents: Array[Agent]): Unit = {
    println(s"\n=== Generation $generation: Evaluating Population ===")
    
    for (i <- population.indices) {
      val agent = population(i)
      var totalPPG = 0.0
      
      // Evaluate against each opponent
      for (opponent <- opponents) {
        val dispatcher = new GameDispatcher(agent, opponent, None)
        dispatcher.playGames(gamesPerEvaluation, learn = false)
        
        val wonGames = dispatcher.getAgent1WonGames
        val wonPoints = dispatcher.getAgent1WonPoints
        val ppg = if (gamesPerEvaluation > 0) wonPoints.toDouble / gamesPerEvaluation else 0.0
        
        totalPPG += ppg
      }
      
      // Fitness is average PPG across all opponents
      fitness(i) = totalPPG / opponents.length
      
      if ((i + 1) % 5 == 0) {
        println(s"  Evaluated ${i + 1}/$populationSize agents...")
      }
    }
    
    // Track best agent
    val currentBestIdx = fitness.zipWithIndex.maxBy(_._1)._2
    val currentBest = fitness(currentBestIdx)
    
    if (currentBest > bestFitness) {
      bestFitness = currentBest
      bestAgent = population(currentBestIdx)
      println(s"  *** New best fitness: ${"%.4f".format(bestFitness)} PPG (Agent $currentBestIdx) ***")
      
      // Save checkpoint
      saveCheckpoint(generation)
    }
    
    val avgFitness = fitness.sum / fitness.length
    val worstFitness = fitness.min
    val variance = fitness.map(f => (f - avgFitness) * (f - avgFitness)).sum / fitness.length
    val stdDev = math.sqrt(variance)
    
    println(f"  Best: $currentBest%.4f | Avg: $avgFitness%.4f | Worst: $worstFitness%.4f | StdDev: $stdDev%.4f")
    
    // Log to CSV (use US locale to ensure period decimal separator)
    val writer = new java.io.PrintWriter(new java.io.FileOutputStream(metricsFile, true))
    writer.println(String.format(java.util.Locale.US, "%d,%f,%f,%f,%f", 
      generation.asInstanceOf[Object],
      currentBest.asInstanceOf[Object],
      avgFitness.asInstanceOf[Object],
      worstFitness.asInstanceOf[Object],
      stdDev.asInstanceOf[Object]))
    writer.close()
  }
  
  /**
   * Selection: Return indices of parents using tournament selection
   */
  private def tournamentSelect(tournamentSize: Int = 3): Int = {
    val contestants = Array.fill(tournamentSize)(random.nextInt(populationSize))
    contestants.maxBy(fitness(_))
  }
  
  /**
   * Create next generation through selection, crossover, and mutation
   */
  def evolvePopulation(): Unit = {
    println(s"\n=== Evolving to Generation ${generation + 1} ===")
    
    // Sort population by fitness
    val sortedIndices = fitness.zipWithIndex.sortBy(-_._1).map(_._2)
    
    val newPopulation = Array.ofDim[GeneticAgent](populationSize)
    
    // Elitism: Keep top performers
    println(s"  Preserving top $eliteCount elites...")
    for (i <- 0 until eliteCount) {
      val eliteIdx = sortedIndices(i)
      newPopulation(i) = population(eliteIdx)
      println(f"    Elite $i: Agent $eliteIdx (fitness: ${fitness(eliteIdx)}%.4f)")
    }
    
    // Create offspring through crossover and mutation
    println(s"  Creating ${populationSize - eliteCount} offspring...")
    var offspring = eliteCount
    while (offspring < populationSize) {
      // Select parents
      val parent1Idx = tournamentSelect()
      val parent2Idx = tournamentSelect()
      
      val parent1 = population(parent1Idx).network
      val parent2 = population(parent2Idx).network
      
      // Crossover
      val childNetwork = crossoverMethod match {
        case "uniform" => WeightMutation.crossoverUniform(parent1, parent2, random)
        case "blend" => WeightMutation.crossoverBlend(parent1, parent2, 0.5f)
        case "single-point" => WeightMutation.crossoverSinglePoint(parent1, parent2, random)
        case _ => WeightMutation.crossoverUniform(parent1, parent2, random)
      }
      
      // Mutation
      WeightMutation.mutateGaussian(childNetwork, mutationRate, mutationStrength, random)
      
      // Create new agent
      newPopulation(offspring) = new GeneticAgent(basePath, childNetwork, offspring)
      offspring += 1
    }
    
    population = newPopulation
    generation += 1
    println(s"  Evolution complete. Now at generation $generation")
  }
  
  /**
   * Run training for specified number of generations
   */
  def train(numGenerations: Int, opponents: Array[Agent]): Unit = {
    println(s"\n========================================")
    println(s"Starting GA Training")
    println(s"Population: $populationSize")
    println(s"Generations: $numGenerations")
    println(s"Elite count: $eliteCount")
    println(s"Mutation rate: $mutationRate")
    println(s"Mutation strength: $mutationStrength")
    println(s"Crossover method: $crossoverMethod")
    println(s"Games per evaluation: $gamesPerEvaluation")
    println(s"========================================\n")
    
    for (gen <- 0 until numGenerations) {
      evaluatePopulation(opponents)
      
      if (gen < numGenerations - 1) {
        evolvePopulation()
      }
    }
    
    println(s"\n========================================")
    println(s"Training Complete!")
    println(s"Best fitness achieved: ${"%.4f".format(bestFitness)} PPG")
    println(s"========================================\n")
  }
  
  /**
   * Get the best agent from training
   */
  def getBestAgent: GeneticAgent = bestAgent
  
  /**
   * Save checkpoint of current best agent
   */
  private def saveCheckpoint(gen: Int): Unit = {
    if (bestAgent != null) {
      val checkpoint = GACheckpoint(
        generation = gen,
        fitness = bestFitness,
        population = populationSize,
        eliteCount = eliteCount,
        mutationRate = mutationRate,
        mutationStrength = mutationStrength,
        inputSize = bestAgent.network.inputSize,
        hiddenSize = bestAgent.network.hiddenSize,
        outputSize = bestAgent.network.outputSize,
        weights = bestAgent.network.saveWeights(),
        timestamp = Instant.now().toString
      )

      // Use String.format with US locale to ensure period decimal separator
      val filename = String.format(java.util.Locale.US, "ga_checkpoint_gen%04d_fitness_%.4f.json",
        gen.asInstanceOf[Object],
        bestFitness.asInstanceOf[Object])
      val checkpointPath = basePath.resolve(filename)
      
      GACheckpoint.save(checkpoint, checkpointPath)
      checkpointTracker.updateIfBetter(checkpoint, checkpointPath)
    }
  }
  
  /**
   * Save best agent's weights and statistics
   */
  def saveBestAgent(filename: String): Unit = {
    if (bestAgent != null) {
      val stats = bestAgent.network.analyzeWeights()
      println(s"\nBest agent statistics:")
      println(s"  Fitness: $bestFitness PPG")
      println(s"  Weight mean: ${stats.mean}")
      println(s"  Weight stdDev: ${stats.stdDev}")
      println(s"  Weight maxAbs: ${stats.maxAbs}")
      
      val bestPath = checkpointTracker.getBestCheckpointPath()
      bestPath match {
        case Some(path) => println(s"  Best checkpoint: ${path.getFileName}")
        case None => println(s"  No checkpoint saved yet")
      }
    }
  }
}
