# Evolutionary Strategies for Backgammon Training - Detailed Guide

**Date:** February 2026
**Context:** Deep dive into ES as alternative to TD(λ) for self-play training

---

## Core Concept

Instead of using gradients to update weights, treat the neural network weights as a **genome** that evolves through natural selection:

1. **Population**: Maintain 20-50 different networks (individuals)
2. **Fitness evaluation**: Play games to measure how good each network is
3. **Selection**: Keep the best performers
4. **Reproduction**: Create offspring by combining/mutating winner weights
5. **Repeat**: New generation replaces old, gradually improving

**Key insight**: You don't need calculus or backpropagation - just evaluate "is this network better than that one?" and breed the winners.

---

## Three Practical Algorithms

### 3A. Simple Genetic Algorithm (GA)

**Simplest approach - great starting point**

```scala
// Pseudo-code for Scala implementation
class GeneticTrainer(populationSize: Int = 30) {
  var population: Array[TdNeuralNetwork] =
    Array.fill(populationSize)(createRandomNetwork())

  def evolve(generations: Int): Unit = {
    for (gen <- 1 to generations) {
      // 1. Evaluate fitness (parallel)
      val fitness = population.par.map(network =>
        evaluateFitness(network, gamesPerEval = 100)
      ).toArray

      // 2. Selection (keep top 30%)
      val survivors = selectTop(population, fitness, keepFraction = 0.3)

      // 3. Reproduction (breed to fill population)
      val offspring = Array.fill(populationSize - survivors.length) {
        val parent1 = randomSelect(survivors)
        val parent2 = randomSelect(survivors)
        crossover(parent1, parent2) // blend weights
      }

      // 4. Mutation (add random noise)
      offspring.foreach(child => mutate(child, mutationRate = 0.1))

      // 5. New generation
      population = survivors ++ offspring

      println(s"Gen $gen: Best fitness = ${fitness.max}")
    }
  }

  def evaluateFitness(network: Network, games: Int): Double = {
    // Play games vs baseline (Random, Heuristic, or self)
    var wins = 0
    for (g <- 0 until games) {
      if (playGame(network, opponent = baselineNetwork)) wins += 1
    }
    wins.toDouble / games  // Win rate as fitness
  }

  def crossover(parent1: Network, parent2: Network): Network = {
    val child = parent1.clone()
    // Blend each weight: 50/50 chance from each parent
    for (h <- 0 until hiddenSize; i <- 0 until inputSize) {
      if (random.nextBoolean()) {
        child.wInputHidden(h)(i) = parent2.wInputHidden(h)(i)
      }
    }
    // Same for hidden→output weights and biases
    for (o <- 0 until outputSize; h <- 0 until hiddenSize) {
      if (random.nextBoolean()) {
        child.wHiddenOutput(o)(h) = parent2.wHiddenOutput(o)(h)
      }
    }
    child
  }

  def mutate(network: Network, rate: Double): Unit = {
    // Add Gaussian noise to subset of weights
    for (h <- 0 until hiddenSize; i <- 0 until inputSize) {
      if (random.nextDouble() < rate) {
        network.wInputHidden(h)(i) += random.nextGaussian().toFloat * 0.1f
      }
    }
    // Same for hidden→output weights and biases
    for (o <- 0 until outputSize; h <- 0 until hiddenSize) {
      if (random.nextDouble() < rate) {
        network.wHiddenOutput(o)(h) += random.nextGaussian().toFloat * 0.1f
      }
    }
  }

  def selectTop(population: Array[Network], fitness: Array[Double],
                keepFraction: Double): Array[Network] = {
    val keepCount = (population.length * keepFraction).toInt
    val sortedIndices = fitness.zipWithIndex.sortBy(-_._1).take(keepCount).map(_._2)
    sortedIndices.map(i => population(i))
  }

  def randomSelect(survivors: Array[Network]): Network = {
    survivors(random.nextInt(survivors.length))
  }
}
```

**Training time**: ~100 generations × 30 networks × 100 games = **300K games**
**Laptop parallelization**: Use `.par` to evaluate all 30 networks simultaneously

**Key parameters:**
- `populationSize = 30`: Good balance for laptop (30 networks in memory ~30 MB)
- `keepFraction = 0.3`: Only top 30% reproduce (strong selection pressure)
- `mutationRate = 0.1`: 10% of weights get noise per mutation
- `mutationMagnitude = 0.1`: Gaussian noise std dev

---

### 3B. CMA-ES (Covariance Matrix Adaptation Evolution Strategy)

**More sophisticated - adapts mutation step sizes automatically**

CMA-ES maintains a **multivariate Gaussian distribution** over weight space and adapts both the mean (best solution) and covariance (search direction) based on which samples succeed.

**Why it's better:**
- Automatically tunes mutation step sizes (no manual tuning)
- Learns correlated mutations (if weight A and B should change together)
- State-of-the-art for black-box optimization
- Used in robotics, control systems, hyperparameter optimization

**Why it's harder:**
- Covariance matrix is huge for neural networks (40×198 = 7,920 weights → 31M matrix entries!)
- Memory intensive: O(n²) where n = number of weights
- Slow for large networks due to matrix operations

**Solution for backgammon**: Use **Sep-CMA-ES** (separable covariance)
- Only learns diagonal covariance (independent per weight)
- Reduces memory from O(n²) to O(n)
- Still much better than fixed mutation rates
- Practical for networks with <10K weights

```scala
class SepCMAES(weightsCount: Int, populationSize: Int = 30) {
  var mean: Array[Float] = Array.fill(weightsCount)(0f)  // Current best weights
  var sigma: Array[Float] = Array.fill(weightsCount)(0.3f)  // Step sizes per weight
  val learningRate = 0.1f
  val initialSigma = 0.3f

  def evolve(generations: Int): Unit = {
    for (gen <- 1 to generations) {
      // 1. Sample population from N(mean, diag(sigma²))
      val population = Array.fill(populationSize) {
        sampleNetwork(mean, sigma)
      }

      // 2. Evaluate fitness (parallel)
      val fitness = population.par.map(evaluateFitness).toArray

      // 3. Select top performers (top 50%)
      val topIndices = fitness.zipWithIndex
        .sortBy(-_._1)
        .take(populationSize / 2)
        .map(_._2)

      // 4. Update mean toward winners (weighted by rank)
      val topWeights = topIndices.map(i => networkToWeights(population(i)))
      for (w <- 0 until weightsCount) {
        val newMean = topWeights.map(_(w)).sum / topWeights.length
        mean(w) += learningRate * (newMean - mean(w))
      }

      // 5. Update step sizes (adaptive mutation strength)
      // Increase sigma if consistent direction, decrease if random
      for (w <- 0 until weightsCount) {
        val variance = topWeights.map(ws =>
          (ws(w) - mean(w)) * (ws(w) - mean(w))
        ).sum / topWeights.length

        sigma(w) = math.sqrt(variance).toFloat * 1.2f  // Slightly increase exploration

        // Clip to reasonable bounds
        sigma(w) = math.min(1.0f, math.max(0.01f, sigma(w)))
      }

      println(s"Gen $gen: Best=${fitness.max}, AvgSigma=${sigma.sum/sigma.length}")
    }
  }

  def sampleNetwork(mean: Array[Float], sigma: Array[Float]): Network = {
    val weights = Array.tabulate(weightsCount) { w =>
      mean(w) + sigma(w) * random.nextGaussian().toFloat
    }
    weightsToNetwork(weights)
  }

  def networkToWeights(network: Network): Array[Float] = {
    // Flatten all weights into single array
    val inputHiddenFlat = network.wInputHidden.flatten
    val hiddenOutputFlat = network.wHiddenOutput.flatten
    val biasesFlat = network.bHidden ++ network.bOutput
    inputHiddenFlat ++ hiddenOutputFlat ++ biasesFlat
  }

  def weightsToNetwork(weights: Array[Float]): Network = {
    // Unflatten weights back into network structure
    val network = new TdNeuralNetwork(inputSize, hiddenSize, outputSize, 0, 0, 0)
    var idx = 0

    // Input→Hidden weights
    for (h <- 0 until hiddenSize; i <- 0 until inputSize) {
      network.wInputHidden(h)(i) = weights(idx)
      idx += 1
    }

    // Hidden→Output weights
    for (o <- 0 until outputSize; h <- 0 until hiddenSize) {
      network.wHiddenOutput(o)(h) = weights(idx)
      idx += 1
    }

    // Biases
    for (h <- 0 until hiddenSize) {
      network.bHidden(h) = weights(idx)
      idx += 1
    }
    for (o <- 0 until outputSize) {
      network.bOutput(o) = weights(idx)
      idx += 1
    }

    network
  }
}
```

**Pros**:
- Self-tuning (no mutation rate hyperparameter needed)
- Faster convergence than simple GA (often 2-3× fewer generations)
- Learns which weights are sensitive vs robust
- Used by DeepMind for some RL tasks

**Cons**:
- More complex implementation (~300 lines vs ~150 for GA)
- Still needs ~1M games for backgammon (fewer generations but more games per gen)
- Requires careful initialization of sigma

**Training time**: ~150 generations × 30 networks × 150 games = **675K games**

---

### 3C. OpenAI ES (Natural Evolution Strategies)

**Modern twist: use gradient-like updates without computing gradients**

OpenAI's 2017 paper "Evolution Strategies as a Scalable Alternative to Reinforcement Learning" showed ES can match policy gradients on some tasks. Key insight: **perturb weights with random noise, use reward as a signal to estimate "pseudo-gradient"**.

**The clever math:**
```
∇J(θ) ≈ (1/nσ) Σᵢ F(θ + σεᵢ) εᵢ
```
Where:
- θ = current weights
- εᵢ = random Gaussian noise
- F(θ + σεᵢ) = fitness of perturbed network
- This approximates gradient without backprop!

```scala
class OpenAIES(baseNetwork: Network, populationSize: Int = 50) {
  val learningRate = 0.01f
  val noiseSigma = 0.02f
  var currentWeights = baseNetwork.getWeights()  // Flatten to 1D array

  def evolve(generations: Int): Unit = {
    for (gen <- 1 to generations) {
      // 1. Generate random noise perturbations (parallel safe - use seeds)
      val noises = Array.fill(populationSize) {
        val seed = random.nextLong()
        (seed, generateGaussianNoise(seed, currentWeights.length))
      }

      // 2. Evaluate positive and negative perturbations (parallel)
      val rewards = noises.par.map { case (seed, noise) =>
        val posWeights = addArrays(currentWeights, scaleArray(noise, noiseSigma))
        val negWeights = addArrays(currentWeights, scaleArray(noise, -noiseSigma))

        val posReward = evaluate(posWeights)
        val negReward = evaluate(negWeights)

        (noise, posReward - negReward)  // Finite difference
      }.toArray

      // 3. Estimate gradient using rewards as weights
      val pseudoGradient = rewards.map { case (noise, reward) =>
        scaleArray(noise, reward)
      }.reduce(addArrays)

      // Normalize by population size and noise sigma
      val normalizedGradient = scaleArray(pseudoGradient,
        1.0f / (populationSize * noiseSigma))

      // 4. Update weights (gradient ascent)
      currentWeights = addArrays(currentWeights,
        scaleArray(normalizedGradient, learningRate))

      // 5. Apply to base network
      baseNetwork.setWeights(currentWeights)

      // 6. Evaluate current best
      val bestReward = evaluate(currentWeights)
      println(s"Gen $gen: Best reward = $bestReward")
    }
  }

  def generateGaussianNoise(seed: Long, length: Int): Array[Float] = {
    val rng = new Random(seed)
    Array.fill(length)(rng.nextGaussian().toFloat)
  }

  def evaluate(weights: Array[Float]): Double = {
    val network = weightsToNetwork(weights)
    evaluateFitness(network, gamesPerEval = 50)
  }

  def addArrays(a: Array[Float], b: Array[Float]): Array[Float] = {
    a.zip(b).map { case (x, y) => x + y }
  }

  def scaleArray(a: Array[Float], scale: Float): Array[Float] = {
    a.map(_ * scale)
  }
}
```

**Why this is clever**:
- Acts like gradient descent but only needs forward passes
- Can parallelize 50+ evaluations per generation
- Robust to local minima (noise provides exploration)
- **Antithetic sampling**: evaluate both +ε and -ε for variance reduction

**Downside**:
- Needs 2× evaluations per sample (positive + negative perturbation)
- Finite differences are noisy → needs large population (50-100)
- More sensitive to hyperparameters (learning rate, noise sigma)

**Training time**: ~200 generations × 50 networks × 2 × 50 games = **1M games**
(But highly parallelizable - good for clusters)

**Key insight from OpenAI paper**: ES is competitive with RL on Atari games when scaled to thousands of CPUs. On a laptop, it's less compelling, but still interesting for exploration robustness.

---

## Implementation for Backgammon

### Architecture Compatibility

Your current network: **198 → 40 → 1 = 7,961 weights**
- Input→Hidden: 198 × 40 = 7,920 weights
- Hidden biases: 40 weights
- Hidden→Output: 40 × 1 = 40 weights  (wait, this seems off - let me recalculate)

Actually for 198→40→1:
- Input→Hidden weights: 198 × 40 = 7,920
- Hidden biases: 40
- Hidden→Output weights: 40 × 1 = 40
- Output bias: 1
- **Total: 8,001 weights**

**Memory for population**:
- 30 networks × 8,000 weights × 4 bytes/float = 960 KB per generation
- Can easily fit 50-100 networks in memory (~3-6 MB)
- Laptop RAM: 16 GB → can handle 1000+ networks if needed

### Fitness Function Options

**Option A: Win rate vs fixed opponent**
```scala
def evaluateFitness(network: Network): Double = {
  var wins = 0
  val gamesPerEval = 100

  for (_ <- 0 until gamesPerEval) {
    val opponent = RandomAgent  // or HeuristicAgent
    if (playGame(network, opponent) > 0) wins += 1
  }

  wins.toDouble / gamesPerEval
}
```
**Pros:**
- Fast to evaluate (Random opponent is fast)
- Clear signal (win = good, lose = bad)
- Deterministic opponent (less variance)

**Cons:**
- Risk: might overfit to beating Random
- Doesn't drive diversity (all networks converge to beat Random the same way)
- No continued improvement once 95%+ win rate

**Option B: Round-robin tournament**
```scala
def evaluateFitness(network: Network, population: Array[Network]): Double = {
  var totalReward = 0.0

  // Play against all other networks in population
  for (opponent <- population if opponent != network) {
    val result = playGame(network, opponent)  // +1 win, 0 loss, 0.5 draw
    totalReward += result
  }

  totalReward / (population.length - 1)
}
```
**Pros:**
- Self-play within population (like TD(λ))
- Drives continuous improvement through competition
- Automatically adjusts difficulty (opponents get stronger)
- Promotes diversity (different strategies can coexist)

**Cons:**
- Slower: 30 networks × 29 opponents = 870 games per generation
- Higher variance in fitness
- Can have rock-paper-scissors dynamics

**Option C: Mixed opponents (recommended)**
```scala
def evaluateFitness(network: Network, population: Array[Network]): Double = {
  var totalReward = 0.0
  val gamesPerOpponentType = 20

  // 40 games vs Random (baseline check)
  for (_ <- 0 until gamesPerOpponentType * 2) {
    if (playGame(network, RandomAgent) > 0) totalReward += 1.0
  }

  // 20 games vs Heuristic (intermediate challenge)
  for (_ <- 0 until gamesPerOpponentType) {
    if (playGame(network, HeuristicAgent) > 0) totalReward += 2.0  // Worth more
  }

  // 20 games vs random population member (self-play)
  for (_ <- 0 until gamesPerOpponentType) {
    val opponent = population(random.nextInt(population.length))
    if (playGame(network, opponent) > 0) totalReward += 1.5
  }

  totalReward / 100.0  // Normalize to [0, 1.5] range
}
```
**Pros:**
- Balanced evaluation (multiple skill levels)
- Fast (100 games per eval, not 870)
- Clear progression metric (beat Random → beat Heuristic)

**Recommendation**:
- **Early training (Gen 0-50)**: Use Option A (vs Random) for fast iterations
- **Mid training (Gen 50-150)**: Switch to Option C (mixed opponents)
- **Late training (Gen 150+)**: Use Option B (round-robin) for final polish

---

### Parallel Execution

Your laptop likely has 8-16 threads. ES is **embarrassingly parallel** - perfect fit!

```scala
import scala.collection.parallel.CollectionConverters._

// Make population collection parallel
val populationPar = population.par

// Each thread evaluates one network independently
val fitness = populationPar.map { network =>
  evaluateFitness(network)  // No shared state!
}.toArray

// No synchronization needed, no race conditions
```

**Expected speedup**:
- 8 cores → 6-7× speedup (some overhead from thread management)
- 16 cores → 10-12× speedup

**Wall clock time comparison**:
- TD(λ): 1.5M games sequential = ~24 hours
- ES: 600K games / 8 cores = ~75K sequential equivalent = **~1.5 hours**

**Important**: Fitness evaluation must be thread-safe
- Each network instance is independent ✅
- Random dice rolls: use thread-local RNG or seed-based generation ✅
- No shared game state ✅

---

### Hyperparameters

**Population size**: 30-50
- **Too small (10-20)**: Not enough diversity, premature convergence to local optima
- **Too large (100+)**: Slow generations, dilutes selection pressure, wastes compute
- **Sweet spot for laptop: 30** (good diversity, manageable memory, fills 8 cores with multiple evals)

**Games per evaluation**: 50-100
- More games = less noisy fitness, but slower
- Start with **50 games** for fast iterations
- Increase to **100 games** when approaching convergence (Gen 100+)
- Trade-off: 50 games × 30 nets = 1500 games/gen → ~2 minutes per generation

**Mutation rate** (for GA): 0.1-0.3
- Fraction of weights to mutate per offspring
- 0.1 = 10% of weights get noise (~800 weights out of 8,000)
- 0.3 = 30% mutation (high exploration)
- **Adaptive schedule**: Start 0.3, decay to 0.1 over 200 generations

**Mutation magnitude** (for GA): 0.02-0.1 (Gaussian std dev)
- How much noise to add to mutated weights
- 0.1 = large changes (exploration)
- 0.02 = small tweaks (exploitation)
- **Adaptive schedule**: Start 0.1, decay to 0.02 as fitness plateaus

```scala
def adaptiveMutationMagnitude(generation: Int, maxGen: Int): Float = {
  val progress = generation.toFloat / maxGen
  0.1f * (1f - progress) + 0.02f * progress  // Linear decay
}
```

**Selection pressure**: Keep top 20-30%
- Elite selection: only winners breed
- **Too aggressive (top 10%)**: Loses diversity, converges too fast
- **Too weak (top 50%)**: Slow progress, diluted signal
- **Recommended: top 30%** = 9 survivors out of 30 → 21 offspring per generation

**Crossover strategy**:
- Uniform crossover: 50/50 per weight (simple, works well)
- Single-point crossover: split at random index (can preserve weight clusters)
- Arithmetic crossover: blend parent weights (0.5×parent1 + 0.5×parent2)
- **Recommended: Uniform** for simplicity

---

## Expected Training Timeline

### Generations vs Games

Assuming 30 networks, 100 games per eval:

| Generation | Total Games | Expected Performance |
|------------|-------------|----------------------|
| 0 | 0 | Random play (~5% vs Random) |
| 10-20 | 30K-60K | Learns basic moves (~30-40% vs Random) |
| 50-100 | 150K-300K | Decent play (~60-70% vs Random) |
| 150-200 | 450K-600K | Strong play (~80-85% vs Random) |
| 250+ | 750K+ | Asymptotic (~85-90% vs Random) |

### Convergence Patterns

**Early training (Gen 0-50)**:
- Rapid fitness increase
- High diversity (many different strategies)
- Exploration dominates

**Mid training (Gen 50-150)**:
- Slower steady improvement
- Convergence begins (population looks similar)
- Balance exploration/exploitation

**Late training (Gen 150+)**:
- Plateauing fitness
- Low diversity (population converges)
- Exploitation dominates

**Warning signs**:
- **Premature convergence**: All networks look identical by Gen 50
  - Solution: Increase mutation rate, larger population
- **No improvement after Gen 100**: Stuck in local optimum
  - Solution: Restart with different random seed, inject random newcomers

---

### Computation Time

**Wall clock estimates** (8-core laptop, ~10 games/second):

```
Generation time = (population × games_per_eval) / (cores × games_per_second)
                = (30 × 100) / (8 × 10)
                = 3000 / 80
                = ~38 seconds per generation
```

**Full training run**:
- 200 generations × 38 seconds = **~2 hours**
- Total games: 200 × 30 × 100 = **600K games**

Compare to TD(λ):
- 1.5M games sequential at 10 games/second = **~42 hours**

**ES is 20× faster in wall clock time** due to parallelization!

But note: ES needs more total games for same quality (600K ES ≈ 300K TD in quality)

---

## Pros for Backgammon

1. **No gradient computation**
   - No backpropagation code needed
   - Simpler training loop (~200 lines vs ~500 for TD)
   - No learning rate decay schedules
   - No eligibility traces

2. **Highly parallelizable**
   - Uses all CPU cores effectively
   - Linear speedup with core count
   - 8 cores → 20× faster wall clock than TD(λ)

3. **Robust optimization**
   - Doesn't get stuck in local minima (population diversity)
   - No vanishing/exploding gradients
   - Works with non-smooth fitness landscapes
   - Can handle noisy fitness evaluations

4. **Simple hyperparameters**
   - Just mutation rate and population size
   - Less sensitive than learning rate in TD
   - Easy to tune (just run 20-gen pilots)

5. **Works with non-differentiable objectives**
   - Could optimize for match equity directly (non-smooth)
   - Could optimize for positional features (discrete)
   - Could use multiple objectives (Pareto optimization)

---

## Cons for Backgammon

1. **Less sample efficient**
   - Needs 2-3× more games than TD(λ) for same quality
   - 600K games to reach 80% vs Random
   - TD(λ) reaches 80% at ~400K games

2. **Memory overhead**
   - 30 networks in memory simultaneously (~30 MB)
   - TD(λ) only needs 1 network (~1 MB)
   - Not a problem for laptops, but limits mobile deployment

3. **Noisy convergence**
   - Fitness evaluation has variance (only 100 games)
   - Sometimes worse networks survive by luck
   - Can oscillate instead of smooth improvement
   - Solution: More games per eval (but slower)

4. **Harder to resume**
   - Need to save entire population, not just one network
   - Checkpoint is 30× larger (30 MB vs 1 MB)
   - More complex checkpoint logic

5. **No credit assignment**
   - Fitness is single scalar per network
   - Doesn't know which moves were good/bad
   - TD(λ) learns from every move
   - ES only learns from final outcome

6. **Diversity loss**
   - Population converges to similar solutions
   - Late training becomes local search
   - Need tricks to maintain diversity (niching, novelty search)

---

## When to Choose ES over TD(λ)

### Choose ES if:

✅ **You want to utilize all 8+ CPU cores**
- ES gives 6-8× speedup on 8-core laptop
- 2 hours to strong play vs 42 hours for TD

✅ **You're willing to trade sample efficiency for wall-clock speed**
- 600K games in 2 hours (ES) vs 400K games in 42 hours (TD)

✅ **You want simpler code**
- No backprop, no eligibility traces
- ~200 lines vs ~500 lines

✅ **You're experimenting and want robustness**
- Less sensitive to hyperparameters
- No learning rate disasters
- Population diversity provides exploration

✅ **You want to optimize non-differentiable objectives**
- Match equity, tournament ranking, style preferences

---

### Choose TD(λ) if:

✅ **You want proven method for backgammon**
- Tesauro's TD-Gammon gold standard
- Extensive literature and tuning advice

✅ **You value sample efficiency**
- 400K games to strong play
- Learns from every move (not just outcomes)

✅ **You're doing long single-threaded runs**
- Let it run overnight for days
- Don't need quick iterations

✅ **You have working TD implementation**
- Which you do! Don't fix what isn't broken
- Checkpointing already implemented

✅ **You want to analyze learning dynamics**
- TD error tracking
- Weight evolution over time
- Value function visualization

---

## Hybrid Approach (Best of Both Worlds)

**Most interesting**: Combine both methods!

### Phase 1: Bootstrap with ES (20-30 generations, ~60K games, ~1 hour)
- Fast parallel training to get decent weights
- Explores weight space broadly
- Avoids random walk of early TD training
- Gets to ~60% win rate vs Random

### Phase 2: Fine-tune with TD(λ) (100K-200K games, ~10 hours)
- Precise gradient-based refinement
- TD(λ) excels at final polish
- Learns from every move
- Gets to 85%+ win rate vs Random

**Total time**: ~11 hours vs 42 hours for pure TD
**Total games**: 200K vs 1.5M for pure TD
**Best of both**: ES parallelism + TD sample efficiency

### Implementation:

```scala
// Phase 1: ES bootstrap
val esTrainer = new GeneticTrainer(populationSize = 30)
esTrainer.evolve(generations = 30)  // ~60K games, ~1 hour

// Take best network from ES
val bestNetwork = esTrainer.getBestNetwork()

// Phase 2: TD fine-tuning
val tdAgent = new RawTd40(path, alpha = 0.003f, lambda = 0.8f,
                          initialNetwork = bestNetwork)
tdAgent.train(games = 200000)  // ~10 hours

// Result: Strong play in 11 hours instead of 42
```

**Why this works**:
- ES quickly finds "good enough" region of weight space (broad search)
- TD refines to optimal point within that region (local search)
- Leverages parallel compute early, sample efficiency late
- Best approach if you have time budget but want quality

---

## Sample Implementation Sketch

Here's a minimal working example (~150 lines):

```scala
package org.akoshterek.backgammon.evolution

import org.akoshterek.backgammon.agent.raw.RawTd40
import org.akoshterek.backgammon.agent.{RandomAgent, HeuristicAgent}
import scala.util.Random
import scala.collection.parallel.CollectionConverters._

class SimpleGA(populationSize: Int = 30,
               mutationRate: Double = 0.15,
               mutationMagnitude: Double = 0.08) {

  private val random = new Random()
  private var population: Array[RawTd40] = _

  def initialize(): Unit = {
    population = Array.fill(populationSize) {
      createRandomAgent()
    }
  }

  def evolve(generations: Int): RawTd40 = {
    initialize()

    for (gen <- 1 to generations) {
      // Evaluate fitness in parallel
      val fitness = population.par.map(agent =>
        evaluateFitness(agent, gamesPerEval = 100)
      ).toArray

      // Report progress
      val bestFitness = fitness.max
      val avgFitness = fitness.sum / fitness.length
      println(f"Gen $gen%3d: Best=$bestFitness%.3f Avg=$avgFitness%.3f")

      // Selection: keep top 30%
      val keepCount = (populationSize * 0.3).toInt
      val survivors = selectTop(population, fitness, keepCount)

      // Reproduction: create offspring
      val offspring = Array.fill(populationSize - keepCount) {
        val parent1 = survivors(random.nextInt(survivors.length))
        val parent2 = survivors(random.nextInt(survivors.length))
        val child = crossover(parent1, parent2)
        mutate(child)
        child
      }

      population = survivors ++ offspring
    }

    // Return best agent
    val finalFitness = population.par.map(evaluateFitness(_, 200)).toArray
    population(finalFitness.zipWithIndex.maxBy(_._1)._2)
  }

  private def evaluateFitness(agent: RawTd40, games: Int): Double = {
    var wins = 0
    val opponent = new RandomAgent(agent.path)

    for (_ <- 0 until games) {
      // Play one game
      // ... (game loop implementation)
      // wins += 1 if agent wins
    }

    wins.toDouble / games
  }

  private def selectTop(pop: Array[RawTd40], fitness: Array[Double],
                        count: Int): Array[RawTd40] = {
    fitness.zipWithIndex
      .sortBy(-_._1)
      .take(count)
      .map { case (_, idx) => pop(idx) }
  }

  private def crossover(parent1: RawTd40, parent2: RawTd40): RawTd40 = {
    val child = parent1.copyAgent()
    val weights1 = parent1.getWeights()  // Need to add this method
    val weights2 = parent2.getWeights()

    // Uniform crossover: 50/50 per weight
    val childWeights = weights1.zip(weights2).map { case (w1, w2) =>
      if (random.nextBoolean()) w1 else w2
    }

    child.setWeights(childWeights)  // Need to add this method
    child
  }

  private def mutate(agent: RawTd40): Unit = {
    val weights = agent.getWeights()

    for (i <- weights.indices) {
      if (random.nextDouble() < mutationRate) {
        weights(i) += random.nextGaussian().toFloat * mutationMagnitude.toFloat
      }
    }

    agent.setWeights(weights)
  }

  private def createRandomAgent(): RawTd40 = {
    val agent = new RawTd40(...)  // Standard initialization
    // Optionally: randomize initial weights
    agent
  }
}

// Usage:
val ga = new SimpleGA(populationSize = 30)
val bestAgent = ga.evolve(generations = 200)
bestAgent.save("best_evolved_agent.json")
```

**What's needed to make this work**:
1. Add `getWeights()` and `setWeights()` methods to TdNeuralNetwork
2. Implement game loop in `evaluateFitness`
3. Add parallel collection support (already in Scala)
4. Add progress tracking and checkpointing

---

## Conclusion

Evolutionary Strategies offer an interesting alternative to TD(λ) for backgammon:

**Best for**: Quick experimentation, parallel hardware utilization, simple implementation
**Not ideal for**: Sample efficiency, single-threaded training, long runs

**Recommended approach**:
- Stick with TD(λ) as primary method (proven, sample efficient, working)
- Experiment with ES for:
  - Quick pilots to test hyperparameters
  - Bootstrapping initial weights (ES 30 gens → TD fine-tune)
  - Research on exploration vs exploitation

**Implementation effort**: ~300 lines of Scala, ~2 days of work
**Expected results**: 80% vs Random in 2 hours (vs 20+ hours for TD)

Would be a fun side project to compare! But your current TD(λ) implementation with checkpointing is solid - don't feel pressured to switch.

---

## References

1. **Tesauro (1995)**: "Temporal Difference Learning and TD-Gammon"
   - Original TD-Gammon, proves TD works for backgammon

2. **Salimans et al. (2017)**: "Evolution Strategies as a Scalable Alternative to Reinforcement Learning"
   - OpenAI ES paper, shows ES competitive with RL on Atari

3. **Hansen (2006)**: "The CMA Evolution Strategy: A Tutorial"
   - Definitive CMA-ES reference

4. **Schaul et al. (2011)**: "High Dimensions and Heavy Tails for Natural Evolution Strategies"
   - Advanced ES techniques for neural networks

5. **Such et al. (2017)**: "Deep Neuroevolution: Genetic Algorithms Are a Competitive Alternative"
   - Shows simple GA can match modern RL methods

---

**Status**: Reference document - not currently implemented
**Estimated implementation time**: 2-3 days for basic GA, 1 week for full comparison study
**Recommendation**: Revisit after successful TD(λ) training run completes
