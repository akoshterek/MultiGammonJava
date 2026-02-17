# Diverse Opponents Training - Usage Guide

## Overview

The diverse opponents training system prevents self-play collapse by mixing training opponents. This addresses the observed performance degradation after 450K games in pure self-play training.

## New Command Line Options

### `--training-opponents`
Controls the mix of opponents during training.

**Format:** `"opponent1:percentage,opponent2:percentage,..."`
- Percentages must sum to 100
- `self` keyword means self-play (copies share the same neural network)

**Examples:**
```bash
# Pure self-play (default, backward compatible)
--training-opponents "self:100"

# Recommended mix: 50% self-play, 25% SimpleHeuristic, 25% Random
--training-opponents "self:50,SimpleHeuristic:25,Random:25"

# Heavy self-play with grounding
--training-opponents "self:70,SimpleHeuristic:20,Random:10"

# Supervised learning (no self-play)
--training-opponents "SimpleHeuristic:60,Random:40"
```

**Available opponents:**
- `self` - Self-play copy (shares neural network, both learn together)
- `Random` - Random move selection
- `SimpleHeuristic` - Basic positional evaluation
- `Heuristic` - Full heuristic agent (strong baseline)

### `--benchmark-opponents`
Controls which opponents to benchmark against every 50K games.

**Format:** `"opponent1,opponent2,opponent3,..."`
- Comma-separated list

**Examples:**
```bash
# Single opponent (default)
--benchmark-opponents "SimpleHeuristic"

# Full benchmark suite
--benchmark-opponents "Random,SimpleHeuristic,Heuristic"

# Extended benchmark
--benchmark-opponents "Random,SimpleHeuristic,Heuristic,PubEval"
```

## Usage Examples

### Example 1: Recommended Configuration
Prevents self-play collapse while maintaining learning speed:

```bash
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B SimpleHeuristic \
  -T 1500000 \
  -G 1000 \
  -P 50000 \
  --alpha 0.003 \
  --lambda 0.8 \
  --gamma 0.99 \
  --experiment-path experiments/007_diverse_opponents \
  --experiment-tag diverse_50_25_25 \
  --training-opponents 'self:50,SimpleHeuristic:25,Random:25' \
  --benchmark-opponents 'Random,SimpleHeuristic,Heuristic'"
```

### Example 2: Pure Self-Play (Baseline)
For comparison with historical runs:

```bash
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B SimpleHeuristic \
  -T 1500000 \
  -G 1000 \
  -P 50000 \
  --alpha 0.003 \
  --lambda 0.8 \
  --gamma 0.99 \
  --experiment-path experiments/007_baseline_selfplay \
  --experiment-tag baseline_selfplay"
```

Note: Omitting `--training-opponents` defaults to `"self:100"` (pure self-play).

### Example 3: Supervised Learning Experiment
Train against fixed opponents only:

```bash
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B SimpleHeuristic \
  -T 500000 \
  -G 1000 \
  -P 50000 \
  --alpha 0.003 \
  --lambda 0.8 \
  --gamma 0.99 \
  --experiment-path experiments/007_supervised \
  --experiment-tag supervised \
  --training-opponents 'SimpleHeuristic:50,Heuristic:30,Random:20' \
  --benchmark-opponents 'Random,SimpleHeuristic,Heuristic'"
```

## Output Files

Training with diverse opponents produces two CSV files:

### 1. TD Metrics (unchanged)
**File:** `{experiment_tag}_RawTd40_td_metrics.csv`
**Frequency:** Every 1,000 games
**Columns:**
- `gamesPlayed` - Total games trained
- `averageTDError` - Average TD error
- `weightDelta` - L2 norm of weight changes
- `weightMean`, `weightStdDev`, `weightMaxAbs` - Weight statistics
- `weightNearZero`, `weightLarge` - Weight distribution metrics

### 2. Benchmarks (NEW)
**File:** `{experiment_tag}_RawTd40_benchmarks.csv`
**Frequency:** Every 50,000 games
**Columns:**
- `gamesPlayed` - Checkpoint (50K, 100K, 150K, ...)
- `opponent` - Opponent name (Random, SimpleHeuristic, Heuristic)
- `ppg` - Points per game (positive = winning)
- `gamesWon` - Games won by training agent
- `gamesLost` - Games won by opponent
- `totalPoints` - Total points scored by training agent

**Example:**
```csv
gamesPlayed,opponent,ppg,gamesWon,gamesLost,totalPoints
50000,Random,0.859,8094,1906,14411
50000,SimpleHeuristic,-0.543,2240,7760,2450
50000,Heuristic,-1.482,1334,8666,1439
100000,Random,1.112,8250,1750,15200
100000,SimpleHeuristic,-0.128,4890,5110,8720
100000,Heuristic,-1.265,1520,8480,2150
```

## Expected Outcomes

### Pure Self-Play (self:100)
- **0-250K games:** Fast improvement
- **250K-450K games:** Peak performance
- **450K+ games:** **Performance decline** (self-play collapse)
- **Final @ 1.5M:** Suboptimal generalization

### Diverse Opponents (self:50,SimpleHeuristic:25,Random:25)
- **0-250K games:** Slower but more stable improvement
- **250K-450K games:** Continued steady improvement
- **450K+ games:** **No collapse** - continuous learning
- **Final @ 1.5M:** Strong generalization, +0.5 to +1.0 PPG vs SimpleHeuristic

## Architecture Notes

### OpponentSelector Component
The opponent management is handled by the `OpponentSelector` class, which:
- Works with any `CopyableAgent` (not specific to RawTd40)
- Caches opponent instances (created once, reused)
- Selects opponents probabilistically each game
- Provides list of benchmark opponents

### Self-Play Copy Behavior
**Critical detail:** When using `self` opponent, the copy shares the same neural network:
```scala
// In RawTd40.copyAgent():
other.tdNN = tdNN  // Direct reference, not a copy!
```

**Implications:**
- Both main agent and self-play copy update the same weights
- No weight synchronization needed
- Copy naturally tracks main agent's learning progress
- Efficient: no redundant network instances

### Backward Compatibility
The system is fully backward compatible:
- Default parameters maintain pure self-play behavior
- Existing training scripts work unchanged
- New parameters are optional

## Troubleshooting

### "Training opponent percentages must sum to 100"
Check that all percentages in `--training-opponents` add up to exactly 100.

### "Unknown opponent: XYZ"
Valid opponent names are: `self`, `Random`, `SimpleHeuristic`, `Heuristic`

### No benchmarks CSV created
Ensure:
1. `--experiment-tag` is set (required for benchmarks)
2. Training runs for at least 50,000 games (benchmark period)
3. Agent is RawTd40 (other agents don't support benchmarks)

## Performance Considerations

### Memory Overhead
- 2-4 cached opponent agents (~200KB each)
- Total overhead: ~1-2MB (negligible)

### Speed Impact
- Opponent selection: O(1) per game (~negligible)
- Opponent caching eliminates creation overhead
- Benchmark overhead: ~6% (3K games per 50K checkpoint)

### Recommended Settings
- Checkpoint interval: 50K games
- Benchmark games per opponent: 1000
- Maximum benchmark opponents: 3-4

## Analysis Workflow

### Python Analysis Example
```python
import pandas as pd
import matplotlib.pyplot as plt

# Load benchmark results
benchmarks = pd.read_csv('experiments/007_diverse/diverse_50_25_25_RawTd40_benchmarks.csv')

# Plot progression for each opponent
for opponent in benchmarks['opponent'].unique():
    data = benchmarks[benchmarks['opponent'] == opponent]
    plt.plot(data['gamesPlayed'], data['ppg'], label=opponent, marker='o')

plt.xlabel('Games Played')
plt.ylabel('Points Per Game (PPG)')
plt.title('Training Progress: Diverse Opponents')
plt.legend()
plt.grid(True)
plt.axhline(y=0, color='k', linestyle='--', alpha=0.3)
plt.show()

# Compare with pure self-play
selfplay = pd.read_csv('experiments/007_baseline/baseline_selfplay_RawTd40_benchmarks.csv')
# ... comparison plots
```

## References

- See `docs/DIVERSE_OPPONENTS_SPEC.md` for full technical specification
- See `experiments/RECOMMENDATIONS.md` for experimental findings
- See `PROJECT_OVERVIEW.md` for overall project context
