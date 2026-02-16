# Checkpoint/Resume System Specification

**Status:** Design complete, ready for implementation
**Estimated effort:** 4-5 hours
**Priority:** High (enables long training runs)

---

## Overview

Implement checkpoint/resume system to enable:
- Long training runs (1.5M+ games) without starting over
- Recovery from crashes/interruptions
- Alpha tuning experiments (resume with different learning rate)
- Training continuation across multiple sessions

---

## Core Design Decisions

### File Format: JSON
- Human-readable for inspection
- Easy to validate/debug
- Git-friendly for version control
- ~5MB per checkpoint (acceptable)

### Resume Behavior: Auto-detect (Default)
- Always check for existing checkpoints on startup
- Resume automatically if found
- No explicit `--resume` flag needed
- To start fresh: manually delete checkpoints

### Error Handling: Fail Fast
- Crash on corrupted checkpoint (don't try to recover)
- Crash on architecture mismatch (don't ignore)
- Crash on missing required fields
- Clear error messages, no graceful degradation

---

## File Naming & Location

### Checkpoint Files
```
experiments/{experiment_path}/checkpoints/{experimentTag}_checkpoint_{gamesPlayed}.json
```

**Example:**
```
experiments/006_long_training_leaky_relu/
├── checkpoints/
│   ├── run_LONG_checkpoint_00050000.json
│   ├── run_LONG_checkpoint_00100000.json
│   ├── run_LONG_checkpoint_00150000.json
│   └── ...
├── run_LONG_RawTd40_td_metrics.csv
├── run_LONG_RawTd40 vs Random.csv
└── run_LONG_RawTd40 vs Heuristic.csv
```

**Naming components:**
- `{experimentTag}`: From `--experiment-tag` parameter (e.g., "run_LONG")
- `{gamesPlayed}`: Checkpoint point (e.g., 50000, 100000, ...)
- Format: Zero-padded 8 digits for sorting

### Discovery Logic
```python
def find_latest_checkpoint(experiment_path, experiment_tag):
    checkpoint_dir = f"{experiment_path}/checkpoints"
    pattern = f"{experiment_tag}_checkpoint_*.json"

    # Find all matching files
    checkpoints = glob(f"{checkpoint_dir}/{pattern}")

    if not checkpoints:
        return None

    # Extract game counts and return highest
    game_counts = [extract_game_count(f) for f in checkpoints]
    latest = max(game_counts)
    return f"{checkpoint_dir}/{experiment_tag}_checkpoint_{latest:06d}.json"
```

---

## Checkpoint Format

### Complete JSON Structure

```json
{
  "formatVersion": "1.0",
  "timestamp": "2026-02-13T14:30:00Z",
  "gamesPlayed": 200000,
  "experimentTag": "run_LONG",

  "hyperparameters": {
    "alpha": 0.003,
    "lambda": 0.8,
    "gamma": 0.99
  },

  "networkArchitecture": {
    "inputSize": 198,
    "hiddenSize": 40,
    "outputSize": 1,
    "hiddenActivation": "LeakyReLU",
    "outputActivation": "Sigmoid"
  },

  "randomSeed": 16000000,

  "performance": {
    "vsRandom": 1.066,
    "vsHeuristic": -0.845
  },

  "weights": {
    "wInputHidden": [[...], ...],
    "wHiddenOutput": [[...]],
    "bHidden": [...],
    "bOutput": [...]
  }
}
```

### Field Descriptions

**Metadata:**
- `formatVersion`: "1.0" (for future compatibility)
- `timestamp`: ISO 8601 format
- `gamesPlayed`: Total games trained (checkpoint point)
- `experimentTag`: Matches file name prefix

**Hyperparameters:**
- `alpha`: Learning rate (can be overridden on resume)
- `lambda`: Eligibility trace decay (must match on resume)
- `gamma`: Discount factor (must match on resume)

**Network Architecture:**
- `inputSize`, `hiddenSize`, `outputSize`: Layer sizes
- `hiddenActivation`: "LeakyReLU", "Sigmoid", etc.
- `outputActivation`: "Sigmoid" (always)
- Must match code exactly on resume

**Other:**
- `randomSeed`: Original seed (reference only, not used on resume)
- `performance`: Optional, latest benchmark results
- `weights`: Full network state (all arrays)

---

## Save Behavior

### When to Save
- **Every 50,000 games** (matches evaluation period)
- At end of training (if not exactly on 50K boundary)
- Never: Rolling recovery checkpoints (50K granularity sufficient)

### Save Logic
```scala
// In RawTd40.endGame()
if (isLearnMode && !isCopy && playedGames % 50000 == 0) {
  saveCheckpoint(playedGames)
}
```

### Atomic Save
```scala
def saveCheckpoint(gamesPlayed: Int): Unit = {
  val tempFile = checkpointFile + ".tmp"
  val finalFile = checkpointFile

  // Write to temp file
  writeJSON(tempFile, checkpointData)

  // Atomic rename
  Files.move(tempFile, finalFile, ATOMIC_MOVE)
}
```

**Guarantees:**
- No partial writes (atomic rename)
- Previous checkpoint preserved until new one complete
- Crash during save = previous checkpoint still valid

---

## Resume Behavior

### Startup Logic

```python
def start_training():
    checkpoint = find_latest_checkpoint(experiment_path, experiment_tag)

    if checkpoint:
        print(f"Found checkpoint: {checkpoint}")
        print(f"Resuming from {checkpoint.gamesPlayed} games...")
        resume_from_checkpoint(checkpoint)
    else:
        print("No checkpoint found, starting fresh...")
        train_from_scratch()
```

### Loading Process

1. **Validate checkpoint format**
   - Parse JSON (crash if invalid)
   - Check formatVersion (crash if unsupported)
   - Verify all required fields present

2. **Validate architecture match**
   ```scala
   if (checkpoint.networkArchitecture != currentArchitecture) {
     throw Error(s"Architecture mismatch: checkpoint has ${checkpoint.hiddenSize} hidden neurons, code expects $hiddenSize")
   }

   if (checkpoint.hiddenActivation != currentActivation) {
     throw Error(s"Activation mismatch: checkpoint uses ${checkpoint.hiddenActivation}, code uses $currentActivation")
   }
   ```

3. **Validate hyperparameters**
   ```scala
   // Lambda and gamma MUST match
   if (checkpoint.lambda != currentLambda) {
     throw Error(s"Lambda mismatch: checkpoint=${checkpoint.lambda}, current=$currentLambda. Cannot change lambda mid-training.")
   }

   if (checkpoint.gamma != currentGamma) {
     throw Error(s"Gamma mismatch: checkpoint=${checkpoint.gamma}, current=$currentGamma. Cannot change gamma mid-training.")
   }

   // Alpha can be overridden
   if (commandLineAlpha != checkpoint.alpha) {
     println(s"⚠️  Alpha override: checkpoint=${checkpoint.alpha}, using $commandLineAlpha")
   }
   ```

4. **Load weights**
   ```scala
   wInputHidden = checkpoint.weights.wInputHidden
   wHiddenOutput = checkpoint.weights.wHiddenOutput
   bHidden = checkpoint.weights.bHidden
   bOutput = checkpoint.weights.bOutput
   ```

5. **Set game counter**
   ```scala
   playedGames = checkpoint.gamesPlayed
   ```

6. **Update random seed**
   ```scala
   // Deterministic but different from checkpoint
   newSeed = checkpoint.randomSeed + checkpoint.gamesPlayed
   Evaluator.diceRoller = PseudoRandomDiceRoller(newSeed)
   ```

### Log File Behavior

**Metrics CSV:**
- **Always append** (never overwrite)
- Resume will continue from checkpoint game count
- Example:
  ```csv
  gamesPlayed,averageTDError,weightDelta,...
  1000, 0.027, 1.163, ...
  ...
  200000, 0.046, 0.758, ...
  # ← Training stopped, checkpoint saved
  201000, 0.047, 0.721, ...  # ← Resumed
  ...
  ```

**Benchmark CSVs:**
- Also append-only
- Seamless continuation of timeline

---

## Alpha Override

### Use Case
Experiment with learning rate adjustments:
```bash
# Train 0 → 300K with α=0.005
python3 run_experiments.py  # Uses α=0.005 from script

# Resume 300K → 500K with α=0.003 (reduce learning rate)
# Edit run_experiments.py to change alpha to 0.003
python3 run_experiments.py  # Auto-detects checkpoint, overrides alpha
```

### Implementation
```scala
// In RawTd40 constructor
val effectiveAlpha = if (resuming && commandLineAlpha != null) {
  println(s"⚠️  Alpha override: ${checkpoint.alpha} → $commandLineAlpha")
  commandLineAlpha  // Use command line
} else if (resuming) {
  checkpoint.alpha   // Use checkpoint value
} else {
  commandLineAlpha   // Fresh start
}
```

### Constraints
- ✅ Alpha: Can change freely
- ❌ Lambda: Cannot change (crash if different)
- ❌ Gamma: Cannot change (crash if different)
- ❌ Network size: Cannot change (crash if different)
- ❌ Activation: Cannot change (crash if different)

**Rationale:**
- Alpha affects learning speed (safe to change)
- Lambda/gamma affect credit assignment (changing invalidates eligibility traces)
- Architecture changes require new training

---

## Error Handling

### Crash Scenarios (Expected)

**1. Corrupted checkpoint:**
```
Error: Failed to parse checkpoint JSON
File: run_LONG_checkpoint_200000.json
Cause: Unexpected end of file

Action: Delete corrupted checkpoint and resume from previous
```

**2. Architecture mismatch:**
```
Error: Network architecture mismatch
Checkpoint: 40 hidden neurons (LeakyReLU)
Current code: 80 hidden neurons (LeakyReLU)

Action: Either use code matching checkpoint, or delete checkpoint
```

**3. Activation mismatch:**
```
Error: Activation function mismatch
Checkpoint: Sigmoid
Current code: LeakyReLU

Action: Change TdNeuralNetwork.scala line 10 to match checkpoint, or delete checkpoint
```

**4. Lambda/gamma mismatch:**
```
Error: Hyperparameter mismatch - cannot change lambda mid-training
Checkpoint: lambda=0.8
Current: lambda=0.7

Action: Use same lambda value, or delete checkpoint to start fresh
```

### No Recovery Attempted
- Don't try to fix corrupted files
- Don't try to migrate architectures
- Don't silently ignore mismatches
- Fail fast with clear error messages

---

## Override Checkpoint Detection

### To Start Fresh (Ignore Existing Checkpoints)

**Method:** Manual deletion
```bash
# Delete all checkpoints for experiment
rm -rf experiments/006_long_training_leaky_relu/checkpoints/

# Or delete specific checkpoint
rm experiments/006_long_training_leaky_relu/checkpoints/run_LONG_checkpoint_00200000.json
```

**No command-line flag needed:**
- Simpler implementation
- Explicit action required (safer)
- Clear intent

---

## Implementation Checklist

### Core Classes

**1. `Checkpoint.scala` (new file)**
```scala
case class CheckpointMetadata(
  formatVersion: String,
  timestamp: String,
  gamesPlayed: Int,
  experimentTag: String,
  hyperparameters: Hyperparameters,
  networkArchitecture: NetworkArchitecture,
  randomSeed: Long,
  performance: Option[Performance]
)

case class Checkpoint(
  metadata: CheckpointMetadata,
  weights: NetworkWeights
)

object CheckpointManager {
  def save(checkpoint: Checkpoint, path: Path): Unit
  def load(path: Path): Checkpoint
  def findLatest(experimentPath: Path, tag: String): Option[Path]
  def validate(checkpoint: Checkpoint, current: NetworkConfig): Unit
}
```

**2. `RawTd40.scala` modifications**
- Add `saveCheckpoint()` method
- Call in `endGame()` every 50K games
- Add `loadCheckpoint()` in constructor
- Auto-detect checkpoint on startup

**3. `TdNeuralNetwork.scala` modifications**
- Add `saveWeights()` method
- Add `loadWeights()` method
- Return weights as structured data

**4. `Dispatcher.scala` modifications**
- Update random seed on checkpoint resume
- Pass checkpoint detection to agent factory

### Testing Strategy

1. **Basic save/load:**
   - Train 50K → save → load → verify weights identical

2. **Resume continuity:**
   - Train 50K → resume → train to 100K → verify game counter correct

3. **Alpha override:**
   - Train 50K with α=0.003 → resume with α=0.005 → verify new α used

4. **Error handling:**
   - Corrupted JSON → crash with error ✅
   - Architecture mismatch → crash with error ✅
   - Lambda mismatch → crash with error ✅

5. **Log continuity:**
   - Train → checkpoint → resume → verify CSV appends correctly

### Files to Modify

```
multi-gammon-core/src/main/java/org/akoshterek/backgammon/
├── nn/
│   ├── TdNeuralNetwork.scala    # Add save/load weights
│   └── Checkpoint.scala          # NEW: Checkpoint data structures
├── agent/
│   └── raw/RawTd40.scala         # Add checkpoint save/load logic
└── dispatch/
    └── Dispatcher.scala          # Handle seed update on resume
```

---

## Usage Examples

### Scenario 1: Long Training with Auto-Resume

```bash
# Start training
python3 run_experiments.py --run LONG
# Trains 0 → 50K → checkpoint
# Trains 50K → 100K → checkpoint
# ... (user stops at 300K)

# Resume later (automatically)
python3 run_experiments.py --run LONG
# Detects checkpoint at 300K
# Resumes 300K → 350K → ...
# Continues to 1.5M
```

### Scenario 2: Alpha Adjustment

```bash
# Initial training
# run_experiments.py has alpha=0.005
python3 run_experiments.py --run LONG
# Trains to 500K with α=0.005

# Adjust learning rate
# Edit run_experiments.py: change alpha to 0.003
python3 run_experiments.py --run LONG
# Resumes from 500K with α=0.003
# Console: "⚠️  Alpha override: 0.005 → 0.003"
```

### Scenario 3: Crash Recovery

```bash
# Training crashes at 437K games
# Last checkpoint: 400K

# Restart
python3 run_experiments.py --run LONG
# Resumes from 400K
# Loses 37K games (acceptable)
```

### Scenario 4: Start Fresh

```bash
# Delete checkpoints to start over
rm -rf experiments/006_long_training_leaky_relu/checkpoints/

# Run again
python3 run_experiments.py --run LONG
# Starts from 0
```

---

## Performance Considerations

### Checkpoint Size
- Weights: 8,001 floats × 4 bytes = ~32KB
- JSON overhead: ~5MB total per checkpoint
- 30 checkpoints (1.5M games): ~150MB total
- Acceptable for modern systems

### Save Time
- JSON serialization: ~10ms
- Disk write: ~50ms
- Total: <100ms per checkpoint
- Negligible compared to 50K games training time

### Load Time
- JSON parse: ~20ms
- Weight initialization: ~5ms
- Total: <50ms
- Once per training session

---

## Future Enhancements (Not in Initial Version)

### Optional Features for Later
1. **Checkpoint compression:** gzip JSON (~80% size reduction)
2. **Binary format:** Protobuf or custom binary (~95% size reduction)
3. **Remote storage:** S3/cloud backup of checkpoints
4. **Checkpoint pruning:** Auto-delete old checkpoints (keep every 100K)
5. **Web UI:** Visualize checkpoint history and metrics
6. **Multi-checkpoint resume:** Average weights from N checkpoints

### Not Planned
- Graceful recovery from corrupted checkpoints
- Architecture migration/conversion
- Resume from different activation function
- Checkpoint encryption

---

## Summary

**Key Points:**
- ✅ Auto-resume by default (no flags needed)
- ✅ Save every 50K games to `{experiment_path}/checkpoints/`
- ✅ Alpha can be overridden, λ/γ cannot
- ✅ Crash on any validation errors (fail fast)
- ✅ Logs always append (seamless continuation)
- ✅ Dice seed: deterministic offset on resume

**Effort:** 4-5 hours implementation + 1 hour testing

**Priority:** High - enables 1.5M game training runs

**Status:** Ready to implement

---

*For questions or clarifications, refer to this document first.*
