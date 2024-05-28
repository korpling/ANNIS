#!/usr/bin/env python3

import subprocess
import argparse

parser = argparse.ArgumentParser(
                    prog='re-optimize-corpora',
                    description='Runs the ANNIS CLI to re-optimize a list of corpora')
parser.add_argument("corpus_list", help="Path to a CSV file containing all the corpora to re-optimize.")
parser.add_argument("data_dir", help="The graphANNIS data directory where the corpora are stored.")
parser.add_argument("--disk-based", action="store_true", help="If given, use the disk-mode to store the corpora.")
parser.add_argument("--annis", default="annis", help="Path to the ANNIS executable.")

args = parser.parse_args()

corpora = []
with open(args.corpus_list, "r", encoding="UTF-8") as f:
    corpora = f.read().splitlines()

for c in corpora:
    subprocess.run([args.annis, args.data_dir, "-c", "corpus " + c ,  "-c", "re-optimize"])