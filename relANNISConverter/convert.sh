#!/bin/sh
# ****************************************************************************
# *** 		This script converts PAULA 1.o data to relANNIS-data			***
# ****************************************************************************

############################
## globale Variablen
############################
HOME=$(pwd)'/'
HOME_SANALYZER=$HOME'PAULAStructureAnalyzer/'
HOME_ANALYZER=$HOME'PAULAAnalyzer/'
HOME_IMPORTER=$HOME'PAULAImporter/'
HOME_TMP=$HOME'_TMP'
HOME_SCRIPTS=$HOME'scripts/'
HOME_LOG=$HOME'logs/'

FILE_LOG=$HOME_LOG'convert.log'
FILE_CLEAN_ANNIS=$HOME_SCRIPTS'relANNIS_2_5_create.sql'
FILE_CONVERT_RESOLVER=`$HOME_SCRIPTS'converter_resolver.sh'`
#Fehlermeldungen
ERR_TOO_MUCH_PARAMS="ERROR: There are too much parameters given, the script needs only the corpus name as parameter."
ERR_TOO_LESS_PARAMS="ERROR: There are too less parameters given, the script needs the corpus name as parameter."
ERR_PATH_NOT_EXISTS="ERROR: The source path of corpus does not exists: "
ERR_SRC_NO_DIR="ERROR: The source path of corpus does not exists: "
ERR_DST_NO_DIR="ERROR: The destination path of corpus does not exists: "
ERR_TMP_NO_DIR="ERROR: The temprorary path of corpus does not exists: "

printHelp()
{
	echo ''
	echo 'The relANNIS converter needs minimum two arguments and maximum three arguments. First is the source path (path of corpus which should be imported), second destination path (path where the outputfiles should be stored) and third as optional a temprorary path (where the converter can store temprorary files). If no temprorary path is given the Converter uses the _TMP-folder in current directory.'
	echo '!!! Please pay attention that all pathes have to be absolute !!!'
	echo ''
	echo 'call convert.sh sourcefolder destinationfolder [temprory folder]'
}

#cleans the ANNIS-database
cleaningRelANNIS()
{
	PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_CLEAN_ANNIS > $FILE_LOG
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

callPAULAStructureAnalyzer()
{
	#cd $HOME_SANALYZER
	SRC_PATH=$1
	DST_PATH=$2
	FLAG1='-s '$SRC_PATH
	FLAG2='-d '$DST_PATH
	FLAGS=' '$FLAG1' '$FLAG2
	PARAMS='-classpath bin:lib/log4j-1.2.15.jar structureAnalyzer.PAULAStructAnalyzer '$FLAGS
	java $PARAMS>>$FILE_LOG
	cd $HOME
}

# führt das Tool PAULAAnalyzer aus
callPAULAAnalyzer()
{
	#cd $HOME_ANALYZER
	FLAG1='-f '$1
	FLAGS=' '$FLAG1
	java -classpath bin:lib/log4j-1.2.15.jar paulaAnalyzer.PAULAAnalyzer $FLAGS>> $FILE_LOG
	cd $HOME
}

# führt das Tool PAULAImporter aus
callPAULAImporter()
{
	#cd $HOME_IMPORTER
	SRC='-s '$1
	DST='-d '$2
	FLAGS=' '$SRC' '$DST
	PARAMS='-classpath bin:lib/log4j-1.2.15.jar:lib/postgresql-8.1-412.jdbc3.jar  main.PAULAImporter '$FLAGS
	java $PARAMS >> $FILE_LOG
	cd $HOME
}

echo '************************************************************'
echo '*** 		Welcome to relANNIS Converter		***'
echo '************************************************************'
# checking parameter
if `test $# -lt 2`
	then echo $ERR_TOO_LESS_PARAMS
	printHelp
	exit
elif `test $# -gt 3`
	then echo $ERR_TOO_MUCH_PARAMS
	printHelp
	exit
else
	if `test ! -d $1`
		then echo $ERR_SRC_NO_DIR$1
		printHelp
		exit
	fi
	if `test ! -d $2`
		then createDir $2
	fi
fi
SRC_DIR=$1
DST_DIR=$2

#TEMP-Folder erstellen
if `test $# -eq 3`
	then TMP_DIR=$3		
	else TMP_DIR=$HOME_TMP
fi
createDir $TMP_DIR
createDir $HOME_LOG
#Rechte der zu beschreibenden Ordner ändern
PARAMS=' 777 '$TMP_DIR 
chmod$PARAMS
PARAMS=' 777 '$DST_DIR
chmod$PARAMS

echo 'SRC: '$SRC_DIR
echo 'DST: '$DST_DIR
echo 'TMP: '$TMP_DIR
echo 'cleaning temprorary database...'
cleaningRelANNIS
echo 'analyzing corpus structure...'
callPAULAStructureAnalyzer $SRC_DIR $TMP_DIR
echo 'analyzing corpus...'
callPAULAAnalyzer $TMP_DIR
echo 'converting corpus...'
callPAULAImporter $TMP_DIR $DST_DIR
PARAMS=$HOME_TMP' -R'
#rm $PARAMS
echo 'creating visualization corpus...'
echo `$HOME_SCRIPTS'converter_resolver.sh' $DST_DIR`
echo '************************************************************'
