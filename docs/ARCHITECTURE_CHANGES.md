# Architecture Changes

## 5-Output Neural Network (February 20, 2026)

### Change
Updated `GeneticAgent` from 1-output to 5-output neural network architecture.

### Motivation
The 1-output network could only predict P(win), making it impossible to explicitly model gammon and backgammon probabilities. This limitation was identified during bearoff database analysis, where we discovered that gammon awareness is critical for accurate position evaluation.

### Architecture Details

**Before:**
```scala
// Network: 198 inputs → 40 hidden → 1 output
val output = network.evaluate(input)  // Returns single float: P(win)
rewardArray(OUTPUT_WIN) = output
```

**After:**
```scala
// Network: 198 inputs → 40 hidden → 5 outputs
val output = Array.ofDim[Float](5)
network.forward(input, output)
new Reward(output)  // Returns all 5 probabilities
```

**5 Output Values (matching GNU Backgammon):**
1. `OUTPUT_WIN` - P(win)
2. `OUTPUT_WINGAMMON` - P(win gammon | win)
3. `OUTPUT_WINBACKGAMMON` - P(win backgammon | win)
4. `OUTPUT_LOSEGAMMON` - P(lose gammon | lose)
5. `OUTPUT_LOSEBACKGAMMON` - P(lose backgammon | lose)

### Move Evaluation
Uses equity formula that directly aligns with PPG optimization:

```scala
equity = (P(win) * 2.0 - 1.0) 
         + (P(win gammon) - P(lose gammon))
         + (P(win backgammon) - P(lose backgammon))
```

### Weight Count Impact
- **Before:** 40 hidden → 1 output = 40 weights + 1 bias = 41 parameters
- **After:** 40 hidden → 5 outputs = 200 weights + 5 biases = 205 parameters
- **Total increase:** 164 additional parameters (0.4% of total network)

### Backward Compatibility
**BREAKING CHANGE:** Old checkpoints with 1 output are incompatible with new 5-output architecture. Training must start from scratch.

Checkpoint system properly stores `outputSize` field, so new checkpoints will load correctly.

### Expected Benefits
1. **Gammon-aware decisions** - Network can distinguish moves with gammon potential
2. **Better PPG optimization** - Output directly computes equity/PPG
3. **Richer training signal** - 5 targets per position instead of 1
4. **Matches proven architecture** - Aligns with GnuBG and TD-Gammon designs

### Testing Strategy
Keep hidden units at 40 to isolate the impact of 5-output architecture from network capacity changes. Future experiments may increase to 60-80 hidden units.

### Related Files Modified
- `GeneticAgent.scala` - Updated `evalContact`, `createRandom`, `fromWeights`
- Architecture verified in: `GACheckpoint.scala`, `SimpleNeuralNetwork.scala`
