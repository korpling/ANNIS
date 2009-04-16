#!/bin/sh

classpath="classes"
for lib in lib/*; do
	classpath="$classpath:$lib"
done

java -Dfile.encoding=UTF-8 -cp $classpath de.deutschdiachrondigital.dddquery.helper.Service $* 
