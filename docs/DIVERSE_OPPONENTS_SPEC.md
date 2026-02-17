# Diverse Opponents Training - Implementation Specification

## Overview

Implement training with multiple opponent types to prevent self-play collapse. Training will alternate between self-play and fixed opponents based on configurable probabilities.

## Problem Statement

Pure self-play training leads to:
- Networks optimizing for playing against themselves, not general backgammon
- Performance plateau around -0.46 PPG vs SimpleHeuristic despite strong self-play results
- Inability to learn basic strategic principles encoded in simple heuristics

## Solution

Mix training opponents with configurable probabilities:
- **Self-play**: Fast learning, develops complex patterns
- **Fixed opponents** (Random, SimpleHeuristic): Grounds learning in basic strategy, prevents overfitting

---

## Command Line Parameters

### New Parameters

```bash
# Training opponents (what to train against)
--training-opponents "self:50,SimpleHeuristic:25,Random:25"

# Benchmark opponents (what to evaluate against, every 50K games)
--benchmark-opponents "Random,SimpleHeuristic,Heuristic"
```

### Parameter Format

**Training Opponents:**
- Format: `"opponent1:percentage,opponent2:percentage,..."`
- `self` keyword means self-play
- Percentages must sum to 100
- Examples:
  - `"self:50,SimpleHeuristic:25,Random:25"` - 50% self-play, 25% each other
  - `"self:70,SimpleHeuristic:20,Random:10"` - 70% self-play focus
  - `"self:100"` - Pure self-play (current behavior)
  - `"SimpleHeuristic:50,Random:50"` - No self-play (supervised learning)

**Benchmark Opponents:**
- Format: `"opponent1,opponent2,opponent3,..."`
- Comma-separated list of opponent names
- Each opponent tested with 1000 games per checkpoint
- Examples:
  - `"Random,SimpleHeuristic,Heuristic"` - Full benchmark suite
  - `"SimpleHeuristic"` - Quick benchmark
  - `"Random,SimpleHeuristic,Heuristic,PubEval"` - Extended benchmark

### Default Values

```scala
val defaultTrainingOpponents = "self:100"  // Pure self-play (backward compatible)
val defaultBenchmarkOpponents = "SimpleHeuristic"  // Quick benchmark
```

---

## Implementation Details

### 1. OpponentConfig Data Structures

```scala
// In RawTd40.scala or new OpponentConfig.scala file

case class TrainingOpponentSpec(
  name: String,        // "self", "Random", "SimpleHeuristic", etc.
  percentage: Int      // 0-100
) {
  require(percentage >= 0 && percentage <= 100, "Percentage must be 0-100")
}

case class OpponentConfig(
  trainingOpponents: List[TrainingOpponentSpec],
  benchmarkOpponents: List[String]
) {
  require(trainingOpponents.map(_.percentage).sum == 100,
    "Training opponent percentages must sum to 100")

  // Convert percentages to cumulative probabilities for selection
  lazy val cumulativeProbs: List[(String, Float)] = {
    var cumulative = 0.0f
    trainingOpponents.map { spec =>
      cumulative += spec.percentage / 100.0f
      (spec.name, cumulative)
    }
  }
}

object OpponentConfig {
  def parse(trainingStr: String, benchmarkStr: String): OpponentConfig = {
    // Parse "self:50,SimpleHeuristic:25,Random:25"
    val trainingSpecs = trainingStr.split(",").map { spec =>
      val parts = spec.trim.split(":")
      require(parts.length == 2, s"Invalid training opponent spec: $spec")
      TrainingOpponentSpec(parts(0).trim, parts(1).trim.toInt)
    }.toList

    // Parse "Random,SimpleHeuristic,Heuristic"
    val benchmarkNames = benchmarkStr.split(",").map(_.trim).toList

    OpponentConfig(trainingSpecs, benchmarkNames)
  }
}
```

### 2. Command Line Options (OptionsBean.java)

```java
public class OptionsBean {
    // Existing fields...

    @Option(names = {"--training-opponents"},
            description = "Training opponent mix (e.g., 'self:50,SimpleHeuristic:25,Random:25')")
    private String trainingOpponents = "self:100";

    @Option(names = {"--benchmark-opponents"},
            description = "Comma-separated benchmark opponents (e.g., 'Random,SimpleHeuristic,Heuristic')")
    private String benchmarkOpponents = "SimpleHeuristic";

    public String getTrainingOpponents() { return trainingOpponents; }
    public String getBenchmarkOpponents() { return benchmarkOpponents; }
}
```

### 3. RawTd40 Constructor Changes

```scala
class RawTd40(
  override val path: Path,
  var alpha: Float = 0.01f,
  val lambda: Float = 0.7f,
  val gamma: Float = 1.0f,
  val experimentTag: String = "",
  val isCopy: Boolean = false,
  val originalSeed: Long = 16000000L,
  val alphaAnnealingEnabled: Boolean = true,
  val alphaAnnealingTarget: Float = 0.0002f,
  val alphaAnnealingGames: Int = 1500000,
  val biasAlphaRatio: Float = 1.0f,
  val gradientClipThreshold: Float = 5.0f,
  val useOutputBias: Boolean = true,
  val opponentConfig: OpponentConfig = OpponentConfig(
    List(TrainingOpponentSpec("self", 100)),
    List("SimpleHeuristic")
  )
) extends AbsAgent("RawTd40", path) with RawAgent {

  // Opponent selection using random number generator
  private val random = new Random(originalSeed + 1)  // Different seed from dice

  // Cache created opponents to avoid recreating every time
  // Self-play copy is cached too - both main agent and copy learn during games
  private val opponentCache = scala.collection.mutable.Map[String, Agent]()

  // Create opponent agent by name
  private def createOpponent(name: String): Agent = {
    opponentCache.getOrElseUpdate(name, {
      name match {
        case "self" =>
          val copy = this.copyAgent()
          copy.setLearnMode(true)  // Copy learns alongside main agent
          copy
        case "Random" => new RandomAgent(path)
        case "SimpleHeuristic" => new SimpleHeuristicAgent(path)
        case "Heuristic" => new HeuristicAgent(path)
        case _ => throw new IllegalArgumentException(s"Unknown opponent: $name")
      }
    })
  }

  // Select opponent based on configured probabilities
  private def selectTrainingOpponent(): Agent = {
    val roll = random.nextFloat()

    opponentConfig.cumulativeProbs.find { case (_, cumProb) =>
      roll < cumProb
    } match {
      case Some((name, _)) => createOpponent(name)
      case None => this.copyAgent()  // Fallback to self-play
    }
  }
}
```

### 4. Training Loop Integration

```scala
// In GameDispatcher or training loop
// Replace: opponent = agent.copyAgent()
// With:

private def playTrainingGames(agent: RawTd40, numGames: Int): Unit = {
  var gamesPlayed = 0

  while (gamesPlayed < numGames) {
    // Select opponent based on probabilities (cached after first creation)
    val opponent = agent.selectTrainingOpponent()

    // Play one game
    val game = new Game()
    game.play(agent, opponent)

    gamesPlayed += 1

    // Note: Opponent cleanup not needed - instances are cached and reused
    // For self-play, the cached copy learns alongside the main agent
  }
}
```

**Key Implementation Note:**

All opponents (including self-play copy) are cached after first creation:
- **Self-play**: The cached copy has `learnMode=true` and shares the same learning experience as the main agent. Both update their weights from each game they play.
- **Fixed opponents** (Random, SimpleHeuristic, Heuristic): Never learn, provide consistent baseline for training.
- **Performance**: Creating agents is expensive; caching reduces overhead to O(1) per game.
```

### 5. Benchmark File Structure

**Keep existing files unchanged:**
- `run_LONG_RawTd40_td_metrics.csv` - TD metrics every 1K games (current behavior)

**Create new benchmark file:**
- `run_LONG_RawTd40_benchmarks.csv` - Benchmark results every 50K games

**Benchmark file format:**
```
gamesPlayed,opponent,ppg,gamesWon,gamesLost,totalPoints
50000,Random,0.859,8094,1906,14411
50000,SimpleHeuristic,-0.543,2240,7760,2450
50000,Heuristic,-1.482,1334,8666,1439
100000,Random,0.861,8102,1898,14425
100000,SimpleHeuristic,-0.779,1890,8110,1920
100000,Heuristic,-1.521,1280,8720,1398
...
```

**Implementation:**

```scala
// In RawTd40.scala endGame() method

override def endGame(): Unit = {
  super.endGame()

  if (isLearnMode && !isCopy && playedGames % 50000 == 0) {
    saveCheckpoint(playedGames)

    // Run benchmarks against configured opponents
    runAndSaveBenchmarks(playedGames)
  }
}

private def runAndSaveBenchmarks(games: Int): Unit = {
  val benchmarkFile = new File(experimentPath.toFile,
    s"${experimentTag}_${name}_benchmarks.csv")

  val writer = new FileWriter(benchmarkFile, true)  // Append mode

  try {
    // If first write, add header
    if (games == 50000) {
      writer.write("gamesPlayed,opponent,ppg,gamesWon,gamesLost,totalPoints\n")
    }

    // Run benchmark against each configured opponent
    opponentConfig.benchmarkOpponents.foreach { opponentName =>
      val opponent = createOpponent(opponentName)
      val results = evaluateAgainst(opponent, games = 1000)

      // Write result row
      writer.write(s"$games,$opponentName,${results.ppg},${results.gamesWon},${results.gamesLost},${results.totalPoints}\n")
    }
  } finally {
    writer.close()
  }
}

case class BenchmarkResult(
  ppg: Float,
  gamesWon: Int,
  gamesLost: Int,
  totalPoints: Int
)

private def evaluateAgainst(opponent: Agent, games: Int): BenchmarkResult = {
  // Create evaluation copy (no learning)
  val evalCopy = this.copyAgent()
  evalCopy.setLearnMode(false)

  // Play games and collect statistics
  val dispatcher = new GameDispatcher(evalCopy, opponent)
  dispatcher.playGames(games)

  BenchmarkResult(
    ppg = dispatcher.getAveragePointsPerGame(),
    gamesWon = dispatcher.getWinsForPlayer(0),  // Player O (our agent)
    gamesLost = dispatcher.getWinsForPlayer(1),  // Player X (opponent)
    totalPoints = dispatcher.getTotalPointsForPlayer(0)
  )
}
```

---

## AgentFactory Integration

```scala
// In AgentFactory.scala

object AgentFactory {
  def createAgent(fullName: String, options: OptionsBean): Agent = {
    val fullNameLower: String = fullName.toLowerCase
    val tokens = fullNameLower.split("-")

    tokens(0) match {
      case "rawtd40" =>
        val opponentConfig = OpponentConfig.parse(
          options.getTrainingOpponents,
          options.getBenchmarkOpponents
        )

        new RawTd40(
          Evaluator.basePath,
          options.alpha,
          options.lambda,
          options.gamma,
          options.experimentRunTag,
          isCopy = false,
          originalSeed = 16000000L,
          opponentConfig = opponentConfig
        )
      // ... other cases
    }
  }
}
```

---

## File Structure

After running experiment with diverse opponents, you'll have:

```
experiments/007_diverse_opponents/
├── checkpoints/
│   ├── run_diverse_00050000.json
│   ├── run_diverse_00100000.json
│   └── ...
├── run_diverse_RawTd40_td_metrics.csv        # TD metrics every 1K games
└── run_diverse_RawTd40_benchmarks.csv        # Benchmark results every 50K games
```

**TD Metrics (unchanged):**
```csv
gamesPlayed,averageTDError,weightDelta,weightMean,weightStdDev,weightMaxAbs,weightNearZero,weightLarge
1000,0.040,0.367,0.000045,0.102,0.381,671,0
2000,0.036,0.241,-0.000007,0.102,0.380,673,0
...
```

**Benchmarks (new):**
```csv
gamesPlayed,opponent,ppg,gamesWon,gamesLost,totalPoints
50000,Random,0.859,8094,1906,14411
50000,SimpleHeuristic,-0.543,2240,7760,2450
50000,Heuristic,-1.482,1334,8666,1439
100000,Random,0.861,8102,1898,14425
100000,SimpleHeuristic,-0.779,1890,8110,1920
100000,Heuristic,-1.521,1280,8720,1398
...
```

---

## Usage Examples

### Example 1: Recommended Mix (50% self-play)

```bash
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B SimpleHeuristic \
  -G 1000 \
  -T 1500000 \
  -P 50000 \
  --alpha 0.001 \
  --lambda 0.8 \
  --gamma 0.99 \
  --experiment-path experiments/007_diverse_opponents \
  --experiment-tag diverse_50 \
  --training-opponents 'self:50,SimpleHeuristic:25,Random:25' \
  --benchmark-opponents 'Random,SimpleHeuristic,Heuristic'"
```

### Example 2: Heavy Self-Play (70%)

```bash
--training-opponents 'self:70,SimpleHeuristic:20,Random:10'
```

### Example 3: No Self-Play (Supervised)

```bash
--training-opponents 'SimpleHeuristic:60,Random:40'
```

### Example 4: Extended Benchmarks

```bash
--benchmark-opponents 'Random,SimpleHeuristic,Heuristic,PubEval'
```

---

## Expected Behavior Changes

### Training Progress

**Before (pure self-play):**
- Fast early learning (MaxAbs growth)
- Peak performance around 250-450K games
- Collapse after 450K games
- Final: -0.994 PPG vs SimpleHeuristic at 450K

**After (50% self-play + 25% SimpleHeuristic + 25% Random):**
- Slower but more stable learning
- Continuous improvement without collapse
- Expected: +0.5 to +1.0 PPG vs SimpleHeuristic at 1.5M games
- Learns both tactical complexity (self-play) and strategic basics (heuristics)

### Benchmark Output Example

**Benchmarks file (run_diverse_RawTd40_benchmarks.csv):**
```csv
gamesPlayed,opponent,ppg,gamesWon,gamesLost,totalPoints
50000,Random,0.85,8094,1906,14411
50000,SimpleHeuristic,-0.45,2240,7760,2450
50000,Heuristic,-1.82,1334,8666,1439
100000,Random,1.12,8250,1750,15200
100000,SimpleHeuristic,-0.28,3890,6110,6920
100000,Heuristic,-1.65,1520,8480,2150
150000,Random,1.35,8450,1550,16100
150000,SimpleHeuristic,0.05,4950,5050,10050
150000,Heuristic,-1.42,1780,8220,2840
```

This shows clear improvement trajectory against all opponents, without the collapse seen in pure self-play.

---

## Testing Plan

### Phase 1: Basic Functionality
1. Test opponent selection probabilities (run 10K selections, verify distribution)
2. Test opponent caching (verify agents aren't recreated)
3. Test benchmark execution (verify correct opponents tested)

### Phase 2: Training Runs
1. **Baseline**: Pure self-play (existing behavior)
2. **Test 1**: 50/25/25 mix (recommended)
3. **Test 2**: 70/20/10 mix (self-play focused)
4. **Test 3**: 100% SimpleHeuristic (supervised baseline)

### Phase 3: Analysis
Compare benchmark files at 500K and 1M games:
- **Benchmark trends**: Parse `*_benchmarks.csv` for PPG progression
- **Training stability**: Check TD metrics for weight stability
- **No collapse**: Verify continued improvement, not decline

**Analysis script example:**
```python
import pandas as pd

# Load benchmark results
benchmarks = pd.read_csv('run_diverse_RawTd40_benchmarks.csv')

# Plot progression for each opponent
for opponent in ['Random', 'SimpleHeuristic', 'Heuristic']:
    data = benchmarks[benchmarks['opponent'] == opponent]
    plt.plot(data['gamesPlayed'], data['ppg'], label=opponent)

plt.xlabel('Games Played')
plt.ylabel('PPG')
plt.legend()
plt.title('Training Progress vs Multiple Opponents')
```

---

## Backward Compatibility

**Default parameters maintain existing behavior:**
```scala
--training-opponents "self:100"      // Pure self-play
--benchmark-opponents "SimpleHeuristic"  // Existing benchmark
```

**File structure:**
- `*_td_metrics.csv` - **Format unchanged**, continues to work with existing analysis scripts
- `*_benchmarks.csv` - **New file**, doesn't affect existing workflow

**No code changes needed for:**
- Checkpoint format (unchanged)
- Weight serialization (unchanged)
- Network architecture (unchanged)
- TD metrics format (unchanged)
- Existing plotting/analysis scripts (still work)

---

## Performance Considerations

### Memory
- **Opponent cache**: Typically 2-4 agents in memory (e.g., self, Random, SimpleHeuristic)
- **Self-play copy**: Same size as main agent (~200KB for 196→40→1 network)
- **Total overhead**: ~1-2MB for cached opponents
- **Negligible impact**: Modern systems handle this easily

### Speed
- **Selection overhead**: O(1) random selection + O(1) cache lookup per game (~negligible)
- **Opponent caching**: Agents created once, reused for all games
  - **Without caching**: 1.5M games × 100ms/creation = 150K seconds = 42 hours wasted!
  - **With caching**: 4 agents × 100ms = 400ms total overhead
- **Benchmark overhead**: 1000 games × 3 opponents = 3K games per checkpoint
  - At 50K checkpoint interval = 6% overhead
  - Acceptable for better insights

### Recommended Settings
- Checkpoint every 50K games (current)
- 1000 games per benchmark opponent
- 3-4 benchmark opponents max

### Data Analysis Workflow

**Separate files enable clean analysis:**

```python
# Training metrics (high frequency, 1K interval)
td_metrics = pd.read_csv('run_diverse_RawTd40_td_metrics.csv')
plt.plot(td_metrics['gamesPlayed'], td_metrics['weightMaxAbs'])
plt.title('Feature Development (Every 1K games)')

# Benchmark results (low frequency, 50K interval)
benchmarks = pd.read_csv('run_diverse_RawTd40_benchmarks.csv')
pivot = benchmarks.pivot(index='gamesPlayed', columns='opponent', values='ppg')
pivot.plot()
plt.title('Performance vs Multiple Opponents (Every 50K games)')
```

**No need to filter mixed data or align different frequencies!**

---

## Future Enhancements

### Self-Play Copy Behavior

**How it works:**
1. First time "self" is selected → create copy with `copyAgent()`, cache it
2. Main agent and cached copy both have `learnMode=true`
3. When they play against each other, both learn from the game
4. Over time, the cached copy tracks the main agent's learning (since both share the same NN)

**Why this works:**
- Both agents see the same training signal (game outcomes)
- Both update their weights using TD(λ)
- The copy naturally stays synchronized with the main agent's skill level
- No explicit weight copying needed

**Alternative considered (rejected):**
- Recreating copy each game → too expensive (100ms × 1.5M games = 42 hours)
- Periodic weight sync → adds complexity, not necessary

### Possible Extensions
1. **Dynamic mixing**: Adjust percentages during training
   - Start 70% self-play → end 30% self-play
2. **Curriculum learning**: Introduce harder opponents over time
   - Early: 100% Random
   - Mid: 50% SimpleHeuristic, 50% Random
   - Late: 50% self-play, 25% SimpleHeuristic, 25% Heuristic
3. **Population-based training**: Maintain multiple networks, train against pool
4. **ELO tracking**: Track relative strength over time
5. **Multiple self-play copies**: Cache N copies at different skill levels (e.g., checkpoints from 100K, 200K, 300K ago)

---

## Open Questions

1. **Should benchmark opponents be subset of training opponents?**
   - **Answer**: No restriction. Benchmark can include stronger opponents not used in training.

2. **What if training opponent doesn't exist?**
   - **Answer**: Fail fast at startup with clear error message.

3. **Should self-play copy be regenerated each game?**
   - **Answer**: No, cache it like other opponents. Both the main agent and cached copy learn during games, so the copy naturally tracks the main agent's learning progress.

4. **How to handle opponent randomness (e.g., Random agent)?**
   - **Answer**: Each opponent instance uses its own random seed (derived from originalSeed + hash of opponent name).

---

## Summary

This specification provides a complete design for diverse opponent training to address the self-play collapse problem. The design is:
- **Backward compatible**: Default parameters preserve existing behavior
- **Flexible**: Configurable mixing ratios and benchmark suite
- **Observable**: Separate benchmark file tracks performance against multiple opponents
- **Clean separation**: TD metrics (every 1K) vs benchmarks (every 50K) in separate files
- **Performant**: Minimal overhead (~6% for benchmarks)
- **Extensible**: Foundation for curriculum learning and population-based training

### File Organization Benefits

**Separate files provide:**
1. **Different frequencies**: TD metrics every 1K, benchmarks every 50K
2. **Easy analysis**: Plot benchmark trends without filtering mixed data
3. **Flexible benchmarking**: Change benchmark opponents without affecting TD metrics format
4. **Backward compatibility**: Existing TD metrics file format unchanged
5. **Clear semantics**: Training metrics vs evaluation metrics
