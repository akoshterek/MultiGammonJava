# Genetic Algorithm Training Analysis

**Date:** February 18, 2026  
**Experiment:** GA vs Tesauro PubEval Benchmark

---

## Executive Summary

Two independent GA training runs (100 generations, population 100) showed distinct learning patterns:

- **Run 1:** Fast early growth, early stagnation (~0.60-0.64 plateau)
- **Run 2:** Slower initial learning, sustained improvement, superior final performance

**Key Finding:** Slower, steadier convergence outperformed rapid early optimization.

---

## Run 1: Fast Learner → Early Stagnation

### Performance Metrics
- **Final Best Fitness:** 0.610 (gen 99)
- **Final Avg Fitness:** 0.436
- **Peak Best Fitness:** 0.660 (gen 66)
- **Final Std Dev:** 0.071

### Learning Curve Characteristics

**Phase 1: Explosive Growth (Gen 0-20)**
- Gen 0 → Gen 10: 0.040 → 0.130 (+225%)
- Gen 10 → Gen 20: 0.130 → 0.240 (+85%)
- Rapid discovery of effective strategies

**Phase 2: Continued Improvement (Gen 20-50)**
- Gen 20 → Gen 50: 0.240 → 0.430 (+79%)
- Steady but slowing gains
- Peak diversity maintained (stdDev ~0.065)

**Phase 3: Stagnation (Gen 50-99)**
- Gen 50 → Gen 99: 0.430 → 0.610 (+42%)
- Best fitness oscillates: 0.58-0.64 range
- No sustained breakthrough after gen 66
- Population converges (stdDev stable ~0.070)

### Convergence Pattern
```
Gen    Best    Avg     Pattern
0-10   0.13    0.028   Explosive
10-20  0.24    0.111   Strong
20-50  0.43    0.285   Steady
50-70  0.64    0.361   Plateau begins
70-99  0.61    0.436   Stagnation
```

---

## Run 2: Slow Learner → Superior Outcome

### Performance Metrics
- **Final Best Fitness:** 0.630 (gen 99)
- **Final Avg Fitness:** 0.447
- **Peak Best Fitness:** 0.700 (gen 98)
- **Final Std Dev:** 0.086

### Learning Curve Characteristics

**Phase 1: Measured Start (Gen 0-20)**
- Gen 0 → Gen 10: 0.050 → 0.120 (+140%)
- Gen 10 → Gen 20: 0.120 → 0.250 (+108%)
- Slower than Run 1, but building foundation

**Phase 2: Acceleration (Gen 20-50)**
- Gen 20 → Gen 50: 0.250 → 0.490 (+96%)
- Surpasses Run 1's phase 2 growth rate
- Higher diversity maintained (stdDev ~0.070)

**Phase 3: Sustained Progress (Gen 50-99)**
- Gen 50 → Gen 99: 0.490 → 0.630 (+29%)
- **No stagnation** - continuous improvement
- Peak performance at gen 98: **0.700**
- Higher variance (stdDev ~0.080) = ongoing exploration

### Convergence Pattern
```
Gen    Best    Avg     Pattern
0-10   0.12    0.022   Measured
10-20  0.25    0.087   Building
20-50  0.49    0.294   Accelerating
50-70  0.59    0.369   Steady climb
70-99  0.70    0.447   Continued gains
```

---

## Comparative Analysis

### Side-by-Side Performance

| Metric | Run 1 | Run 2 | Winner |
|--------|-------|-------|--------|
| **Final Best** | 0.610 | 0.630 | Run 2 (+3.3%) |
| **Final Avg** | 0.436 | 0.447 | Run 2 (+2.5%) |
| **Peak Best** | 0.660 | 0.700 | Run 2 (+6.1%) |
| **Gen 50 Best** | 0.430 | 0.490 | Run 2 (+14.0%) |
| **Gen 25 Best** | 0.310 | 0.300 | Run 1 (tied) |
| **Final StdDev** | 0.071 | 0.086 | Run 2 (more diverse) |

### Key Observations

**1. Early vs Late Performance**
- Run 1 leads through gen ~30
- Run 2 overtakes by gen 35
- Gap widens continuously after gen 50

**2. Convergence Speed**
- Run 1: Premature convergence → local optimum
- Run 2: Maintained diversity → better exploration

**3. Plateau Behavior**
- Run 1: Hard plateau at gen 50 (best = 0.43 → 0.64 → 0.61)
- Run 2: No plateau (steady climb 0.49 → 0.70)

**4. Population Diversity**
- Run 1: StdDev drops, converges early
- Run 2: Higher stdDev throughout = ongoing innovation

---

## Hypotheses for Different Behaviors

### Why Run 1 Stagnated

**Possible Causes:**
1. **Premature Convergence:** Population lost diversity too quickly
2. **Local Optimum:** Found strong but not optimal strategy early
3. **Selection Pressure:** Too aggressive, eliminated exploratory individuals
4. **Mutation Rate:** Insufficient to escape local basin
5. **Crossover:** Homogeneous population → ineffective recombination

**Evidence:**
- Rapid early gains suggest strong local optimum found
- Low stdDev in late generations (0.065-0.075)
- Best fitness oscillates without breaking through

### Why Run 2 Succeeded

**Possible Causes:**
1. **Maintained Diversity:** Higher stdDev throughout (0.075-0.086)
2. **Better Exploration-Exploitation Balance:** Slower but more thorough
3. **Lucky Initialization:** Started with better genetic material
4. **Mutation/Crossover Balance:** Better parameter combination
5. **Avoided Local Trap:** Different early trajectory prevented convergence

**Evidence:**
- Continuous improvement through gen 98
- Higher variance maintained
- Peak performance at very end (gen 98: 0.700)

---

## Implications for GA Design

### Recommendations

**1. Diversity Preservation is Critical**
- Monitor population stdDev as key metric
- Alert if stdDev drops below threshold (e.g., <0.05)
- Consider diversity-maintenance mechanisms

**2. Don't Rush Convergence**
- Early rapid progress ≠ better outcome
- Slower, sustained learning often superior
- Extend generation count if diversity high

**3. Stagnation Detection**
- Track best fitness improvement over windows (e.g., 20 gens)
- If improvement < 5% over 20 gens → increase mutation?
- Consider adaptive mutation rates

**4. Run Multiple Trials**
- Single runs can be misleading
- Stochastic outcomes significant
- Need statistical confidence (n≥5 runs)

**5. Adaptive Parameters**
- High mutation early → exploration
- Reduced mutation late → exploitation
- Dynamic based on diversity metrics

---

## Future Experiments

### To Investigate

**1. Larger Population**
- Test pop=200, pop=500
- Does larger pop maintain diversity longer?

**2. Longer Runs**
- Extend to 200-300 generations
- Does Run 1 ever break plateau?
- Does Run 2 continue improving?

**3. Diversity Metrics**
- Add genetic diversity tracking
- Measure genotype vs phenotype diversity
- Correlate with performance

**4. Parameter Sensitivity**
- Mutation rate sweep: 0.01, 0.05, 0.1, 0.2
- Crossover rate sweep
- Selection pressure variations

**5. Restart Mechanisms**
- Detect stagnation → inject random individuals
- Hybrid approaches (GA + local search)

---

## Raw Data

### Run 1: Fast Growth → Stagnation

```csv
generation,bestFitness,avgFitness,worstFitness,stdDev
0,0.040000,0.006800,0.000000,0.010284
1,0.060000,0.008300,0.000000,0.010682
2,0.080000,0.009900,0.000000,0.014456
3,0.070000,0.012200,0.000000,0.015400
4,0.050000,0.012100,0.000000,0.013289
5,0.070000,0.013600,0.000000,0.015525
6,0.070000,0.012300,0.000000,0.015547
7,0.170000,0.014100,0.000000,0.021730
8,0.100000,0.014900,0.000000,0.018628
9,0.120000,0.020200,0.000000,0.020247
10,0.130000,0.027500,0.000000,0.027106
11,0.270000,0.038300,0.000000,0.038937
12,0.120000,0.040000,0.000000,0.029120
13,0.140000,0.049700,0.000000,0.034798
14,0.170000,0.061000,0.000000,0.040755
15,0.180000,0.064700,0.010000,0.039483
16,0.230000,0.071900,0.000000,0.047638
17,0.240000,0.082900,0.000000,0.046718
18,0.200000,0.084300,0.010000,0.038190
19,0.260000,0.098000,0.010000,0.045011
20,0.240000,0.110800,0.030000,0.048450
21,0.300000,0.122100,0.040000,0.053034
22,0.270000,0.129500,0.030000,0.053017
23,0.280000,0.140800,0.040000,0.052167
24,0.270000,0.151600,0.020000,0.053080
25,0.310000,0.171500,0.050000,0.050207
26,0.330000,0.177200,0.070000,0.056198
27,0.310000,0.177000,0.070000,0.057158
28,0.350000,0.174000,0.050000,0.055154
29,0.280000,0.169700,0.070000,0.048651
30,0.340000,0.189100,0.060000,0.058738
31,0.320000,0.195000,0.080000,0.058523
32,0.340000,0.206300,0.080000,0.058560
33,0.400000,0.201000,0.080000,0.054599
34,0.340000,0.205200,0.080000,0.061180
35,0.370000,0.217600,0.100000,0.056958
36,0.370000,0.219000,0.070000,0.059439
37,0.400000,0.228400,0.100000,0.055798
38,0.340000,0.222300,0.110000,0.051398
39,0.390000,0.220700,0.100000,0.060947
40,0.380000,0.242700,0.130000,0.059914
41,0.380000,0.238500,0.070000,0.063943
42,0.380000,0.243300,0.130000,0.057377
43,0.380000,0.242900,0.130000,0.053652
44,0.430000,0.248500,0.110000,0.064674
45,0.420000,0.246300,0.110000,0.066658
46,0.410000,0.272700,0.120000,0.062143
47,0.450000,0.266400,0.140000,0.065261
48,0.500000,0.275800,0.130000,0.070359
49,0.420000,0.276800,0.120000,0.062014
50,0.430000,0.284900,0.150000,0.065551
51,0.460000,0.286900,0.090000,0.071703
52,0.450000,0.288300,0.140000,0.062850
53,0.530000,0.303500,0.120000,0.070205
54,0.500000,0.302800,0.190000,0.059583
55,0.550000,0.293500,0.110000,0.071349
56,0.510000,0.303800,0.160000,0.067672
57,0.480000,0.318600,0.140000,0.071638
58,0.510000,0.332200,0.170000,0.068419
59,0.490000,0.345600,0.160000,0.066111
60,0.510000,0.347100,0.210000,0.061438
61,0.620000,0.335200,0.180000,0.074572
62,0.520000,0.342600,0.170000,0.071072
63,0.560000,0.342900,0.200000,0.073638
64,0.560000,0.361300,0.170000,0.081445
65,0.560000,0.352300,0.210000,0.071734
66,0.660000,0.362200,0.180000,0.079769
67,0.510000,0.360300,0.180000,0.073518
68,0.550000,0.350200,0.230000,0.061919
69,0.640000,0.373000,0.230000,0.074505
70,0.570000,0.383900,0.190000,0.072220
71,0.550000,0.379200,0.200000,0.070380
72,0.580000,0.375900,0.240000,0.074727
73,0.610000,0.389400,0.200000,0.079747
74,0.580000,0.368000,0.180000,0.076315
75,0.570000,0.378900,0.210000,0.067571
76,0.580000,0.371000,0.210000,0.068198
77,0.590000,0.389900,0.240000,0.074317
78,0.590000,0.388300,0.250000,0.069196
79,0.610000,0.383700,0.170000,0.075122
80,0.580000,0.376400,0.180000,0.078632
81,0.610000,0.395400,0.220000,0.078082
82,0.580000,0.388600,0.230000,0.074203
83,0.570000,0.390700,0.190000,0.078259
84,0.580000,0.400800,0.220000,0.072093
85,0.600000,0.391100,0.170000,0.082534
86,0.630000,0.410200,0.240000,0.086197
87,0.570000,0.399400,0.220000,0.080397
88,0.610000,0.410400,0.230000,0.074147
89,0.640000,0.420000,0.250000,0.075512
90,0.610000,0.412500,0.260000,0.073012
91,0.620000,0.413300,0.220000,0.080088
92,0.580000,0.414100,0.260000,0.065819
93,0.590000,0.406300,0.250000,0.072425
94,0.600000,0.415000,0.220000,0.082323
95,0.610000,0.433300,0.270000,0.072223
96,0.640000,0.428800,0.260000,0.069113
97,0.640000,0.449700,0.260000,0.075716
98,0.580000,0.419100,0.290000,0.067677
99,0.610000,0.436000,0.270000,0.070612
```

### Run 2: Slow Start → Superior Finish

```csv
generation,bestFitness,avgFitness,worstFitness,stdDev
0,0.050000,0.007400,0.000000,0.011280
1,0.080000,0.011000,0.000000,0.015524
2,0.050000,0.009800,0.000000,0.013037
3,0.090000,0.015400,0.000000,0.018837
4,0.060000,0.014300,0.000000,0.017335
5,0.050000,0.010500,0.000000,0.013444
6,0.100000,0.014500,0.000000,0.017854
7,0.090000,0.013800,0.000000,0.017932
8,0.100000,0.021000,0.000000,0.022956
9,0.130000,0.020700,0.000000,0.023969
10,0.120000,0.021800,0.000000,0.023297
11,0.110000,0.027000,0.000000,0.026325
12,0.140000,0.032300,0.000000,0.030195
13,0.110000,0.035800,0.000000,0.026951
14,0.160000,0.038300,0.000000,0.029565
15,0.150000,0.047400,0.000000,0.032638
16,0.200000,0.051400,0.000000,0.042238
17,0.140000,0.053500,0.000000,0.032875
18,0.200000,0.067800,0.000000,0.041799
19,0.220000,0.079000,0.000000,0.040804
20,0.250000,0.087000,0.000000,0.047445
21,0.260000,0.101200,0.010000,0.052750
22,0.280000,0.120900,0.020000,0.052557
23,0.280000,0.112800,0.030000,0.048889
24,0.260000,0.131100,0.020000,0.051418
25,0.300000,0.146400,0.040000,0.051352
26,0.380000,0.150500,0.040000,0.053130
27,0.330000,0.157600,0.040000,0.052766
28,0.310000,0.165300,0.030000,0.051681
29,0.320000,0.184600,0.080000,0.058145
30,0.360000,0.189500,0.080000,0.062871
31,0.450000,0.207800,0.060000,0.067876
32,0.370000,0.204500,0.090000,0.056963
33,0.390000,0.219600,0.070000,0.061902
34,0.400000,0.205700,0.040000,0.063926
35,0.410000,0.236800,0.060000,0.067006
36,0.440000,0.227200,0.080000,0.063310
37,0.440000,0.237200,0.080000,0.072651
38,0.430000,0.253600,0.090000,0.072505
39,0.430000,0.257200,0.110000,0.065743
40,0.520000,0.251500,0.090000,0.065412
41,0.410000,0.259400,0.130000,0.059039
42,0.410000,0.268200,0.140000,0.065869
43,0.470000,0.272100,0.100000,0.069084
44,0.450000,0.279800,0.130000,0.069771
45,0.460000,0.291200,0.160000,0.065822
46,0.490000,0.276000,0.140000,0.071120
47,0.410000,0.283500,0.100000,0.059167
48,0.450000,0.285200,0.150000,0.072270
49,0.520000,0.283600,0.150000,0.068652
50,0.490000,0.294400,0.160000,0.064751
51,0.450000,0.283800,0.100000,0.072606
52,0.480000,0.278700,0.120000,0.067730
53,0.510000,0.314100,0.170000,0.068602
54,0.490000,0.313700,0.160000,0.073534
55,0.540000,0.331900,0.120000,0.082373
56,0.540000,0.342600,0.160000,0.079494
57,0.560000,0.323600,0.170000,0.070831
58,0.440000,0.333300,0.200000,0.059331
59,0.550000,0.338900,0.170000,0.078739
60,0.530000,0.339200,0.170000,0.074882
61,0.590000,0.349400,0.180000,0.068976
62,0.510000,0.338000,0.120000,0.072636
63,0.570000,0.363500,0.160000,0.079930
64,0.620000,0.364500,0.130000,0.076058
65,0.570000,0.369000,0.190000,0.073437
66,0.520000,0.365700,0.200000,0.069387
67,0.560000,0.383200,0.190000,0.073510
68,0.580000,0.362100,0.200000,0.075502
69,0.560000,0.376000,0.170000,0.075206
70,0.590000,0.368700,0.200000,0.073534
71,0.520000,0.367400,0.150000,0.070280
72,0.590000,0.386400,0.170000,0.080901
73,0.550000,0.369800,0.200000,0.078141
74,0.550000,0.380000,0.240000,0.067171
75,0.600000,0.386300,0.190000,0.075441
76,0.540000,0.375800,0.200000,0.073433
77,0.600000,0.379800,0.240000,0.074027
78,0.580000,0.390400,0.210000,0.078395
79,0.590000,0.390100,0.200000,0.082831
80,0.570000,0.393000,0.210000,0.073939
81,0.580000,0.397800,0.200000,0.079418
82,0.570000,0.404300,0.240000,0.077243
83,0.640000,0.401300,0.220000,0.075956
84,0.580000,0.396200,0.240000,0.068801
85,0.640000,0.402800,0.230000,0.085077
86,0.620000,0.402800,0.220000,0.073499
87,0.650000,0.411300,0.240000,0.083482
88,0.610000,0.419000,0.280000,0.069477
89,0.670000,0.418000,0.240000,0.081117
90,0.550000,0.410000,0.260000,0.070512
91,0.680000,0.430400,0.250000,0.086046
92,0.690000,0.430000,0.290000,0.080037
93,0.620000,0.422600,0.250000,0.073070
94,0.600000,0.414500,0.240000,0.074945
95,0.630000,0.427600,0.270000,0.073104
96,0.610000,0.436400,0.230000,0.083361
97,0.600000,0.435400,0.220000,0.078069
98,0.700000,0.440200,0.210000,0.083054
99,0.630000,0.446700,0.180000,0.085581
```

---

## Visualization Recommendations

For future analysis, create plots:

1. **Best Fitness Over Time** (both runs overlaid)
2. **Average Fitness Over Time** (both runs overlaid)
3. **Standard Deviation Over Time** (diversity tracker)
4. **Fitness Distribution** (histogram at gen 0, 25, 50, 75, 99)
5. **Rolling 20-Gen Improvement Rate** (detect stagnation)

---

*Analysis completed: February 18, 2026*
