#!/usr/bin/env python3
import subprocess
import sys
import os
import argparse
from typing import Dict, List

def run_experiment(run_id: str, params: Dict[str, float], fixed_args: List[str], script_dir: str) -> bool:
    """
    Run a single experiment with the given parameters.

    Args:
        run_id: Identifier for the run (e.g., 'A', 'B', 'C', etc.)
        params: Dictionary containing alpha, lambda, gamma values
        fixed_args: List of fixed command line arguments
        script_dir: Directory where the script is located

    Returns:
        True if successful, False otherwise
    """
    # Construct the variable arguments
    variable_args = [
        "--alpha", str(params['alpha']),
        "--lambda", str(params['lambda']),
        "--gamma", str(params['gamma']),
        "--experiment-tag", f"run_{run_id}"
    ]

    # Combine fixed and variable arguments
    all_args = fixed_args + variable_args

    # Construct the gradle command
    gradle_cmd = ["./gradlew", "--no-daemon", ":multi-gammon-core:runMultiGammon", "--args=" + " ".join(all_args)]

    print(f"Running experiment {run_id} with parameters:")
    print(f"  alpha={params['alpha']}, lambda={params['lambda']}, gamma={params['gamma']}")
    print(f"  Command: {' '.join(gradle_cmd)}")
    print(f"  Working directory: {script_dir}")
    print("-" * 80)

    try:
        # Run the command from the script's directory
        subprocess.run(gradle_cmd, check=True, cwd=script_dir, capture_output=False)
        print(f"✓ Experiment {run_id} completed successfully")
        return True
    except subprocess.CalledProcessError as e:
        print(f"✗ Experiment {run_id} failed with return code {e.returncode}")
        return False
    except Exception as e:
        print(f"✗ Experiment {run_id} failed with error: {e}")
        return False

def parse_arguments():
    """Parse command line arguments."""
    parser = argparse.ArgumentParser(
        description="Run MultiGammon experiments with different parameters",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python run_experiments.py                   # Run all experiments
  python run_experiments.py --run A           # Run only experiment A
  python run_experiments.py --run A B C       # Run experiments A, B, and C
  python run_experiments.py --list            # List available experiments
        """
    )

    parser.add_argument(
        '--run',
        nargs='*',
        help='Specific experiment(s) to run. If not specified, all experiments will be run.'
    )

    parser.add_argument(
        '--list',
        action='store_true',
        help='List all available experiments and exit'
    )

    return parser.parse_args()

def list_experiments(experiments: Dict[str, Dict[str, float]]):
    """Display available experiments."""
    print("Available experiments:")
    print("=" * 80)
    print("| Run | alpha | lambda | gamma | Description")
    print("|-----|-------|--------|-------|-------------")
    for run_id, params in experiments.items():
        desc = "baseline" if run_id == "0" else f"variation {run_id}"
        print(f"| {run_id:3} | {params['alpha']:5.3f} | {params['lambda']:6.1f} | {params['gamma']:5.2f} | {desc}")
    print("=" * 80)

def main():
    """Main function to run all experiments."""

    # Parse command line arguments
    args = parse_arguments()

    # Get the directory where this script is located
    script_dir = os.path.dirname(os.path.abspath(__file__))

    # Fixed command line parameters
    fixed_args = [
        "-A", "RawTd40",
        "-B", "Heuristic",
        "-G", "1000",
        "-T", "400000",
        "-P", "20000",
        "--experiment-path", "experiments/004_lower_alpha2",
    ]

    # Variable parameters table
    experiments = {
        "N": {"alpha": 0.006, "lambda": 0.7, "gamma": 0.99},
        "O": {"alpha": 0.004, "lambda": 0.7, "gamma": 0.99},
        "P": {"alpha": 0.005, "lambda": 0.5, "gamma": 0.95},
        "Q": {"alpha": 0.006, "lambda": 0.6, "gamma": 1.00}
    }

    # Handle --list option
    if args.list:
        list_experiments(experiments)
        return

    # Check if gradlew exists in the script's directory
    gradlew_path = os.path.join(script_dir, "gradlew")
    if not os.path.exists(gradlew_path):
        print(f"Error: gradlew not found in script directory: {script_dir}")
        print("Please ensure the script is in the project root directory.")
        sys.exit(1)

    # Determine which experiments to run
    if args.run is not None:
        # Validate specified runs
        invalid_runs = [run_id for run_id in args.run if run_id not in experiments]
        if invalid_runs:
            print(f"Error: Invalid experiment ID(s): {invalid_runs}")
            print(f"Available experiments: {list(experiments.keys())}")
            sys.exit(1)

        experiments_to_run = {run_id: experiments[run_id] for run_id in args.run}
        print(f"Running specific experiments: {args.run}")
    else:
        experiments_to_run = experiments
        print("Running all experiments")

    # Display experiment summary
    print(f"\nScript directory: {script_dir}")
    print("\nExperiment Summary:")
    print("=" * 80)
    print("| Run | alpha | lambda | gamma |")
    print("|-----|-------|--------|-------|")
    for run_id, params in experiments_to_run.items():
        print(f"| {run_id:3} | {params['alpha']:5.3f} | {params['lambda']:6.1f} | {params['gamma']:5.2f} |")
    print("=" * 80)
    print()

    # Run experiments
    successful_runs = []
    failed_runs = []

    for run_id, params in experiments_to_run.items():
        print(f"\nStarting experiment {run_id}...")
        if run_experiment(run_id, params, fixed_args, script_dir):
            successful_runs.append(run_id)
        else:
            failed_runs.append(run_id)
        print()

    # Summary
    print("=" * 80)
    print("EXPERIMENT SUMMARY")
    print("=" * 80)
    print(f"Total experiments: {len(experiments_to_run)}")
    print(f"Successful: {len(successful_runs)} {successful_runs}")
    print(f"Failed: {len(failed_runs)} {failed_runs}")

    if failed_runs:
        sys.exit(1)
    else:
        print("All experiments completed successfully!")

if __name__ == "__main__":
    main()
