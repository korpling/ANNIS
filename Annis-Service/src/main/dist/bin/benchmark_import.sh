#!/bin/sh

report="$HOME/Desktop/benchmark_import"

for corpus in "$@"; do
	annis-admin.sh init -d annis_test -u annis_test -p annis_test -h kenny -P postgres
	( time annis-admin.sh import "$corpus" ) 2>&1 | tee "$report/`basename "$corpus"`"
done

grep real $report/* | sed -e 's/real    / /' -e 's/\(.*\): \(.*\)m\(.*\)\....s/\2m \3s  \1/' -e 's/^0m //'