#!/bin/sh

if [ -z "$ANNIS_HOME" ]; then
	echo Please set the environment variable ANNIS_HOME to the Annis distribution directory.
	exit 2
fi

# build classpath
classpath=`$ANNIS_HOME/bin/classpath.sh`

# class with Java entry point
class=annis.AnnisRunner
# additionally options for java, e.g. "-Xmx512M"
JAVAOPTS=""

java $JAVAOPTS -cp $classpath -Dfile.encoding="utf-8" -Dannis.home=$ANNIS_HOME $class "$@"
