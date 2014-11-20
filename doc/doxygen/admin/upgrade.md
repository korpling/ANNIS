Upgrading an ANNIS installation {#admin-upgrade}
==========

[TOC]



These instructions are a guideline for upgrading the installation of ANNIS on a UNIX-like server. 
If you use the [local ANNIS Kickstarter version](@ref admin-install-kickstarter) 
just download the new version and re-initialize the database.
Please read [the installation instructions](@ref admin-install-server) first if you
haven't done so yet.
The upgrade path described here tries to have a minimum downtime.

Upgrade for minor version updates {#admin-upgrade-minor}
===========

For minor version updates, e.g. from 3.1.0 to 3.1.1 (thus only
the last version number changes) you can use the database from the older version
without any modifications. Thus an upgrade only consists of the following steps

1. backup the old installation files
2. download the files of the new version
3. backup the `conf/database.properties` file of the ANNIS service
4. if you made any manual adjustments to the file `conf/annis-service.properties` also backup it 
5. stop the old ANNIS service
6. overwrite the files of the old ANNIS service installation with the new version
7. overwrite the `conf/database.properties` file with the backup
8. apply the changes you made to the `conf/annis-service.properties` on the new version (e.g. set the port number)
9. start the ANNIS service again
10. undeploy the old WAR file and deploy the new WAR file

Full upgrade {#admin-upgrade-full}
===========

Whenever the first or second number of the version changes you have to re-import
the corpora into a newly initialized database.

1. Download
-----------
Download both the `annis-service-<VERSION>.tar.gz` and the `annis-gui-<VERSION>.war`
to a folder of your choice, e.g. `/tmp/`.

2. Install the new service
--------------------------

Unzip the annis service to a new  directory (don't delete or stop the old service)
and install it. 
These steps are similiar to how to [install a new ANNIS service](@ref admin-install-server).
\code{.sh}
tar xvzf /tmp/annis-service-<VERSION>.tar.gz -C <new installation directory>
export ANNIS_HOME=<new installation directory>>
export PATH=$PATH:$ANNIS_HOME/bin
\endcode
If you made any manual changes to the `conf/annis-service.properties` file copy
the changes to the new installation.
Then initialize the the database for the new installation.
\code{.sh}
annis-admin.sh init -u <username> -d <dbname> -p <user password> --schema <new schema name>
\endcode
Please re-use the old database name, the user name and the password for the existing user (they can be found
in the `conf/database.properties` file of the old installation).
The parameter `--schema` allows you to define a new [PostgreSQL schema](http://www.postgresql.org/docs/9.1/static/ddl-schemas.html)
which will be used for the new installation. 
A good name could be something like "v32" if the version is 3.2.0.

3. Copy old corpora
-------------------
With the command
\code{.sh}
annis-admin.sh copy <old installation director>/conf/database.properties
\endcode
the existing corpora of the old installation will be imported. This
can take a long time, so if you use SSH you might want use `nohup` to make sure 
the process will continue to run in the background even if the connection is interrupted.
\code{.sh}
nohup annis-admin.sh copy <old installation director>/conf/database.properties &
tail -f nohup.out
\endcode
At the end a summary of all successfull and failed imports 
will be given. If there where any errors please try to import the corpus
manually. When all corpora are imported, proceed to the next step.

4. Switch service
-----------------

Stop the old service and start the
new service. Remember to set the `ANNIS_HOME` variable to the right value in
both cases before you call `annis-service.sh start/stop` command.

5. Upgrade front-end
--------------------

Undeploy the old WAR file and deploy the new WAR file.

6. Cleanup
----------

If everything works as expected you can delete the old installation files. You
should also remove the contents of the old database by deleting the schema from the
PostgreSQL client:
\code{.sql}
DROP SCHEMA <oldschema>;
\endcode

To delete all unused external data files execute
\code{.sh}
annis-admin.sh cleanup-data
\endcode
\warning This will delete all data files not known to the current instance of ANNIS.
If you have multiple parallel installations and did not use different values for
the `annis.external-data-path` variable in the `conf/annis-service.properties`
the data files of the other installations will be lost.
