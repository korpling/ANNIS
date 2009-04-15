#!/bin/sh
# ***************************************************************************
# ***					Beispiel Konvertierung von Korpora					***
# ***************************************************************************
FILE_CONVERT=`pwd`'/convert.sh'
PARAM_1=`pwd`'/example/exData'
PARAM_2=`pwd`'/example/dst'
PARAM_3=`pwd`'/example/_TMP'
# echo `sh $FILE_CONVERT' '$PARAM_1' '$PARAM_2' '$PARAM_3`
#echo `sh $FILE_CONVERT $PARAM_1 $PARAM_2 $PARAM_3`
echo '\n'
echo ' Converting a sample corpus...................................................................START'
sh convert.sh $PARAM_1 $PARAM_2 $PARAM_3
echo ' Converting a sample corpus...................................................................END'
echo '\n'
