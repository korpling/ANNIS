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
Additionally, the adminstrator can also directly adjust the contents of the conf/
shiro.ini configuration file. This allows a much more individual configuration
and the usage of external authorization services like LDAP.

Configuration file location {#admin-configure-userfileloc}
---------------------------

There is a central location where the user configuration files are stored.
Configure the path to this location in the `conf/shiro.info` configuration file of
the ANNIS service. The default path is `/etc/annis/user_config_trunk/` and
must be changed at two locations in the configuration file.

\verbatim
[main]
annisRealm = annis.security.ANNISUserRealm
annisRealm.resourcePath=/etc/annis/user_config_trunk/
annisRealm.authenticationCachingEnabled = true
globalPermResolver = annis.security.ANNISRolePermissionResolver
globalPermResolver.resourcePath = /etc/annis/user_config_trunk/
\endverbatim

User and group files {#admin-configure-userformat}
--------------------

1. Create a file "groups" in the user-configuration directory (e.g. `/etc/annis/user_config_trunk/groups`):
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
given_name=userGivenName
surname=userSurname
\endverbatim
  - A superuser who has access to every corpus can be created with `groups=*`
  - `given_name` and `surname` can contain any string
  - The password must be hashed with SHA256 (one iteration and using a Salt) and formatted in the [Shiro1CryptFormat](http://shiro.apache.org/static/current/
apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html).

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

### anonymous group ### {#admin-configure-anonymous}

The special group `anonymous` is used for non logged-in users. Thus every corpus listed here is available for everyone without (and with) login.

Changing maximal context size {#admin-configure-contextsize}
=============================

The maximal context size of ±n tokens from each search result (for the KWIC
view, but also for other visualization) can be set for the ANNIS service in the file
`<service-home>/conf/annis-service.properties` Using the syntax, e.g. for a
maximum context of 10 tokens:
\verbatim
annis.max-context=10
\endverbatim
