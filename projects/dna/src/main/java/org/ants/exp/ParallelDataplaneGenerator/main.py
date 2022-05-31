#!/usr/bin/env python3
import argparse
import multiprocessing as mp
from parallel_runner import ParallelRunner
from timer import Timer


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Run routing_cli in parallel')
    parser.add_argument('input_path', help='path of input relations', type=str)
    parser.add_argument('-p', '--processes', help='number of processes to be uesd, default to be available cpu/thread count', type=int, default=mp.cpu_count())
    parser.add_argument('-g', '--groups', help='number of groups to hold prefixes', type=int, default=mp.cpu_count())
    args = parser.parse_args()

    fibs = None
    with Timer('total time'):
        runner = ParallelRunner(args.input_path, args.processes, args.groups)
        runner.analyze_dep()

        with Timer('runtime'):
            runner.run()
        with Timer('update time'):
            runner.run_node_failure_update()

        runner.generate_output()
        runner.close()