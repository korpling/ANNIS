#!/bin/sh
# a very dirty script to rcompute the annis-viz
HOME_BULK='/home/annis/data/bulk/'
HOME_SCRIPT='/home/annis/programs/relANNISConverter/scripts/'
HOME_RECOMPUTE=$HOME_SCRIPT'recompute_dirty/'

FILE_CLEAR_ANNIS=$HOME_SCRIPT'relANNIS_2_5_create.sql'
FILE_LOG=$HOME_RECOMPUTE'recompute_dirty.log'
FILE_REFILL=$HOME_RECOMPUTE'copy.sql'
FILE_CLEAR_ANNIS_VIZ=$HOME_SCRIPT'create_ANNIS_VIZ.sql'
FILE_COMPUTE=$HOME_SCRIPT'compute_ANNIS_VIZ.sql'
FILE_EXPORT_VIZ=$HOME_RECOMPUTE'export_viz.sql'

ERR_TOO_MUCH_PARAMS="ERROR: There are too much parameters given, the script needs only the corpus name as parameter."
ERR_TOO_LESS_PARAMS="ERROR: There are too less parameters given, the script needs the corpus name as parameter."

#keine Parameter ?rgeben
if `test $# -lt 1`
	then echo $ERR_TOO_LESS_PARAMS
	exit
elif `test $# -gt 1`
	then echo $ERR_TOO_MUCH_PARAMS
	exit
fi
#im gesamten Corpus-Verzeichniss die Rechte ändern
PARAMS='777 '$HOME_BULK$1
chmod $PARAMS
PARAMS1='777 '$HOME_BULK'TMP/'
chmod $PARAMS1
# TMP-Dir leeren
DST='-f '$HOME_BULK'TMP/*.tab'
rm $DST
#relANNIS-BUlks in TMP kopieren
DST=$HOME_BULK'TMP/'
SRC=$HOME_BULK$1'/*.tab'
cp $SRC $DST 

#relANNIS-Daten reimportieren
echo 'reimport the corpus '$1'...'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_CLEAR_ANNIS > recompute_dirty.log
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_REFILL >> recompute_dirty.log

#visualisierung berechnen
echo 'create ANNIS_VIZ...'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_CLEAR_ANNIS_VIZ >> recompute_dirty.log
echo 'computing visualization of corpus '$1'...'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_COMPUTE >> recompute_dirty.log

# TMP-Dir leeren
DST='-f '$HOME_BULK'TMP/*.tab'
rm $DST

#in quellverzeichnis kopieren
echo 'exporting visualization...'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_EXPORT_VIZ >> recompute_dirty.log
echo 'copying computed files in corpus directory...'
DST=$HOME_BULK$1
SRC=$HOME_BULK'TMP/*.tab'
PARAMS2='-uf '$SRC' '$DST
mv $PARAMS2
