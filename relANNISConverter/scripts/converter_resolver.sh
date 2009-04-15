#!/bin/sh

# This script computes the entries for ANNIS-Visualization. Therefor it uses sql-scripts which reads
# the entries of relANNIS and computes visualization types. After computing it exports bulk-files to the
# given destination folder

############################
## globale Variablen
############################
HOME=`pwd`'/scripts/'
HOME_TMP=$HOME'_TMP/'
FILE_CREATE_VIZ=$HOME'create_ANNIS_VIZ.sql'
FILE_COMPUTE_VIZ=$HOME'compute_ANNIS_VIZ.sql'
FILE_EXPORT_END='export_resolver.sql'
FILE_LOG=$HOME'../logs/convert.log'

ERR_TOO_MUCH_PARAMS="ERROR: There are too much parameters given, the script needs only the corpus name as parameter."
ERR_TOO_LESS_PARAMS="ERROR: There are too less parameters given, the script needs the corpus name as parameter."

############################
## Funktionen
############################
printHelp()
{
	echo ''
	echo 'The relANNIS converter for resolver computes the Resolver data from data in database and exports them to the given directory.\n'
	echo ' '
	echo '!!! Please pay attention, the destination folder has to be absolut !!!\n'
	echo ' '
	echo 'call convert_resolver.sh destinationfolder\n'
}

#erzeugt das übergebene Verzeichniss, falls dieses nicht existiert
createDir()
{
	NEW_DIR=$1
	if `test ! -d $NEW_DIR`
		then
			mkdir $NEW_DIR
			chmod 777 $NEW_DIR
	fi
}

#erzeugt die export datei für die datenbank in sql
createExportFile()
{
	EXP_DIR=$1
	DST_DIR=$2
	echo "COPY corp_2_viz TO E'"$DST_DIR"corp_2_viz.tab' USING DELIMITERS E'\\\\t' WITH NULL AS 'NULL';" >$EXP_DIR
	echo "COPY xcorp_2_viz TO E'"$DST_DIR"xcorp_2_viz.tab' USING DELIMITERS E'\\\\t' WITH NULL AS 'NULL';">>$EXP_DIR 
	echo "COPY viz_type TO E'"$DST_DIR"viz_type.tab' USING DELIMITERS E'\\\\t' WITH NULL AS 'NULL';">>$EXP_DIR 
	echo "COPY viz_errors TO E'"$DST_DIR"viz_errors.tab' USING DELIMITERS E'\\\\t' WITH NULL AS 'NULL';">>$EXP_DIR 
}
############################
## Hauptprogramm
############################
#keine Parameter übergeben
if `test $# -lt 1`
	then 
		echo $ERR_TOO_LESS_PARAMS
		printHelp
		exit
elif `test $# -gt 1`
	then 
		echo $ERR_TOO_MUCH_PARAMS
		printHelp
		exit
fi
#Verbesserungen: Datenbankverbindung über geben, logFile-Destination übergeben

DST_DIR=$1'/'
#Zielverzeichnis erzeugen, wenn es nicht existiert
createDir $DST_DIR
echo 'cleaning database for computing resolver data...\n'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_CREATE_VIZ >> $FILE_LOG
echo 'computing resolver data...\n'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_COMPUTE_VIZ >> $FILE_LOG
echo 'exporting resolver data to '$DST_DIR'...\n'
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_EXPORT >> $FILE_LOG
#'COPY corp_2_viz TO E'/home/annis/data/bulk/TMP/corp_2_viz.tab' USING DELIMITERS E'\t' WITH NULL AS 'NULL';
createDir $HOME_TMP
chmod 777 $HOME_TMP
FILE_EXP=$HOME_TMP$FILE_EXPORT_END
createExportFile $FILE_EXP $DST_DIR
PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_EXP >> $FILE_LOG
PARAMS=$HOME_TMP' -R'
rm $PARAMS