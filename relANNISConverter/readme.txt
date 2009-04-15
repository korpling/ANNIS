***************************************************************************************************
***								Readme for relANNISConverter 1.0								***
***************************************************************************************************

The relANNISConverter 1.0 converts PAULA 1.0 data into relANNISex 1.0 (relANNIS exchange format)data.

0.Content:
========
1.Requirements
2.Installation
3.Running
4.Example
5.Troubleshooting

1.Requirements:
===============
- Postgres 8.2 or higher (http://www.postgresql.org/)
- JAVA 1.5 or higher (http://www.java.com/)
- ANT 1.7 or higher (http://ant.apache.org/)
- Sh-Interpreter (nativ supported in unix and linux systems or in Windows use e.g. Cygwin: www.cygwin.com/)

2.Installation:
===============
/1/	Unzip the folder relANNISConverter to any folder (e.g. named RACONVERTER_HOME)
/2/	Creating database-instance
	In this version relANNISConverter needs a database connection to compute the relANNISex data, therefore you have to install
	Postgres and add the POSTGRES_HOME to the PATH-variable.
	After you did so, you have two possibilities to make the db accessible to relANNISConverter: a simple one and a complex one.
	/1/	Simple way:
		-	run the the script in RACONVERTER_HOME/scripts/create_DBUser.sql under a postgres user who can create users and databases
			the syntax for calling the script can look like this: psql -U postgres -h localhost -f RACONVERTER_HOME/scripts/create_DBUser.sql
		-	After you did so, the script will create a user named relANNIS_flo with password relANNIS and a databsae named relANNIS
	/2/	Complex way (If you don´t want to use this default settings you can use your own)
		-	Create a database instance with your own name (for example ownDB)
		-	Create a database user with your own name (for example ownUser) and own password (e.g. ownPW). After that
			you have to give him all rights for 'ownDB'.
		So far so good. This was the simple part, let´s come to difficult part of this way. In this version of 
		relANNISConverter the DB_Connection is fix given in a lot of files, and you have to change them all. 
		This could take a while. The files you have to change are:
			- RACONVERTER_HOME/scripts/clean_db.sh (there you have to change the variables 'DB_DB', 'DB_USR', 'DB_PWD'
			- RACONVERTER_HOME/scripts/converter_resolver.sh (change the values for psql calling in line 74 till 85)
			- RACONVERTER_HOME/PAULAImporter/settings/dbSettings.xml (change the value entries of 'database', 'user', 'password')
/3/	Run	RACONVERTER_HOME/install.sh. This script should install all required program parts. You will find a log file in RACONVERTER_HOME/logs/install.log.
	There you can check the installation

/4/	You´re done and you can run your first converting.

3.Running:
==========
For running the converter call script 'RACONVERTER_HOME/convert.sh'. 
The relANNIS converter needs minimum two arguments and maximum three arguments. First is the source path (path of corpus which shall be imported), 
second destination path (path where the outputfiles shall be stored) and third (optional) a temprorary path (where the converter can store temprorary files). 
If no temprorary path is given the Converter uses the _TMP-folder in current directory.
!!! Please pay attention that all paths have to be absolute !!!

Synopsis:
---------
call convert.sh sourcefolder destinationfolder [temporary folder]

If something is wrong you can take a look in 'RACONVERTER_HOME/logs/convert.log', there you can find all messages created while converting.


4.Example:
==========
You can find a little example in RACONVERTER_HOME/example, there you find a folder named exData (the name of corpus root) with paula 1.0 files in it. 
You will also find a script named example_run.sh with following content, you can run it and take a look:

sh convert.sh pwd/example/exData pwd/example/dst pwd/example/TMP

This means, the converter will convert a corpus in source path and put its output (the relANNISex data) to dst-path. Temprorary files will be stored in path TMP. 
You do not have to name a TMP-folder, if you don´t, a default folder will be created. 

5.Troubleshooting:
==================
We´re sorry, but at this time, we do not have a good way of troubleshooting. 
/1/	If installation hasn´t worked, please read the section about installation and try again
/2/	If running does not work, please try running the example in section Example. 
	If this doesn´t work, there is something going wrong with installation. 
	Else read the paula 1.0 doku and all appendix and find out wether your corpus follows the standard. 
	
	For more information take a look into the log-file 'RACONVERTER_HOME/logs/convert.log'.
	
/3/	If everything doesn´t work please write an eMail to 'zipser@informatik.hu-berlin.de', describe the problem and attach all log-files from 'RACONVERTER_HOME/logs/'.
		
		