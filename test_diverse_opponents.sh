#!/bin/bash
# Test script for diverse opponents training

# Example 1: Pure self-play (default behavior)
echo "Test 1: Pure self-play (backward compatible)"
./gradlew :multi-gammon-core:runMultiGammon --args="\
  -A RawTd40 \
  -B SimpleHeuristic \
  -T 1500000 \
  -G 1000 \
  -P 50000 \
  --alpha 0.0003 \
  --lambda 0.8 \
  --gamma 0.99 \
  --experiment-path experiments/010_bugfix_selfplay \
  --experiment-tag bugfix_test \
  --benchmark-opponents 'Random,SimpleHeuristic,Heuristic' \
  --training-opponents 'self:50,Heuristic:50' \
  --use-output-bias false"

# Example 2: Diverse opponents (50% self-play, 25% SimpleHeuristic, 25% Random)
echo "Test 2: Diverse opponents training"
#./gradlew :multi-gammon-core:runMultiGammon --args="\
#  -A RawTd40 \
#  -B SimpleHeuristic \
#  -T 1500000 \
#  -G 1000 \
#  -P 50000 \
#  --alpha 0.001 \
#  --lambda 0.8 \
#  --gamma 0.99 \
#  --experiment-path experiments/007_test_diverse \
#  --experiment-tag test_diverse \
#  --training-opponents 'SimpleHeuristic:100' \
#  --benchmark-opponents 'Random,SimpleHeuristic,PubEval' \
#  --use-output-bias false"


# Example 3: No self-play (supervised learning from heuristics)
#echo "Test 3: Supervised learning from heuristics"
#./gradlew :multi-gammon-core:runMultiGammon --args="\
#  -A RawTd40 \
#  -B SimpleHeuristic \
#  -T 10000 \
#  -G 1000 \
#  -P 5000 \
#  --alpha 0.003 \
#  --lambda 0.8 \
#  --gamma 0.99 \
#  --experiment-path experiments/test_supervised \
#  --experiment-tag test_supervised \
#  --training-opponents 'SimpleHeuristic:60,Random:40' \
#  --benchmark-opponents 'Random,SimpleHeuristic'"

echo "All tests defined. Check results in experiments/ directory"
