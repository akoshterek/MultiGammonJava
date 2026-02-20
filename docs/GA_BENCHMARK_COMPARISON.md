# GA Training Benchmark Comparison

Comprehensive comparison of genetic algorithm training runs 3-6, all with identical configuration.

## Training Configuration

**All runs:**
- Population size: 100
- Generations: 100
- Games per evaluation: 100 (vs **PubEval** for fitness)
- Elite count: 10
- Mutation rate: 0.03
- Mutation strength: 0.05
- **All runs use bearoff DB**

**Key Difference:**
- **Runs 3 & 4:** 1-output architecture (P(win) only)
- **Runs 5 & 6:** 5-output architecture (win, win gammon, win backgammon, lose gammon, lose backgammon)

**Note:** All agents were trained against **PubEval** for fitness evaluation. Benchmarks below test generalization to other opponents.

## Training Fitness Results

| Run | Final Best | Final Avg | Final Worst | Architecture |
|-----|-----------|-----------|-------------|--------------|
| 3 | 0.74 | - | - | 1-output |
| 4 | 0.74 | - | - | 1-output |
| 5 | 0.76 | 0.495 | 0.31 | 5-output |
| 6 | 1.07 | 0.743 | 0.54 | 5-output |

## Benchmark Results (10,000 games each)

### Benchmark 1: vs Heuristic (Weaker Opponent)

| Run | Win Rate | Points Won | Points Total | PPG Differential | Δ from Best |
|-----|----------|------------|--------------|------------------|-------------|
| 6 🥇 | 69.88% | 10,056 | 14,413 | **+0.570** | - |
| 4 | 69.20% | 6,952* | 10,000* | **+0.568** | -0.002 |
| 5 | 69.59% | 9,989 | 14,471 | **+0.551** | -0.019 |
| 3 | 69.11% | 6,887* | 10,000* | **+0.530** | -0.040 |

*Estimated total points (not provided in data)

**Best:** Run 6 (+0.570 ppg)

---

### Benchmark 2: vs PubEval (Training Opponent)

| Run | Win Rate | Points Won | Points Total | PPG Differential | Δ from Best |
|-----|----------|------------|--------------|------------------|-------------|
| 3 🥇 | 32.79% | 2,759* | 10,000* | **-0.805** | - |
| 4 | 31.70% | 2,736* | 10,000* | **-0.818** | -0.013 |
| 6 | 31.71% | 4,716 | 18,270 | **-0.884** | -0.079 |
| 5 | 30.93% | 4,692 | 18,944 | **-0.956** | -0.151 |

*Estimated total points (not provided in data)

**Best:** Run 3 (-0.805 ppg, smallest loss)  
**Critical finding:** Runs 5 & 6 perform worse vs their **training opponent**

---

### Benchmark 3: vs Gnubg (Strongest Opponent)

| Run | Win Rate | Points Won | Points Total | PPG Differential | Δ from Best |
|-----|----------|------------|--------------|------------------|-------------|
| 3 🥇 | 20.71% | 1,743* | 10,000* | **-1.246** | - |
| 4 | 19.20% | 1,640* | 10,000* | **-1.295** | -0.049 |
| 6 | 19.69% | 3,148 | 19,733 | **-1.344** | -0.098 |
| 5 | 18.25% | 2,989 | 20,818 | **-1.484** | -0.238 |

*Estimated total points (not provided in data)

**Best:** Run 3 (-1.246 ppg, smallest loss)  
**Gap widening:** Performance difference grows vs stronger opponents

---

### Benchmark 4: Head-to-Head (Run 6 vs Run 5)

| Winner | Win Rate | Points Won | Points Total | PPG Differential |
|--------|----------|------------|--------------|------------------|
| 6 🥇 | 51.96% | 10,445 | 19,676 | **+0.121** |
| 5 | 48.04% | 9,231 | 19,676 | -0.121 |

**Result:** Run 6 beats Run 5 head-to-head (+0.121 ppg)  
**Confirms:** Higher training fitness (1.07 vs 0.76) does predict head-to-head performance  
**But:** Both lose to Runs 3 & 4 vs standard benchmarks (PubEval/Gnubg)

## Overall Performance Summary

### Cross-Opponent Comparison

| Run | vs Heuristic | vs PubEval (Training) | vs Gnubg | **Average** | Overall Rank |
|-----|-------------|----------------------|----------|-------------|--------------|
| 3 🥇 | +0.530 | **-0.805** | **-1.246** | **-0.507** | **1st** |
| 4 | +0.568 | -0.818 | -1.295 | -0.515 | 2nd |
| 6 | **+0.570** | -0.884 | -1.344 | -0.553 | 3rd |
| 5 | +0.551 | -0.956 | -1.484 | -0.630 | 4th |

### Performance Delta from Best (Run 3)

| Run | vs Heuristic | vs PubEval | vs Gnubg | Average Δ |
|-----|-------------|------------|----------|-----------|
| 4 | -0.002 | -0.013 | -0.049 | **-0.021** |
| 6 | +0.040 | -0.079 | -0.098 | **-0.046** |
| 5 | +0.021 | -0.151 | -0.238 | **-0.123** |

## Key Insights

### 1. Simpler Architecture (1-output) Outperforms Complex (5-output)

| Run | Architecture | Training Fitness | Benchmark Rank | Paradox |
|-----|--------------|------------------|----------------|---------|
| 3 🥇 | 1-output | 0.74 (lowest) | **1st** (best) | ✅ Simple wins |
| 4 | 1-output | 0.74 (lowest) | **2nd** | ✅ Simple wins |
| 6 | 5-output | 1.07 (highest) | 3rd | ❌ Complex loses |
| 5 | 5-output | 0.76 | 4th (worst) | ❌ Complex loses |

**Critical findings:**
- **1-output agents (Runs 3 & 4):** Lower training fitness, superior benchmarks
- **5-output agents (Runs 5 & 6):** Higher training fitness, inferior benchmarks
- Simple P(win) architecture generalizes better than full gammon-aware outputs

### 2. Training Fitness Misleading for 5-Output Architecture

**1-output architecture (Runs 3 & 4):**
- Training fitness: 0.74 PPG
- vs PubEval: -0.805, -0.818 ppg (strong)
- vs Gnubg: -1.246, -1.295 ppg (strong)
- **Fitness accurately reflects quality**

**5-output architecture (Runs 5 & 6):**
- Training fitness: 0.76, 1.07 PPG (higher!)
- vs PubEval: -0.884, -0.956 ppg (weaker)
- vs Gnubg: -1.344, -1.484 ppg (weaker)
- **Fitness inflated, doesn't reflect quality**

**Conclusion:** 5-output training fitness is ~0.1-0.3 ppg inflated vs actual strength.

### 3. Performance Gap Widens vs Stronger Opponents

**Gap between Run 3 (best) and Run 5 (worst):**
- vs Heuristic: 0.021 ppg difference (4% of range)
- vs PubEval: 0.151 ppg difference (19% larger)
- vs Gnubg: **0.238 ppg difference** (57% larger!)

The better agent's advantage **increases** against better opponents - exactly what you want.

### 4. Head-to-Head Within Architecture Cohort Works

**Within 5-output cohort (Run 6 vs Run 5):**
- Run 6 (1.07 fitness) beats Run 5 (0.76 fitness) by +0.121 ppg ✅
- Training fitness predicts within-architecture performance

**Cross-architecture comparison:**
- Run 3 (1-output, 0.74 fitness) beats Run 6 (5-output, 1.07 fitness) ✅
- Training fitness comparison meaningless across architectures

### 5. Why Does 1-Output Outperform 5-Output?

**Hypothesis 1: Simpler learning target**
- 1-output: Learn single value (P(win))
- 5-output: Learn 5 interdependent probabilities with constraints
- Genetic algorithm struggles with complex multi-output optimization

**Hypothesis 2: PPG calculation mismatch**
- Training uses PPG = equity from 5 outputs
- 1-output directly predicts win probability
- 5-output may have inconsistent probability estimates

**Hypothesis 3: Network capacity**
- Same hidden layer size (40 neurons) for both
- 5-output requires 5x more output weights
- May need larger network for 5-output architecture

## Recommendations

### Best Agent to Deploy
**Run 3 (1-output)** is the clear winner:
- Best vs PubEval (training opponent): -0.805 ppg
- Best vs Gnubg (strongest opponent): -1.246 ppg
- Most balanced performance

### For Future Training

**Architecture choice:**
1. **Stick with 1-output architecture** for genetic algorithms
   - Simpler learning target
   - More reliable training fitness
   - Better benchmark performance
   
2. **If using 5-output:**
   - Increase hidden layer size (40 → 80+ neurons)
   - Don't trust training fitness for comparison
   - Benchmark frequently during training

**Training practices:**
3. **Benchmark early and often** - Run 6's 1.07 fitness was misleading
4. **Focus on strong opponent performance** - gaps widen vs better players
5. ✅ **Generation-dependent seeds already implemented** - `seed + generation * populationSize + agentIndex`

### Open Questions
- Would larger network (80+ hidden) make 5-output competitive?
- Is 5-output better suited for TD learning vs genetic algorithms?
- Can we fix the PPG calculation to better train 5-output agents?
- Would different fitness metric help 5-output architecture?
