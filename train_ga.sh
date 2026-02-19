#!/bin/bash

./gradlew :multi-gammon-core:runMultiGammon --args="\
  --ga-train \
  --ga-population 100 \
  --ga-generations 100 \
  --ga-elite 10 \
  --ga-mutation-rate 0.03 \
  --ga-mutation-strength 0.05 \
  -G 100 \
  --benchmark-opponents 'PubEval' \
  --experiment-path experiments_ga/004_pubeval_bearoff"
