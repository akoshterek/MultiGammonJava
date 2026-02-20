#!/bin/bash

./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A GeneticAgent6 \
  -B GeneticAgent5 \
  -T 0 \
  -G 10000 \
  --experiment-path experiments_ga/006_pubeval_5out"