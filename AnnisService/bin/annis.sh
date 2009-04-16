#!/bin/sh

if [ -z "$ANNIS_HOME" ]; then
	echo Please set the environment variable ANNIS_HOME to the Annis distribution directory.
	exit
fi

# build classpath
classpath=`$ANNIS_HOME/bin/classpath.sh`

# class with Java entry point
class=annis.AnnisRunner

java -cp $classpath -Dannis.home=$ANNIS_HOME $class "$@"