Configure back-end service {#admin-configure-service}
==========================

[TOC]

User Configuration {#admin-configure-user}
==================

ANNIS has an authentication system which allows to handle multiple users
which will see different corpora depending on which groups the user is part
of. Behind the scenes ANNIS uses the [Apache Shiro](http://shiro.apache.org/
) security framework. Per default ANNIS uses a file based authentication and
authorization approach where some configuration files with an ANNIS specific
layout are evaluated. This section will discuss how to manage this configuration.
Additionally, the administrator can also directly adjust the contents of the conf/
shiro.ini configuration file. This allows a much more individual configuration
and the usage of external authorization services like LDAP.

Configuration file location {#admin-configure-userfileloc}
---------------------------

There is a central location where the user configuration files are stored.
Configure the path to this location in the `conf/shiro.info` configuration file of
the ANNIS service. The default path is `/etc/annis/user_config/` and
can be changed in the configuration file.

\verbatim
[main]
confManager = annis.security.ANNISUserConfigurationManager
confManager.resourcePath=/etc/annis/user_config/
[...]
\endverbatim

User and group files {#admin-configure-userformat}
--------------------

1. Create a file "groups" in the user-configuration directory (e.g. `/etc/annis/user_config/groups`):
\verbatim
group1=pcc3,falko,tiger2
group2=pcc3
group3=tiger1
anonymous=pcc2,falko
\endverbatim
This example means that a member of group group1 will have access to
corpora with the names pcc3,falko, tiger2 (corpus names can be displayed
with the `annis-admin.sh list` command).
2. Create a subdirectory `users`
3. You have to create a file for each user inside the users subdirectory where
the user's name is *exactly* the file name (no file endings).
\verbatim
groups=group1,group3
password=$shiro1$SHA-256$1$tQNwUIxEQhrDn6FKcY1yNg==$Xq8ZCb3RFBwn3GfQ7pav3G3vHg4TKRGD1ItpfdW+JvI=
# these are optional entries
permissions=adm:*,query:*
expires=2015-04-25
\endverbatim
  - A superuser who has access to every corpus can be created with `groups=*`
  - The password must be hashed with SHA256 (one iteration and using a Salt) and formatted in the [Shiro1CryptFormat](http://shiro.apache.org/static/current/
apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html).
  - Additional permissions for the user are given as comma seperated list in the `permissions` field.
  - With `expires` you can define when an account will expire. The format must be in encoded according to the [ISO-8601 standard](http://en.wikipedia.org/wiki/ISO_8601).

  The easiest way to generate the passwort hash is to use the
Apache Shiro command line hasher (http://shiro.apache.org/command-line-hasher.html) which can be downloaded from http://shiro.apache.org/download.html#Download-1.2.1.BinaryDistribution .

  1. Execute `java -jar shiro-tools-hasher-1.2.1-cli.jar -i 1 -p` from the
command line (the jar-file must be in the working directory)
  2. Type the password
  3. Retype the password
  4. It will produce the following output:
  \code{.sh}
$ java -jar shiro-tools-hasher-1.2.1-cli.jar -i 1 -p
Password to hash: 
Password to hash (confirm): 
$shiro1$SHA-256$1$kRMX+Et6w7XJgwSEAgq9nw==$sQOgObXsQdO76wnNxvN0aesvTSPoBsd/2bjxasydB+I=
  \endcode
  The last line is what you have to insert into the password field.

### "anonymous" and "user" group ### {#admin-configure-anonymous}

The special group `anonymous` is used for non logged-in users. Thus every corpus listed here is available for everyone without (and with) login. In addition the group "user" is added to
every user that is logged in.

### Advanced permissions ### {#admin-configure-permissions}

The following permissions can be granted to individual users. Wildcards ("*") can be used
as described in the [Apache Shiro documentation](https://shiro.apache.org/permissions.html).

#### Administration ####

If you want to have an adminstrator user just add
\verbatim
admin:*
\endverbatim
to it's permissions. For more fine-grained control (e.g. for the web service users) you can specifiy the actual action. You can always use "*" wildcard instead of the corpus name to allow the specific action for any corpus.

permission               | description 
-------------------------|-------------
admin:import:<b>{corpusname}</b> | Allow to @ref annis.service.AdminService#importCorpus "import and overwrite" a corpus with a specific name.
admin:query-import:finished | Allow to @ref annis.service.AdminService#finishedImport "check if an import has finished".
admin:query-import:running | Allow to list the @ref annis.service.AdminService#currentImports "currently running imports".
admin:write:user | Allow to the @ref annis.service.AdminService#updateOrCreateUser "update or create" users.
admin:read:user | Allow get the information about a user (like groups or additionally permissions).
admin:write:adminuser | Additional permission to @ref annis.service.AdminService#updateOrCreateUser "update or create" users with adminstration rights (thus having an extra permission thats starts "admin:").


#### Querying ####

Every user that is part of a group that contains a corpus always get these permissions for a corpus automatically. If you want to allow a user to access only a certain functionality 
you can add more fine grained permissions. E.g.
\verbatim
query:count:*
\endverbatim
allows a user to count on all corpora but won't allow him to fetch the annotation graphs. For users that use the graphical user interface and not the service directly you should always grant a user all query permissions for a corpus. Otherwise he the user interface might not function as expected.

permission               | description 
-------------------------|-------------
query:show:<b>{corpusname}</b> | Allow to show information about a specific corpus.
query:count:<b>{corpusname}</b> | Allow to @ref annis.service.QueryService#count "count" on a specific corpus.
query:find:<b>{corpusname}</b>  | Allow to @ref annis.service.QueryService#find "find matches" on a specific corpus.
query:subgraph:<b>{corpusname}</b>  | Allow to query @ref annis.service.QueryService#subgraph "subgraphs" on a specific corpus.
query:subgraph:<b>{corpusname}</b>  | Allow to query @ref annis.service.QueryService#subgraph "annptated subgraphs" on a specific corpus
query:subgraph:<b>{corpusname}</b>  | Allow to query @ref annis.service.QueryService#subgraph "annotated subgraphs" and @ref annis.service.QueryService#graph "complete annotated graphs" on a specific corpus.
query:binary:<b>{corpusname}</b>  | Allow to get the  @ref annis.service.QueryService#binary "binary files" of a specific corpus.

Changing maximal context size {#admin-configure-contextsize}
=============================

The maximal context size of ±n tokens from each search result (for the KWIC
view, but also for other visualization) can be set for the ANNIS service in the file
`<service-home>/conf/annis-service.properties` Using the syntax, e.g. for a
maximum context of 10 tokens:
\verbatim
annis.max-context=10
\endverbatim
