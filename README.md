![Build Status](https://github.com/akoshterek/MultiGammonJava/actions/workflows/gradle.yml/badge.svg)

# MultiGammonJava

A Scala/Java implementation of TD-Gammon using temporal-difference reinforcement learning with eligibility traces. Trains a neural network to play backgammon through self-play, similar to Tesauro's famous TD-Gammon (1992).

**Current Status:** Successfully replicated core TD-Gammon results with modern optimizations (SIMD, LeakyReLU). Achieved +1.066 points/game vs Random at 200K games.

## Quick Start

```bash
# Run long training (1.5M games)
python3 run_experiments.py --run LONG

# List available experiments
python3 run_experiments.py --list
```

## Documentation

- **[PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)** - Complete project guide (start here!)
- **[experiments/RECOMMENDATIONS.md](experiments_td/RECOMMENDATIONS.md)** - Detailed experimental analysis
- **[experiments/005_sigmoid_test/EXPERIMENT_005_RESULTS.md](experiments_td/005_sigmoid_test/EXPERIMENT_005_RESULTS.md)** - Latest experiment findings

## Key Features

- ✅ TD(λ) learning with eligibility traces
- ✅ LeakyReLU activation (70% better than Sigmoid)
- ✅ SIMD-optimized neural network (~3x speedup)
- ✅ Real-time weight diagnostics
- ✅ Comprehensive metrics logging

## Optimal Configuration

```
Neural Network: 198 inputs → 40 hidden (LeakyReLU) → 1 output (Sigmoid)
Hyperparameters: α=0.003, λ=0.8, γ=0.99
Training Target: 1,500,000 games
```

## Credits

Some service code borrowed from GNU Backgammon and Steffen Nissen PhD Thesis.
Original implementation by Alex Koshterek.
Recent optimizations: February 2026.

## License

See LICENSE file.
