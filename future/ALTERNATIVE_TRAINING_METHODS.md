# Alternative Training Methods for Backgammon Self-Play

**Date:** February 2026
**Context:** Alternatives to TD(λ) for consumer laptop training

---

## Overview

This document explores practical alternatives to TD(λ) for training backgammon agents via self-play on consumer hardware. All methods assume similar network architecture (198→40→1) and focus on feasibility for laptop training.

---

## 1. Policy Gradient Methods (PPO/A2C)

**Proximal Policy Optimization** or **Advantage Actor-Critic**

### Description
Modern reinforcement learning algorithms that directly optimize a policy network using gradient ascent on expected reward. Unlike TD(λ) which learns values, these methods learn both policy (what moves to make) and value (how good positions are).

### Pros
- More modern and often more sample efficient than TD(λ)
- Better exploration through entropy bonuses
- More stable training with clipped objectives (PPO)
- Less prone to catastrophic forgetting

### Cons
- Needs separate value network (or shared backbone with two heads)
- Slightly more complex implementation
- Requires tuning more hyperparameters (clip ratio, entropy coefficient)

### Laptop Feasibility
✅ **Very good** - Similar compute requirements to TD(λ)

### Implementation Notes
- Replace TD updates with policy gradient + value loss
- Use advantage estimation (GAE) for variance reduction
- Batch updates every N games for stability

### Expected Training
500K-1M games to reach strong play (comparable to TD(λ))

---

## 2. AlphaZero-lite (MCTS + Neural Net)

**Simplified AlphaGo Zero approach without heavy residual networks**

### Description
Combines Monte Carlo Tree Search (MCTS) for move planning with a neural network that guides the search. The network learns from the MCTS-improved policy and game outcomes.

### Pros
- State-of-the-art approach (best known method for board games)
- Combines planning (MCTS) with learning (neural net)
- Very strong play even with small networks
- Explicit exploration through MCTS

### Cons
- MCTS simulations are CPU intensive
- Needs more memory for tree storage
- Complex implementation with many moving parts

### Laptop Feasibility
⚠️ **Marginal** - Would need reduced simulations (50-100 per move instead of 800+)

### Implementation Notes
- Small network like current: 198→40→1 for value + policy head
- Lightweight MCTS with reduced simulation count
- Store MCTS-improved policies as training targets

### Expected Training
200K-500K games with MCTS guidance (fewer games but more compute per game)

---

## 3. Evolutionary Strategies (ES)

**CMA-ES or simple genetic algorithms**

### Description
Treat network weights as a genome and evolve them through selection, crossover, and mutation. No gradient computation needed - fitness is determined by win rate against opponents.

### Pros
- No backpropagation needed - just forward passes
- Trivially parallelizable across CPU cores
- Robust to local minima (population diversity)
- Simple to implement and debug
- Works well with non-differentiable objectives

### Cons
- Generally less sample efficient than gradient methods
- Needs population of networks (memory overhead)
- Convergence can be slow for large networks
- Requires careful tuning of mutation rates

### Laptop Feasibility
✅ **Good** - Embarrassingly parallel across cores, can utilize all 8-16 threads

### Implementation Notes
- Maintain population of 20-50 networks
- Evaluate fitness through tournament play
- Select top performers, breed offspring with mutation
- Population diversity prevents premature convergence

### Expected Training
1M-2M games across entire population to reach strong play

### Detailed Implementation
See **EVOLUTIONARY_STRATEGIES_DETAILS.md** for comprehensive guide including:
- Three practical algorithms (Simple GA, CMA-ES, OpenAI ES)
- Complete code examples in Scala
- Hyperparameter tuning guide
- Expected training timelines and convergence patterns
- Hybrid approach combining ES + TD(λ)

---

## 4. Imitation + Self-Play Hybrid ⭐ RECOMMENDED

**Bootstrap from GNU Backgammon games, then fine-tune with self-play**

### Description
First train the network to imitate expert play from GNU Backgammon, then switch to self-play for refinement. Combines the best of supervised learning (fast convergence) and reinforcement learning (discovering novel strategies).

### Pros
- 10-100x faster convergence compared to pure self-play
- Avoids random walk of early training
- Starts with reasonable features and strategy
- Still discovers novel moves beyond GNU

### Cons
- Needs existing expert data (GNU Backgammon)
- May inherit biases from expert
- Requires two-phase training pipeline

### Laptop Feasibility
✅✅ **Excellent** - Supervised learning is very fast

### Implementation
1. **Phase 1: Imitation (1-2 hours)**
   - Generate 50K positions from GNU vs GNU games
   - Supervised learning: minimize MSE(network_output, gnu_eval)
   - Gets you to ~60% win rate vs Random

2. **Phase 2: Self-Play (1-2 days)**
   - Switch to self-play TD(λ) for 100K-200K games
   - Gets you to 80%+ vs Random
   - Discovers strategies beyond GNU

### Expected Training
150K total games to strong play (much less than pure self-play)

### Data Generation
```bash
# Generate training data from GNU
for i in {1..50000}; do
  play_game gnu_vs_gnu
  sample_positions_and_evaluations
done
```

---

## 5. Monte Carlo RL (no eligibility traces)

**Simple outcome-based learning without eligibility traces**

### Description
Simplified version of TD learning where weights are only updated at game end based on final outcome. No need to maintain eligibility traces or do TD error backups during play.

### Pros
- Simpler than TD(λ) - no trace bookkeeping
- Unbiased estimates (uses actual outcomes)
- Easy to implement and understand
- Lower memory overhead

### Cons
- Higher variance (only one signal per game)
- Slower convergence due to sparse updates
- Less credit assignment to early moves
- Less sample efficient than TD(λ)

### Laptop Feasibility
✅ **Very good** - Simpler and lighter than TD(λ)

### Implementation Notes
- Play full game, record all positions
- Update all position evaluations toward final outcome
- Use outcome (win=1, loss=0) as target
- Can batch updates for stability

### Expected Training
2M-3M games (slower convergence than TD(λ))

---

## Comparison Table

| Method | Sample Efficiency | Implementation Complexity | Laptop Feasible | Training Time | Parallelization |
|--------|------------------|---------------------------|-----------------|---------------|-----------------|
| TD(λ) (current) | Good | Medium | ✅ Yes | 1-1.5M games | Single thread |
| PPO/A2C | Good | Medium-High | ✅ Yes | 500K-1M games | Batch parallel |
| AlphaZero-lite | Excellent | High | ⚠️ Marginal | 200K-500K games | MCTS parallel |
| Evolutionary Strategies | Poor-Medium | Low | ✅ Yes | 1M-2M games | Highly parallel |
| Imitation + Self-Play | Excellent | Medium | ✅✅ Best | 150K games | Single thread |
| Monte Carlo RL | Poor | Low | ✅ Yes | 2M-3M games | Single thread |

---

## Recommendation

**For immediate experimentation:** Option 4 (Imitation + Self-Play)
- Fastest path to strong play
- Leverages existing GNU Backgammon expertise
- 10x less training time than pure self-play

**For pure self-play research:** Option 1 (PPO)
- More modern and robust than TD(λ)
- Similar training time but better exploration
- Industry standard for RL

**For multi-core utilization:** Option 3 (Evolutionary Strategies)
- Best use of laptop's parallel processing
- Simple to implement
- Robust and easy to debug

---

## Next Steps

If exploring alternatives:
1. Start with Imitation + Self-Play for quick wins
2. Compare convergence speed to current TD(λ) baseline
3. Experiment with PPO if gradient methods preferred
4. Try ES if wanting to utilize all CPU cores effectively

Each method has trade-offs - TD(λ) is a solid baseline and your current implementation is working well!
