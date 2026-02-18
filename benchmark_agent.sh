#!/bin/bash

echo "Run benchmarking..."
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B Heuristic \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/002_simple_pubeval"

./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B PubEval \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/002_simple_pubeval"

./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B GnuBg \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/002_simple_pubeval"

# -A Heuristic -B SimpleHeuristic -G 10000 -T 0 --experiment-path experiments/006_long_training_leaky_relu