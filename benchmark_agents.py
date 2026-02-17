#!/usr/bin/env python3
"""
Benchmark script to verify agent strength ordering:
Random < SimpleHeuristic < Heuristic

This helps validate that SimpleHeuristic provides a good intermediate benchmark.
"""
import subprocess
import sys

def run_match(agent_a, agent_b, games=10000):
    """Run a match between two agents and return PPG for agent A."""
    cmd = [
        "./gradlew", "--no-daemon", "--quiet",
        ":multi-gammon-core:runMultiGammon",
        f"--args=-A {agent_a} -B {agent_b} -G {games} -T 0 --experiment-path experiments/006_long_training_leaky_relu"
    ]

    print(f"\nRunning {agent_a} vs {agent_b} ({games} games)...", end=" ", flush=True)

    try:
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)

        # Parse output to find the result
        for line in result.stdout.split('\n'):
            if 'Average' in line or 'points per game' in line:
                print(f"Found: {line.strip()}")
                return line.strip()

        # If not found in stdout, check stderr
        print("(checking stderr)", end=" ", flush=True)
        for line in result.stderr.split('\n'):
            if line.strip():
                print(line.strip())

        return "Result not found"

    except subprocess.CalledProcessError as e:
        print(f"FAILED: {e}")
        return None

def main():
    print("=" * 80)
    print("Agent Strength Benchmark")
    print("=" * 80)
    print("\nExpected ordering: Random < SimpleHeuristic < Heuristic")
    print("\nThis will take several minutes...\n")

    games = 10000

    # Test 1: SimpleHeuristic vs Random (should be positive, ~2-4 PPG)
    print("\n--- Test 1: SimpleHeuristic vs Random ---")
    result1 = run_match("SimpleHeuristic", "Random", games)

    # Test 2: Heuristic vs Random (should be positive, ~8-12 PPG)
    print("\n--- Test 2: Heuristic vs Random ---")
    result2 = run_match("Heuristic", "Random", games)

    # Test 3: Heuristic vs SimpleHeuristic (should be positive, ~4-8 PPG)
    print("\n--- Test 3: Heuristic vs SimpleHeuristic ---")
    result3 = run_match("Heuristic", "SimpleHeuristic", games)

    print("\n" + "=" * 80)
    print("Summary:")
    print("=" * 80)
    print(f"SimpleHeuristic vs Random:       {result1}")
    print(f"Heuristic vs Random:             {result2}")
    print(f"Heuristic vs SimpleHeuristic:    {result3}")
    print("\nIf all three are positive, strength ordering is confirmed: Random < SimpleHeuristic < Heuristic")
    print("=" * 80)

if __name__ == "__main__":
    main()
