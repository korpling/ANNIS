#!/bin/sh
# ****************************************************************************
# *** 		This script cleans the working instance of relANNIS DB			***
# ****************************************************************************

# creates new instances for relANNIS-kernel, ANNIS_VIZ
HOME=`pwd`'/'

DB_DB='relANNIS'
DB_USR='relANNIS_flo'
DB_HST='localhost'
DB_PWD='relANNIS'

FILE_CREATE_RELANNIS=$HOME'relANNIS_2_5_create.sql'
FILE_CREATE_ANNISVIZ=$HOME'create_ANNIS_VIZ.sql'
#FILE_CREATE_EXTDATA=$HOME'extData_create.sql'

FILE_LOG=$HOME'clean.log'

callPostgres()
{
	SQL_FILE=$1
	PGPASSWORD=$DB_PWD psql -d $DB_DB -U $DB_USR -h $DB_HST -f $SQL_FILE > $FILE_LOG
}

callPostgres $FILE_CREATE_RELANNIS
callPostgres $FILE_CREATE_ANNISVIZ

