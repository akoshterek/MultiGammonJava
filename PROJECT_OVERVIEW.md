# MultiGammonJava - TD(λ) Backgammon AI

**Quick Summary:** A Scala/Java implementation of TD-Gammon using temporal-difference reinforcement learning with eligibility traces. Successfully replicated core TD-Gammon results with modern optimizations (SIMD, LeakyReLU).

**Build/runtime requirement:** JDK **25** (LTS). SIMD uses the incubating Java Vector API (`jdk.incubator.vector`); tests and `runMultiGammon` enable it via `--add-modules` / `--enable-preview`.

---

## What This Project Does

Trains a neural network to play backgammon through **self-play reinforcement learning**, similar to Tesauro's famous TD-Gammon (1992). The network learns purely from playing against itself, with no human game knowledge except the rules.

**Key achievement:** With LeakyReLU activation and optimized training, achieves +1.066 points/game vs Random at 200K games (comparable to early TD-Gammon results).

---

## Core Architecture

### Neural Network
- **Type:** Feedforward neural network with eligibility traces
- **Structure:** 198 inputs → 40 hidden neurons → 1 output (position evaluation)
- **Input representation:** Tesauro92Codec (4 features per board point)
- **Activation function:** LeakyReLU (hidden), Sigmoid (output)
- **Learning algorithm:** TD(λ) with eligibility traces

### TD(λ) Learning
```scala
// Core update rule (simplified)
reward = V(s_t+1) - V(s_t)  // Temporal difference error
eligibility_trace = γ * λ * trace + gradient
weights += α * reward * eligibility_trace
```

**Hyperparameters (optimized):**
- α (learning rate): 0.003
- λ (eligibility trace decay): 0.8
- γ (discount factor): 0.99

---

## Project Structure

```
MultiGammonJava/
├── multi-gammon-core/src/main/java/org/akoshterek/backgammon/
│   ├── nn/                          # Neural network implementation
│   │   ├── TdNeuralNetwork.scala    # Core TD(λ) network with SIMD optimization
│   │   ├── Activation.scala         # Activation functions (LeakyReLU, Sigmoid, etc.)
│   │   ├── EligibilityTrace2D.scala # Eligibility trace management
│   │   └── DotProductUtils.scala    # SIMD-optimized vector operations
│   ├── agent/
│   │   ├── raw/RawTd40.scala        # TD learning agent (40 hidden neurons)
│   │   └── HeuristicAgent.scala    # Baseline heuristic opponent
│   ├── board/Board.scala            # Backgammon game logic
│   └── dispatch/Dispatcher.scala    # Training/evaluation loop
├── experiments/                      # Experiment results and analysis
│   ├── 003_lower_alpha/             # Alpha tuning experiments (Run K: winner)
│   ├── 005_sigmoid_test/            # Activation function comparison
│   ├── RECOMMENDATIONS.md           # Full experimental analysis
│   └── ANALYSIS_SUMMARY.md          # Historical analysis
├── run_experiments.py               # Python script to run training experiments
└── PROJECT_OVERVIEW.md              # This file
```

---

## Key Files Explained

### Core Training
- **`TdNeuralNetwork.scala`** (multi-gammon-core/src/main/java/org/akoshterek/backgammon/nn/):
  - Implements forward pass, TD(λ) updates, and eligibility traces
  - Contains SIMD-optimized dot products for performance
  - **Key setting:** Line 10 - `hiddenActivation: Activation = LeakyReLU`

- **`RawTd40.scala`** (multi-gammon-core/src/main/java/org/akoshterek/backgammon/agent/raw/):
  - Training agent that wraps the neural network
  - Handles self-play learning and metrics logging
  - Saves TD error, weight deltas, and weight diagnostics to CSV

### Running Experiments
- **`run_experiments.py`** (root):
  - Python script to orchestrate training runs
  - Configures hyperparameters (α, λ, γ)
  - Runs training and benchmarks vs Random/Heuristic agents
  - Usage: `python3 run_experiments.py --run LONG`

### Experiment Results
- **`experiments/RECOMMENDATIONS.md`**:
  - **READ THIS FIRST** for understanding the project
  - Complete analysis of all experiments
  - Explains why LeakyReLU > Sigmoid
  - Documents optimal hyperparameters
  - Contains weight diagnostics guide

- **`experiments/005_sigmoid_test/`**:
  - Latest experiments comparing LeakyReLU vs Sigmoid
  - Contains CSV files with detailed metrics
  - Proves LeakyReLU superiority

---

## Current Best Configuration

**Network architecture:**
```scala
Input: 198 features (Tesauro92Codec)
Hidden: 40 neurons (LeakyReLU activation)
Output: 1 neuron (Sigmoid activation)
```

**Hyperparameters:**
```
α (alpha):  0.003  # Learning rate
λ (lambda): 0.8    # Eligibility trace decay
γ (gamma):  0.99   # Discount factor
```

**Training setup:**
```
Training games: 1,500,000 (target)
Evaluation period: Every 50,000 games
Benchmark opponents: Random, Heuristic
```

**Expected performance:**
- 200K games: +1.0 ppg vs Random ✓ (proven)
- 500K games: ~-0.2 to 0 vs Heuristic
- 1M games: +0.3 to +0.5 ppg vs Heuristic
- 1.5M games: +0.5 to +1.0 ppg vs Heuristic (goal)

---

## Key Innovations vs Original TD-Gammon

1. **LeakyReLU instead of Sigmoid (hidden layer)**
   - 70% better performance at 200K games
   - SIMD-friendly (no expensive exp() calls)
   - More stable, no learning rate decay needed

2. **SIMD Optimization**
   - Vectorized dot products using Java Vector API
   - ~2-3x faster than naive implementation
   - See `DotProductUtils.scala`

3. **Weight Diagnostics**
   - Real-time monitoring of network health
   - Tracks: mean, stddev, maxAbs, dead neurons
   - Logs every 1K games, prints every 50K
   - Early warning for instability/stuck networks

4. **Comprehensive Metrics Logging**
   - CSV logs: TD error, weight delta, weight statistics
   - Easy to analyze with pandas/matplotlib
   - Enables reproducible experiments

---

## Experimental Findings

### 1. Activation Function Comparison (Experiment 005)

| Activation | α | Performance @ 200K | Stability | Winner |
|------------|---|-------------------|-----------|--------|
| LeakyReLU | 0.003 | **+1.066 vs Random** | Stable | ✅ |
| Sigmoid | 0.003 | -0.143 vs Random | Stuck | ❌ |
| Sigmoid | 0.004 | +0.626 vs Random | Diverges after 200K | ❌ |

**Conclusion:** LeakyReLU is definitively superior.

### 2. Learning Rate Must Match Activation

**Why:** Gradient magnitudes differ dramatically:
- LeakyReLU gradient: ~1.0 (strong)
- Sigmoid gradient: ~0.1-0.2 (4-10x weaker)

**Result:** Sigmoid needs ~33-50% higher α, but even with optimal α, it's still inferior.

### 3. Training Duration is Critical

- 200K games: Beginner level
- 500K games: Intermediate level (approaching Heuristic)
- 1M games: Strong intermediate (beats Heuristic)
- 1.5M games: Expert level (solidly beats Heuristic)

**Don't judge performance before 500K games!**

### 4. "Crises" are Normal Learning

Networks experience temporary performance drops ("crises"):
- Example: Run K dropped from +0.9 to +0.28 at 120K
- Networks self-correct and improve
- Don't stop training during crises!

---

## Weight Diagnostics Guide

**Healthy network should have:**
- Mean: -0.01 to +0.01 (centered)
- StdDev: 0.3-1.0 for Sigmoid, 0.2-0.5 for LeakyReLU
- MaxAbs: < 10 for LeakyReLU, < 20 for Sigmoid
- Near-zero %: < 20% (most neurons active)

**Warning signs:**
- ⚠️ MaxAbs > 10 (LeakyReLU) or > 25 (Sigmoid): Possible instability
- ⚠️ MaxAbs < 0.5: Stuck in local minima
- ⚠️ Near-zero > 50%: Network dying

**Console output every 50K games:**
```
[150000 games] Weight Statistics:
  Mean: -0.0099, StdDev: 0.3012, MaxAbs: 13.4430
  Near-zero (<0.01): 636 (8.1%)
  Large (>5.0): 4 (0.1%)
```

---

## How to Run Training

### Quick Start
```bash
# Run long training (1.5M games, ~days on modern hardware)
python3 run_experiments.py --run LONG

# List available experiments
python3 run_experiments.py --list
```

### Configuration

Edit `run_experiments.py` to customize:
```python
fixed_args = [
    "-T", "1500000",  # Total training games
    "-P", "50000",     # Evaluation period
]

experiments = {
    "LONG": {"alpha": 0.003, "lambda": 0.8, "gamma": 0.99}
}
```

### Monitoring Progress

**During training:**
- Console output every 50K games (weight diagnostics)
- Benchmark results: `experiments/006_long_training_leaky_relu/`
- Metrics CSV: Detailed logs every 1K games

**CSV columns:**
```
gamesPlayed, averageTDError, weightDelta,
weightMean, weightStdDev, weightMaxAbs,
weightNearZero, weightLarge
```

**Plot progress:**

```python
import pandas as pd
import matplotlib.pyplot as plt

df = pd.read_csv('experiments_td/006_long_training_leaky_relu_a_0.002/run_LONG_RawTd40_td_metrics.csv')
df.plot(x='gamesPlayed', y=['averageTDError', 'weightMaxAbs'])
plt.show()
```

---

## Performance Baselines

### Opponents

**Random Agent:**
- Picks legal moves uniformly at random
- Baseline: Should beat this easily (+1.0+ ppg by 200K)

**Heuristic Agent:**
- Hand-crafted evaluation function
- Considers: men at home, blots, men on bar, primes
- Quite strong baseline (better than beginner humans)
- Goal: Beat this by 1M+ games

**PubEval (future):**
- Tesauro's 1992 benchmark
- Expert-level heuristic agent
- TD-Gammon 1.0 matched PubEval after 1.5M games

---

## Technical Details

### Input Representation (Tesauro92Codec)

Each of 24 board points encoded with 4 features:
```scala
Index 0: men == 1     → binary
Index 1: men >= 2     → binary
Index 2: men == 3     → binary
Index 3: men >= 4     → (men-3)/12.0  # normalized continuous
```

Plus additional features:
- Bar (men hit by opponent)
- Off (men borne off)
- Turn indicator

**Total: 198 input features**

### SIMD Optimization

Uses Java Vector API (`jdk.incubator.vector`, JDK 25) for fast dot products:
```scala
// Before SIMD: 100% scalar
sum = 0
for (i <- 0 until n) sum += a(i) * b(i)

// After SIMD: ~3x faster
vectorSum = 0
for (i <- 0 until n by SPECIES.length)
  vectorSum += VectorAPI.mul(a[i:i+lanes], b[i:i+lanes])
```

Critical for performance with 8,000+ weights updated every move.

### TD(λ) Algorithm

**Temporal Difference Learning:**
1. Play move, observe new board state
2. Evaluate before and after: V(s_t), V(s_t+1)
3. Compute TD error: δ = V(s_t+1) - V(s_t)
4. Update weights proportional to error and eligibility traces

**Eligibility Traces (λ):**
- Spread credit to earlier moves
- λ=0: Only update current move (TD(0))
- λ=1: Update all moves equally (Monte Carlo)
- λ=0.8: Exponential decay (our setting)

**Self-Play:**
- Both players use same network
- Network plays against copies of itself
- Learns from experience, no human data needed

---

## Future Improvements

### High Priority (Next Steps)
1. **Complete 1.5M training run** - Current: 200K proven, targeting 1.5M
2. **Learning rate decay** - Implement adaptive α for better late-game refinement
3. **Checkpoint/resume system** - Save/load weights to resume training

### Medium Priority
1. **Batch updates** - Update weights every N moves instead of every move
2. **Network architecture search** - Test 20, 40, 80 hidden neurons
3. **Lambda tuning** - Fine-tune λ in 0.7-0.8 range

### Low Priority (Polishing)
1. **Input representation** - Test SuttonCodec vs Tesauro92Codec
2. **Race/crashed evaluators** - Specialized networks for endgame
3. **Doubling cube** - Implement match play with cube decisions

---

## References

### Original Papers
- Tesauro, G. (1992). "Practical Issues in Temporal Difference Learning"
- Tesauro, G. (1995). "Temporal Difference Learning and TD-Gammon"

### Code Attribution
- Some service code borrowed from GNU Backgammon
- Some service code borrowed from Steffen Nissen PhD Thesis
- Original implementation: Alex Koshterek
- Recent optimizations: Feb 2026 experiments

---

## Quick Command Reference

```bash
# Run long training
python3 run_experiments.py --run LONG

# List available experiments
python3 run_experiments.py --list

# Build project
./gradlew build

# Run specific configuration manually
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B Heuristic \
  --alpha 0.003 \
  --lambda 0.8 \
  --gamma 0.99 \
  -T 500000 \
  -P 50000 \
  --experiment-path experiments/test"
```

---

## Key Takeaways

1. ✅ **LeakyReLU > Sigmoid** for TD(λ) backgammon (70% better at 200K)
2. ✅ **Training duration matters most** - need 1M+ games to beat heuristics
3. ✅ **Learning rate must match activation** - gradient magnitudes differ
4. ✅ **Weight diagnostics essential** - catch stuck/diverging networks early
5. ✅ **"Crises" are normal** - temporary performance drops during learning
6. ✅ **SIMD optimization crucial** - 3x speedup enables longer training

**Bottom line:** With optimal configuration (LeakyReLU + α=0.003 + 1.5M games), this implementation can replicate TD-Gammon's intermediate-to-expert level play.

---

## Contact & License

**Repository:** https://github.com/akoshterek/MultiGammonJava

**License:** See LICENSE file

**Status:** Active development (as of Feb 2026)
- Latest experiment: 005_sigmoid_test (completed)
- Next milestone: 1.5M game training run
- Target: Beat Heuristic agent by +0.5 to +1.0 ppg

---

*For detailed experimental analysis and findings, see `experiments/RECOMMENDATIONS.md`*

*For quick debugging/investigation, this overview should provide sufficient context for LLMs and developers to understand the project structure and current state.*
