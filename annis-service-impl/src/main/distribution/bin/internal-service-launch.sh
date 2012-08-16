#!/bin/bash
# execute service, redirect stdout to stderr in order not to disturb our output 
# (which should be only the echo $1) 
java -Dfile.encoding=UTF-8 -Dannis.home="$ANNIS_HOME" -cp "$classpath" $jvm_args annis.service.internal.AnnisServiceRunner $service_args <&- &
echo $!