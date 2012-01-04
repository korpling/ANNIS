#!/bin/bash
java -Dfile.encoding=UTF-8 -Dannis.home="$ANNIS_HOME" -cp "$classpath" $jvm_args annis.service.internal.AnnisServiceRunner $service_args <&- &
echo $!