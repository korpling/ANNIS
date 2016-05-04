Upgrading an ANNIS installation {#admin-upgrade}
==========

[TOC]

These instructions are a guideline for upgrading the installation of ANNIS on a UNIX-like server. 
If you use the [local ANNIS Kickstarter version](@ref admin-install-kickstarter) 
just download the new version and re-initialize the database.
Please read [the installation instructions](@ref admin-install-server) first if you
haven't done so yet.


Automatic upgrade {#admin-upgrade-automatic}
=================

Upgrading the ANNIS service is more complex than deploying the user interface WAR file.
Therefore a Python script is available for an automatic upgrade. This script needs as least Python 3.2.

<!-- TODO: replace "develop" with "master" once released -->
1. Download the latest version of the script from GitHub: https://raw.githubusercontent.com/korpling/ANNIS/develop/Misc/upgrade_service.py
2. Download the new ANNIS release files (```annis-service-<VERSION>.tar.gz``` and ```annis-gui-<VERSION>.war```) 
3. Run the script \code{.sh}python3 upgrade_service.py --cleanup-data <installation-directory> annis-service-<VERSION>.tar.gz\endcode
   If the new release uses a new database schema the update might take some time. Thus it might be better to execute the script in the background:
   \code{.sh}
   nohup python3 upgrade_service.py --cleanup-data <installation-directory> annis-service-<VERSION>.tar.gz &
   tail -f nohup.out
   \endcode
4. If succesful undeploy the old WAR file and deploy the new one.


In case the upgrade script needed to update the database (it will tell you so), 
you should delete the old schema from your PostgreSQL database by running the 
following command in your PostgreSQL-Client:
\code{.sql}
DROP SCHEMA <oldschema>;
\endcode

\remarks To learn more about the (additional) parameters of the script run: \code{.sh}python3 upgrade_service.py --help\endcode

Manual upgrade {#admin-upgrade-manual}
==============

When Python is not available it is still possible to execute the steps
of the upgrade process manually.
The upgrade path described here tries to have a minimum downtime.


Upgrade for minor version updates {#admin-upgrade-minor}
---------------------------------

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
-------------

Whenever the first or second number of the version changes you have to re-import
the corpora into a newly initialized database.

### 1. Download
Download both the `annis-service-<VERSION>.tar.gz` and the `annis-gui-<VERSION>.war`
to a folder of your choice, e.g. `/tmp/`.

### 2. Install the new service

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

### 3. Copy old corpora

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

Additionally copy  the "user_config" and "url_shortener" tables from the
old installation, e.g. with the PostgreSQL `COPY` command (http://www.postgresql.org/docs/9.3/static/sql-copy.html#AEN69268)

### 4. Switch service

Stop the old service and start the
new service. Remember to set the `ANNIS_HOME` variable to the right value in
both cases before you call `annis-service.sh start/stop` command.

### 5. Upgrade front-end

Undeploy the old WAR file and deploy the new WAR file.

### 6. Cleanup

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
