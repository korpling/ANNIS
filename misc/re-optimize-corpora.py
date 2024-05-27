#!/usr/bin/env python3

import subprocess
import argparse

parser = argparse.ArgumentParser(
                    prog='re-optimize-corpora',
                    description='Runs the ANNIS CLI to re-optimize a list of corpora')
parser.add_argument("corpus_list")
parser.add_argument("data_dir")

args = parser.parse_args()

print(args.data_dir)

#subprocess.run(["ls", "-l"])