#!/bin/sh

classpath="classes"
for lib in lib/*; do
	classpath="$classpath:$lib"
done

java -cp $classpath de.deutschdiachrondigital.dddquery.helper.Shell $*
