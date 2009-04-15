#!/bin/sh
# ****************************************************************************
# *** 	This script reimports a bulk directory into a relANNIS-instance			***
# ****************************************************************************

# creates new instances for relANNIS-kernel, ANNIS_VIZ
HOME=`pwd`'/'

DB_DB='relANNIS'
DB_USR='relANNIS_flo'
DB_HST='localhost'
DB_PWD='relANNIS'

FILE_CLEAN_DB=$HOME'clean_db.sh'

FILE_LOG=$HOME'reimport.log'

callPostgres()
{
	SQL_FILE=$1
	PGPASSWORD=$DB_PWD psql -d $DB_DB -U $DB_USR -h $DB_HST -f $SQL_FILE > $FILE_LOG
}

#bisherige Datenbank bereinigen
echo `sh $FILE_CLEAN_DB` >$FILE_LOG
echo '\n\n!!!This script has to be completed!!!'
