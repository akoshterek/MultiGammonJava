#!/bin/bash

echo "Run benchmarking..."
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B Heuristic \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/004_pubeval_bearoff"

./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B PubEval \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/004_pubeval_bearoff"

./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent \
  -B GnuBg \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/004_pubeval_bearoff"

#./gradlew :multi-gammon-core:runMultiGammon --args="\
#  -A GeneticAgentBearoff \
#  -B GeneticAgent \
#  -T 0 \
#  -G 10000 \
#  --experiment-path experiments_ga/003_pubeval_bearoff"
