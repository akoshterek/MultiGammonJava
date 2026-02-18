package org.akoshterek.backgammon.genetic

import java.nio.file.Paths
import org.akoshterek.backgammon.agent.{AgentFactory, SimpleHeuristicAgent, HeuristicAgent, Agent}
import org.akoshterek.backgammon.eval.Evaluator
import org.akoshterek.backgammon.util.OptionsBean

/**
 * Simple runner for testing genetic algorithm training
 */
object GARunner {
  
  def main(args: Array[String]): Unit = {
    println("\n" + "="*60)
    println("Genetic Algorithm Training Runner")
    println("="*60 + "\n")
    
    // Configuration
    val basePath = Paths.get("experiments/012_ga_test")
    val populationSize = 10
    val generations = 5
    val gamesPerEval = 50
    val eliteCount = 2
    val mutationRate = 0.05f
    val mutationStrength = 0.1f
    
    // Create opponents
    println("Creating opponent agents...")
    val opponents = Array[Agent](
      new SimpleHeuristicAgent(Evaluator.basePath),
      new HeuristicAgent(Evaluator.basePath)
    )
    println(s"  - SimpleHeuristicAgent")
    println(s"  - HeuristicAgent")
    println()
    
    // Create trainer
    val trainer = new GeneticTrainer(
      basePath = basePath,
      populationSize = populationSize,
      eliteCount = eliteCount,
      mutationRate = mutationRate,
      mutationStrength = mutationStrength,
      crossoverMethod = "uniform",
      gamesPerEvaluation = gamesPerEval
    )
    
    // Initialize population
    trainer.initializePopulation()
    
    // Train
    trainer.train(generations, opponents)
    
    // Save best agent
    trainer.saveBestAgent("best_genetic_agent.json")
    
    println("\nTraining complete! Check " + basePath + "/genetic_metrics.csv for results.")
  }
}
