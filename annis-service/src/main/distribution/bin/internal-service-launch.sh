#!/bin/bash
# IMPORANT: our service is only allowed to output messages on stderr and 
# not stdout in order not to disturb our output 
# (which should be only the echo $!) 
java -Dfile.encoding=UTF-8 -Dannis.home="$ANNIS_HOME" -Dannis.nosecurity="$ANNIS_NOSECURITY" -cp "$classpath" $jvm_args annis.service.internal.AnnisServiceRunner $service_args <&- &
echo $!
