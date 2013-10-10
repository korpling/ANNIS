PostgreSQL Server Configuration {#admin-configure-postgresql}
===============================

[TOC]

Performance tuning {#admin-configure-postgresperf}
==================

The default configuration of PostgreSQL uses system resource very sparingly. To improve the performance of the ANNIS service it is necessary to change a few settings in the PostgreSQL configuration file `postgresql.conf` as shown in the excerpt below.

Most of the options below are commented out in the `postgresql.conf` file. This means that PostgreSQL will use the default value, i.e. the value as it appears in the `postgresql.conf` file, for this option. To make your changes take effect you have to uncomment it.

The values below are for machine with 2 GB RAM that is exclusively dedicated to running PostgreSQL. If you're running ANNIS on your local machine and don't have large corpora, you should use lower values as explained in the comments.

Changes in `postgresql.conf`:

\verbatim
max_connections = 100        # expected maximum number of connections (users) at peak load

shared_buffers = 512MB      # RAM cache shared across all sessions
                            # 25% of available RAM
                            # use lower value on a desktop system, i.e. 128MB or 256MB

work_mem = 128MB            # RAM for *one* sort, aggregate or hash operation inside a query plan 
                            # RAM / (2 x max_connections)
                            # (many operations can run in parallel!)
                            # increase for large corpora, i.e. 256MB, 512MB
                            # decrease if you have many users

maintenance_work_mem = 256MB    # RAM for maintenance operations during corpus import
                                # CREATE INDEX, VACUUM etc.
                                # increase for large corpora

effective_cache_size = 1536MB   # estimated size of disk cached used by the OS
                                # 75% of available RAM
                                # use lower value on a desktop system, where other applications are running
                                # e.g. 512MB for a desktop with 2 GB RAM

default_statistcs_target = 100      # size of value histogram for each table column
                                    # use maximum value

checkpoint_segments = 20    # affects how quickly buffers are written
                            # to disk inside a transaction

autovacuum = off        # VACUUM is done automatically during corpus import
\endverbatim

More information on these settings can be found in the PostgreSQL manual:

- "18.4. Resource Consumption": http://www.postgresql.org/docs/9.2/interactive/runtime-config-resource.html
- "18.5. Write Ahead Log": http://www.postgresql.org/docs/9.2/interactive/runtime-config-wal.html
- "18.6. Query Planning": http://www.postgresql.org/docs/9.2/interactive/runtime-config-query.html 
- "28.4. WAL Configuration": http://www.postgresql.org/docs/9.2/interactive/wal-configuration.html
- "PostgreSQL Wiki": http://wiki.postgresql.org/wiki/SlowQueryQuestions

Logging {#admin-configure-logging}
=======

If you want to log the duration of SQL statements you should also set the following options in `postgresql.conf`:

\verbatim
log_min_duration_statement = 0
\endverbatim

Remote access {#admin-configure-remote}
=============

If the PostgreSQL server runs on a separate machine, remote access has to be enabled.

Changes in `postgresql.conf`:

\verbatim
listen_adresses = 'localhost,192.168.1.1'
\endverbatim
Where 192.168.1.1 is the IP address of the machine running PostgreSQL.

Changes in `pg_hba.conf`:

\verbatim
host annis_db annis_user 192.168.1.2/0 md5
\endverbatim
Where 192.168.1.2 is the machine running the ANNIS service that is connecting to the remote PostgreSQL server.

Configuration of System Resources {#admin-configure-res}
=================================

PostgreSQL needs to access large areas of continuous RAM which can easily exceed the maximum size allowed by the operating system. PostgreSQL will check the OS resource settings during startup and exit with an error if they are not adequate.

Reproduced below are the commands to change the resource settings on Linux and OS X. More information can be found in the PostgreSQL manual: [Managing Kernel Resources](http://www.postgresql.org/docs/8.3/interactive/kernel-resources.html 17.4.).

## Linux ##

\verbatim
sysctl -w kernel.shmmax=536870912   # bytes; corresponds to 512MB
\endverbatim
This command takes effect immediately. To make the change permanent across system reboots, add it to the file `/etc/sysctl.conf`.

## Mac OS X ##

\verbatim
sysctl -w kern.sysv.shmmax=536870912        # bytes; corresponds to 512MB
sysctl -w kern.sysv.shmall=131072           # measured in 4 kB pages
\endverbatim
OS X has to be rebooted for the command to take effect. To make the change permanent across system reboots, add it to the file `/etc/sysctl.conf`.


