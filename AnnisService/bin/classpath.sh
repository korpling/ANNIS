#!/bin/sh

# build classpath
for lib in $ANNIS_HOME/lib/*.jar; do
	classpath="$classpath:$lib"
done

echo $classpath