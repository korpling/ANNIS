Installing {#admin-install}
==========

[TOC]


Installing an ANNIS public server {#admin-install-server}
================================


The ANNIS server version can be installed on UNIX based server, or else under
Windows using Cygwin [http://www.cygwin.com/], the freely available UNIX
emulator. To install the ANNIS server:

1. Download and install PostgreSQL 9.3 or a newer version for your operating system from 
http://www.postgresql.org/download/ and **make a note of the administrator
password** you set during the installation.
\note Under Linux, you might have to set the PostgreSQL password
manually. E.g. on Ubuntu you can achieve this with by running
the following commands:
\code{.sh}
sudo -u postgres psql
\password
\q
\endcode

2. Make sure you have **installed [JDK 7 or a later version](http://java.sun.com/javase/downloads/index.jsp)**
3. **Install a Java Servlet Container** ("Java web server") such as [Tomcat](http://tomcat.apache.org/) or [Jetty](http://www.mortbay.org/jetty/)
 (or install them if you don’t)
4. **Download** the ANNIS service distribution file `annis-service-<version>-distribution.tar.gz` from our website
and then **unzip** the downloaded file:
\code{.sh}
tar xzvf annis-service-<version>-distribution.tar.gz -C <installation directory>
\endcode
5. Set the **environment variables** (each time when starting up)
\code{.sh}
export ANNIS_HOME=<installation directory>
export PATH=$PATH:$ANNIS_HOME/bin
\endcode
6. Next **initialize** your ANNIS database (only the first time you use the system).
When the ANNIS service is normally installed, it assumes it can get PostgreSQL super user rights for this step. Thus you need the superuser password.
\code{.sh}
annis-admin.sh init -u <username> -d <dbname> --schema <schema> -p <new user password> -P <postgres superuser password>
\endcode
This call will 
<ul><li>create a new database with the name given by the "-d" parameter</li>
<ul><li>create a [PostgreSQL schema](http://www.postgresql.org/docs/9.1/static/ddl-schemas.html), you should use a schema name the corresponds to the ANNIS version to make an upgrade easier (e.g. you can name it "v31" for ANNIS 3.1.0)</li>
<li>create a new PostgreSQL user with the user name given by the "-u" parameter and the password given by the "-p" parameter</li>
<li>create all necessary tables, PSQL functions and initial data in the database</li></ul>
If you want to have parallel installations of ANNIS inside the same database you
can use the `--schema` parameter to define a [PostgreSQL schema](http://www.postgresql.org/docs/9.1/static/ddl-schemas.html)
other than the default `public` schema.
You can also omit the PostgreSQL administrator password option (`-P`) if you don't have
access to the administrator password. In this case the database and the user must already
exist. To manually create a user and a database the PostgreSQL adminstrator should execute the following:
\code{.sql}
CREATE LANGUAGE plpgsql; -- ignore the error if the language is already installed
CREATE USER myuser PASSWORD 'mypassword';
CREATE DATABASE mydb OWNER myuser ENCODING 'UTF8';
\endcode
\warning Do not use "postgres" as database name for ANNIS since it is reserved by PostgresSQL itself.

7. Have a look in the file `conf/annis-service.properties` and check if you need to change
any of the configuration variables.

8. Now you can **import** some corpora:
\code{.sh}
annis-admin.sh import path/to/corpus1 path/to/corpus2 ...
\endcode
\warning
The above import-command calls other PostgreSQL database
commands. If you abort the import script with Ctrl+C, these
SQL processes will not be automatically terminated; instead they
might keep hanging and prevent access to the database. The
same might happen if you close your shell before the import
script terminates, so you will want to prefix it with the "nohup"-
command.

9. Now you are ready to **start** the ANNIS service:
\code{.sh}
annis-service.sh start
\endcode
If you don't want to use any authentification and every user should see every
corpus without login you can start ANNIS with
\code{.sh}
annis-service-no-security.sh start
\endcode
instead of the default script.
10. To get the ANNIS **front-end** running, first download annis-
gui-<version>.war from our website and deploy it to your Java servlet
container (this is depending on the servlet container you use).
11. Configure users and groups as described [here](@ref admin-configure-user) 
and define who is allowed to see which corpus.

\note
We also **strongly recommend** reconfiguring the Postgres server’s default
settings as described [here](@ref admin-configure-postgresql).

Installing a local version (ANNIS Kickstarter)     {#admin-install-kickstarter}
==============================================

Local users who do not wish to make their corpora available online can install
ANNIS Kickstarter under most versions of Linux, Windows and Mac OS. To install
Kickstarter follow these steps:

1. Download and install PostgreSQL 9.2 for your operating system from 
http://www.postgresql.org/download/ and **make a note of the administrator
password** you set during the installation.
\note Under Linux, you might have to set the PostgreSQL password
manually. E.g. on Ubuntu you can achieve this with by running
the following commands:
\code{.sh}
sudo -u postgres psql
\password
\q
\endcode

After installation, PostgreSQL may automatically launch the PostgreSQL
Stack Builder to download additional components – you can safely skip this
step and cancel the Stack Builder if you wish. You may need to restart your
OS if the Postgres installer tells you to.
2. Download and the ANNIS Kickstarter ZIP-file for your version from the ANNIS website.
3. Start AnnisKickstarter.bat if you’re using Windows, AnnisKickstarter.cmd on Mac or run the bash script
 AnnisKickstarter.sh otherwise (this may take a few seconds the first time you
run Kickstarter). At this point your Firewall may try to block Kickstarter and
offer you to unblock it – do so and Kickstarter should start up.
\note For most users it is a good idea to give Java more memory (if this
is not already the default). You can do this by editing the script
AnnisKickstarter and typing the following after the call to start
java (before -splash:splashscreen.gif):
\code
-Xss1024k -Xmx1024m
\endcode
(To accelerate searches it is also possible to give the Postgres
database more memory, see the link in the next section below).
4. Once the program has started, if this is the first time you run Kickstarter,
press “Init Database” and supply your PostGres administrator password
from step 1.
5. Download and unzip the [pcc2 demo corpus](http://korpling.german.hu-berlin.de/~annis/downloads/sample_corpora/pcc2_relAnnis.zip) from the
ANNIS website.
6. Press “Import Corpus” and navigate to the directory containing the directory pcc2_v2_relAnnis/. Select this directory (but do not go into it) and press OK.
7. Once import is complete, press "Launch Annis frontend" and test the corpus (try selecting the pcc2
corpus, typing pos="NN" in the AnnisQL box and clicking “Show Result”. See
the section “Running Queries in ANNIS” in this guide for some more example
queries, or press the Tutorial button at the top left of the interface).

