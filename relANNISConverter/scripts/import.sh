#!/bin/sh
############################
## globale Variablen
############################
HOME_PATH='/home/annis/programs/relANNISImporter/'
# home of tool PAULAStructureAnalyzer
PS_HOME=$HOME_PATH'PAULAStructureAnalyzer/'
# home of tool PAULAAnalyzer
PA_HOME=$HOME_PATH'PAULAAnalyzer/'
# home of tool PAULAImporter
PI_HOME=$HOME_PATH'PAULAImporter/'
#home des Bereinigungsscriptes der DB relANNIS
HOME_CLEANING=$PI_HOME'scripts/'
#home des Füllskripts für den ANNIS-Resolver
HOME_RESOLVER='/home/annis/programs/Resolver/scripts/'
#Home des Exportscripts
HOME_EXPORT=$HOME_PATH'relANNISExport/'

#DB-Bereinigungsscript
FILE_CLEANING=$HOME_CLEANING'relANNIS_2_5_create.sql'
FILE_LOG=$HOME_PATH'import.log'
FILE_RESOLVER=$HOME_RESOLVER'fill_ANNIS_VIZ.sql'
FILE_EXPORT=$HOME_EXPORT'relANNISExport.sh'
#Aufruf um PSQL zu starten und sich bei relANNIS anzumelden
#... siehe Funktion cleaningRelANNIS

#Programsemantik
HOME_D='/home/annis/data/'
# Pfad in den die entpackten Korpora kommen
HOME_DATA=$HOME_D'data/'
# Pfad in den die Envelopes erstellt werden sollen
HOME_ENV=$HOME_D'/ENVs/'

#Fehlermeldungen
ERR_TOO_MUCH_PARAMS="ERROR: There are too much parameters given, the script needs only the corpus name as parameter."
ERR_TOO_LESS_PARAMS="ERROR: There are too less parameters given, the script needs the corpus name as parameter."
ERR_PATH_NOT_EXISTS="ERROR: The source path of corpus does not exists: "

############################
## Funktionen
############################

showInfo()
{
	echo "-----------------------------------------------"
	echo "|    Import of PUALA data into relANNIS	    |"
	echo "|    	 Humboldt university of Berlin   		|"
	echo "-----------------------------------------------"
	echo ""
}

cleaningRelANNIS()
{
	cd $PS_HOME
	echo 'cleaning the db relANNIS......'
	PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_CLEANING > $FILE_LOG
}

callPAULAStructureAnalyzer()
{
	cd $PS_HOME
	echo 'analyzing of file structure...'
	SRC_PATH=$1
	DST_PATH=$2
	FLAG1='-s '$SRC_PATH
	FLAG2='-d '$DST_PATH
	FLAGS=' '$FLAG1' '$FLAG2
	java -classpath bin:lib/log4j-1.2.15.jar structureAnalyzer.PAULAStructAnalyzer $FLAGS >> $FILE_LOG
}

# führt das Tool PAULAAnalyzer aus
callPAULAAnalyzer()
{
	cd $PA_HOME
	FLAG1='-f '$1
	echo 'analyzing of paula files......'
	FLAGS=' '$FLAG1
	java -classpath bin:lib/log4j-1.2.15.jar paulaAnalyzer.PAULAAnalyzer $FLAGS >> $FILE_LOG
}

# führt das Tool PAULAImporter aus
callPAULAImporter()
{
	cd $PI_HOME
	echo 'importing paula files.........'
	FLAG1='-s '$1
	FLAGS=' '$FLAG1
	java -classpath bin:lib/log4j-1.2.15.jar:lib/postgresql-8.1-412.jdbc3.jar  main.PAULAImporter $FLAG1 >> $FILE_LOG
}

fillANNISResolver()
{
	cd $PS_HOME
	echo 'filling the ANNIS-Resolver....'
	PGPASSWORD="relANNIS" psql -d relANNIS -U relANNIS_flo -h localhost -f $FILE_RESOLVER >> $FILE_LOG
}

exportData()
{
	cd $PS_HOME
	echo 'exporting data from relANNIS...'
	sh $FILE_EXPORT $1
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

############################
## Hauptprogramm
############################
showInfo
#keine Parameter übergeben
if `test $# -lt 1`
	then echo $ERR_TOO_LESS_PARAMS
	exit -1
elif `test $# -gt 1`
	then echo $ERR_TOO_MUCH_PARAMS
	exit -1
fi

#prüfen ob die notwendigen Verzeichnisse existieren
createDir $HOME_DATA
createDir $HOME_ENV

PATH_ENV=$HOME_ENV
PATH_DATA=$HOME_DATA$1
	
#prüfen ob Quellverzeichniss vorhanden
if `test -d $PATH_DATA`
	then
		#Datenbank bereinigen
		cleaningRelANNIS
		#PAULA Strukur analysieren
		callPAULAStructureAnalyzer $PATH_DATA $PATH_ENV
		PATH_ENV=$PATH_ENV'ENV_'$1
		#PAULA-Dateien analysieren
		callPAULAAnalyzer $PATH_ENV		
		#eigentlichen Importvorgang starten
		callPAULAImporter $PATH_ENV
		#Resolver mit Daten füllen
		fillANNISResolver
		#Daten exportieren
		exportData $1
		cd $HOME_PATH
else
		echo $ERR_PATH_NOT_EXISTS $SOURCE_PATH
fi