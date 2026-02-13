# 4th run
400K games

| Run   | α (alpha) | λ (lambda) | γ (gamma) | Goal                                                          |
| ----- | --------- | ---------- | --------- | ------------------------------------------------------------- |
| **N** | 0.006     | 0.7        | 0.99      | Boost learning speed while retaining long planning horizon    |
| **O** | 0.004     | 0.7        | 0.99      | Like L, but a bit more aggressive — avoid stuck local minima  |
| **P** | 0.005     | 0.5        | 0.95      | Shorter trace + shorter horizon = faster feedback, less noise |
| **Q** | 0.006     | 0.6        | 1.0       | Balanced mix — mild trace, full discounting                   |

---

### 📊 Learning Behavior Summary

#### 🅽 Run N
- **TD Error**: Smooth decline to 0.028 by 100K, then **major crisis at 173K** (spiked to 0.051). Dramatic recovery after 240K, dropping to 0.014 range.
- **Weight Delta**: Normal decay to ~0.09 by 100K, then catastrophic spikes (5.0+, peaking at 17.08 at 239K). High volatility throughout crisis period.
- **vs Random**: Started weak (0.09-0.36), ended strong (0.89-0.92 in final stretch).
- **vs Heuristic**: Consistently losing (-1.2 to -1.3 range).
- **Conclusion**: ⚠️ **Survived major mid-training collapse but showed remarkable late-stage recovery.** Crisis likely due to high α + long horizon causing temporary divergence.

#### 🅾️ Run O
- **TD Error**: Healthy decline to 0.019 by 100K, then **massive divergence at 131K** (jumped to 0.050+). Brief crash at 159K, followed by persistent instability (0.034-0.057 range).
- **Weight Delta**: Started at 1.56, normalized to 0.19 by 100K, then constant high volatility (peaks up to 8.59).
- **vs Random**: Best consistent performance (0.58-0.82 throughout).
- **vs Heuristic**: Best among all runs (-0.75 to -0.95).
- **Conclusion**: ⚠️ **Never fully recovered from mid-training crisis.** Lower α wasn't enough to prevent instability with long planning horizon (γ=0.99).

#### 🅿️ Run P
- **TD Error**: High throughout (0.048-0.058), spike at 172K, then **catastrophic collapse at 302K** (0.059 → 0.006 → 0.001). Partial recovery afterwards.
- **Weight Delta**: Persistently high with **devastating spikes at 302K** (20.37, 10.79, 9.43, 12.33, 13.98). Complete learning breakdown.
- **vs Random**: Poor throughout, often negative, ended at 0.37.
- **vs Heuristic**: Consistently bad (-0.97 to -1.36).
- **Conclusion**: ❌ **CATASTROPHIC FAILURE.** Even with shorter trace (λ=0.5) and horizon (γ=0.95), mid-range α caused complete destabilization. Worst performing run.

> The 302K collapse shows classic overlearning symptoms: sudden TD error drop to near-zero followed by massive weight oscillations indicates the network briefly "overfitted" to recent experiences, then tried to correct via huge weight updates, destroying previous learning.

#### 🆀 Run Q
- **TD Error**: Climbed to 0.047 by 50K, **early divergence at 87-94K** (peaked at 0.065+), then crashed back to 0.046-0.050 range. Relatively stable afterwards (0.037-0.039).
- **Weight Delta**: Started 1.35, major spike at 87K (2.96), peak at 172K (5.24), then moderate volatility (0.5-3.5 range).
- **vs Random**: Started terrible (negative), recovered late (0.44-0.65 in final stretch).
- **vs Heuristic**: Losing (-0.82 to -1.04 range).
- **Conclusion**: ⚠️ **Reproducible early instability around 87-94K games.** Full discounting (γ=1.0) with α=0.006 creates early divergence pattern. Stabilizes after crisis but never achieves strong performance.

> **User note confirmed**: Run Q was repeated multiple times and consistently showed instability around the 87-94K game mark, making this a reproducible failure mode.

---

### 🔍 Key Insights

**Instability Patterns:**
- All runs experienced significant instability, suggesting parameter combinations are too aggressive for 400K training
- **Run N**: Late crisis (173K) with recovery — suggests parameters work but need longer training
- **Run O**: Mid crisis (131K) without recovery — lower α delays but doesn't prevent divergence
- **Run P**: Catastrophic late failure (302K) — shorter λ and γ insufficient to stabilize learning
- **Run Q**: Early predictable crisis (87-94K) — full discounting causes reproducible divergence

**Best Performing:**
- **vs Random**: Run N (final: 0.89-0.92) after surviving crisis
- **vs Heuristic**: Run O (-0.75 to -0.95) most consistent
- **Stability**: None achieved smooth convergence — all had major crises

**Recommendations:**
- Consider reducing α further (0.002-0.003 range) for 400K+ training
- γ=1.0 appears problematic — prefer γ=0.95-0.99
- May need adaptive learning rate that decreases over training
- Current parameters better suited for shorter training (200K) or need more conservative values for 400K+
