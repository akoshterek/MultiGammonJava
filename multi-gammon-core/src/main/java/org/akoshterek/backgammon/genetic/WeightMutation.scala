package org.akoshterek.backgammon.genetic

import scala.util.Random

/**
 * Utilities for genetic algorithm operations on neural network weights
 */
object WeightMutation {
  
  /**
   * Apply Gaussian noise mutation to a network
   * @param network Network to mutate (in-place)
   * @param mutationRate Probability of mutating each weight (0.0 to 1.0)
   * @param mutationStrength Standard deviation of Gaussian noise
   * @param random Random number generator
   */
  def mutateGaussian(network: SimpleNeuralNetwork, 
                     mutationRate: Float, 
                     mutationStrength: Float,
                     random: Random): Unit = {
    // Mutate input-hidden weights
    for (h <- 0 until network.hiddenSize; i <- 0 until network.inputSize) {
      if (random.nextFloat() < mutationRate) {
        val noise = random.nextGaussian().toFloat * mutationStrength
        network.wInputHidden(h)(i) += noise
      }
    }
    
    // Mutate hidden-output weights
    for (o <- 0 until network.outputSize; h <- 0 until network.hiddenSize) {
      if (random.nextFloat() < mutationRate) {
        val noise = random.nextGaussian().toFloat * mutationStrength
        network.wHiddenOutput(o)(h) += noise
      }
    }
    
    // Mutate hidden biases
    for (h <- 0 until network.hiddenSize) {
      if (random.nextFloat() < mutationRate) {
        val noise = random.nextGaussian().toFloat * mutationStrength
        network.bHidden(h) += noise
      }
    }
    
    // Mutate output biases
    for (o <- 0 until network.outputSize) {
      if (random.nextFloat() < mutationRate) {
        val noise = random.nextGaussian().toFloat * mutationStrength
        network.bOutput(o) += noise
      }
    }
  }
  
  /**
   * Uniform crossover between two parent networks
   * Each weight has 50% chance of coming from either parent
   * @param parent1 First parent network
   * @param parent2 Second parent network
   * @param random Random number generator
   * @return New offspring network
   */
  def crossoverUniform(parent1: SimpleNeuralNetwork,
                       parent2: SimpleNeuralNetwork,
                       random: Random): SimpleNeuralNetwork = {
    require(parent1.inputSize == parent2.inputSize && 
            parent1.hiddenSize == parent2.hiddenSize &&
            parent1.outputSize == parent2.outputSize,
            "Parent networks must have identical architecture")
    
    val offspring = parent1.cloneNetwork()
    
    // Crossover input-hidden weights
    for (h <- 0 until offspring.hiddenSize; i <- 0 until offspring.inputSize) {
      if (random.nextBoolean()) {
        offspring.wInputHidden(h)(i) = parent2.wInputHidden(h)(i)
      }
    }
    
    // Crossover hidden-output weights
    for (o <- 0 until offspring.outputSize; h <- 0 until offspring.hiddenSize) {
      if (random.nextBoolean()) {
        offspring.wHiddenOutput(o)(h) = parent2.wHiddenOutput(o)(h)
      }
    }
    
    // Crossover hidden biases
    for (h <- 0 until offspring.hiddenSize) {
      if (random.nextBoolean()) {
        offspring.bHidden(h) = parent2.bHidden(h)
      }
    }
    
    // Crossover output biases
    for (o <- 0 until offspring.outputSize) {
      if (random.nextBoolean()) {
        offspring.bOutput(o) = parent2.bOutput(o)
      }
    }
    
    offspring
  }
  
  /**
   * Blend crossover between two parent networks
   * Each weight is a weighted average of both parents
   * @param parent1 First parent network
   * @param parent2 Second parent network
   * @param alpha Blending factor (0.5 = equal blend)
   * @return New offspring network
   */
  def crossoverBlend(parent1: SimpleNeuralNetwork,
                     parent2: SimpleNeuralNetwork,
                     alpha: Float = 0.5f): SimpleNeuralNetwork = {
    require(parent1.inputSize == parent2.inputSize && 
            parent1.hiddenSize == parent2.hiddenSize &&
            parent1.outputSize == parent2.outputSize,
            "Parent networks must have identical architecture")
    
    val offspring = parent1.cloneNetwork()
    val beta = 1.0f - alpha
    
    // Blend input-hidden weights
    for (h <- 0 until offspring.hiddenSize; i <- 0 until offspring.inputSize) {
      offspring.wInputHidden(h)(i) = alpha * parent1.wInputHidden(h)(i) + beta * parent2.wInputHidden(h)(i)
    }
    
    // Blend hidden-output weights
    for (o <- 0 until offspring.outputSize; h <- 0 until offspring.hiddenSize) {
      offspring.wHiddenOutput(o)(h) = alpha * parent1.wHiddenOutput(o)(h) + beta * parent2.wHiddenOutput(o)(h)
    }
    
    // Blend hidden biases
    for (h <- 0 until offspring.hiddenSize) {
      offspring.bHidden(h) = alpha * parent1.bHidden(h) + beta * parent2.bHidden(h)
    }
    
    // Blend output biases
    for (o <- 0 until offspring.outputSize) {
      offspring.bOutput(o) = alpha * parent1.bOutput(o) + beta * parent2.bOutput(o)
    }
    
    offspring
  }
  
  /**
   * Single-point crossover between two parent networks
   * Split at a random point, take first part from parent1, second from parent2
   */
  def crossoverSinglePoint(parent1: SimpleNeuralNetwork,
                           parent2: SimpleNeuralNetwork,
                           random: Random): SimpleNeuralNetwork = {
    require(parent1.inputSize == parent2.inputSize && 
            parent1.hiddenSize == parent2.hiddenSize &&
            parent1.outputSize == parent2.outputSize,
            "Parent networks must have identical architecture")
    
    val offspring = parent1.cloneNetwork()
    
    // Count total weights
    val totalWeights = (parent1.inputSize * parent1.hiddenSize) + 
                       (parent1.hiddenSize * parent1.outputSize) +
                       parent1.hiddenSize + parent1.outputSize
    
    val crossoverPoint = random.nextInt(totalWeights)
    var currentWeight = 0
    
    // Process input-hidden weights
    var switched = false
    for (h <- 0 until offspring.hiddenSize; i <- 0 until offspring.inputSize) {
      if (currentWeight >= crossoverPoint && !switched) switched = true
      if (switched) {
        offspring.wInputHidden(h)(i) = parent2.wInputHidden(h)(i)
      }
      currentWeight += 1
    }
    
    // Process hidden-output weights
    for (o <- 0 until offspring.outputSize; h <- 0 until offspring.hiddenSize) {
      if (currentWeight >= crossoverPoint && !switched) switched = true
      if (switched) {
        offspring.wHiddenOutput(o)(h) = parent2.wHiddenOutput(o)(h)
      }
      currentWeight += 1
    }
    
    // Process biases
    for (h <- 0 until offspring.hiddenSize) {
      if (currentWeight >= crossoverPoint && !switched) switched = true
      if (switched) {
        offspring.bHidden(h) = parent2.bHidden(h)
      }
      currentWeight += 1
    }
    
    for (o <- 0 until offspring.outputSize) {
      if (currentWeight >= crossoverPoint && !switched) switched = true
      if (switched) {
        offspring.bOutput(o) = parent2.bOutput(o)
      }
      currentWeight += 1
    }
    
    offspring
  }
}
