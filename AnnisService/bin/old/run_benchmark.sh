#!/bin/sh

# CLASS=de.deutschdiachrondigital.dddquery.helper.Benchmark
CLASS=annisservice.AnnisServiceImpl

classpath="classes"
for lib in lib/*; do
	classpath="$classpath:$lib"
done

# java -cp $classpath $CLASS $*

date
rm log/benchmark.log
cp benchmark.log4j.properties classes/log4j.properties
java -cp $classpath de.deutschdiachrondigital.dddquery.helper.Benchmark $* > /dev/null
# ./report.py
date
