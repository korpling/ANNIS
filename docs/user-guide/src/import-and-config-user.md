# User Configuration

ANNIS has an authentication system which allows to handle multiple users
which will see different corpora depending on which groups the user is part
of. Behind the scenes ANNIS uses the [Apache Shiro](http://shiro.apache.org/
) security framework. Per default ANNIS uses a file based authentication and
authorization approach where some configuration files with an ANNIS specific
layout are evaluated. This section will discuss how to manage this configuration.
Additionally, the administrator can also directly adjust the contents of the conf/
shiro.ini configuration file. This allows a much more individual configuration
and the usage of external authorization services like LDAP.

## Configuration file location


There is a central location where the user configuration files are stored.
Configure the path to this location in the `conf/shiro.info` configuration file of
the ANNIS back-end service. The default path is `/etc/annis/user_config/` and
can be changed in the back-end configuration file.

~~~ini
[main]
confManager = annis.security.ANNISUserConfigurationManager
confManager.resourcePath=/etc/annis/user_config/
~~~

## User and group files

1. Create a file "groups" in the user-configuration directory (e.g. `/etc/annis/user_config/groups`)
~~~ini
group1=pcc3,falko,tiger2
group2=pcc3
group3=tiger1
anonymous=pcc2,falko
~~~
This example means that a member of group group1 will have access to
corpora with the names pcc3,falko, tiger2 (corpus names can be displayed
with the `annis-admin.sh list` command).
2. Create a subdirectory `users`
3. You have to create a file for each user inside the users subdirectory where
the user's name is *exactly* the file name (no file endings).
~~~ini
groups=group1,group3
password=$shiro1$SHA-256$1$tQNwUIxEQhrDn6FKcY1yNg==$Xq8ZCb3RFBwn3GfQ7pav3G3vHg4TKRGD1ItpfdW+JvI=
# these are optional entries
permissions=adm:*,query:*
expires=2015-04-25
~~~
***Notes:***
  - A superuser who has access to every corpus can be created with `groups=*`
  - The password must be hashed with SHA256 (one iteration and using a Salt) and formatted in the [Shiro1CryptFormat](http://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html).
  - Additional permissions for the user are given as comma seperated list in the `permissions` field.
  - With `expires` you can define when an account will expire. The format must be in encoded according to the [ISO-8601 standard](http://en.wikipedia.org/wiki/ISO_8601).

The easiest way to generate the passwort hash is to use the
[Apache Shiro command line hasher](http://shiro.apache.org/command-line-hasher.html) which can be downloaded from 
[here](http://shiro.apache.org/download.html#Download-1.2.1.BinaryDistribution).

  1. Execute `java -jar shiro-tools-hasher-1.2.1-cli.jar -i 1 -p` from the
command line (the jar-file must be in the working directory)
  2. Type the password
  3. Retype the password
  4. It will produce the following output:
~~~bash
$ java -jar shiro-tools-hasher-1.2.1-cli.jar -i 1 -p
Password to hash: 
Password to hash (confirm): 
$shiro1$SHA-256$1$kRMX+Et6w7XJgwSEAgq9nw==$sQOgObXsQdO76wnNxvN0aesvTSPoBsd/2bjxasydB+I=
~~~
The last line is what you have to insert into the password field.

### "anonymous" and "user" group

The special group `anonymous` is used for non logged-in users. Thus every corpus listed here is available for everyone without (and with) login. In addition the group "user" is added to
every user that is logged in.

### Advanced permissions

The following permissions can be granted to individual users. Wildcards ("*") can be used
as described in the [Apache Shiro documentation](https://shiro.apache.org/permissions.html).

#### Administration

If you want to have an administrator user just add
~~~
admin:*
~~~
to it's permissions. For more fine-grained control (e.g. for the web service users) you can specify the actual action. You can always use "*" wildcard instead of the corpus name to allow the specific action for any corpus.

| permission                    | description                                                                                                                        |
| ----------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| admin:import:**{corpusname}** | Allow to import and overwrite a corpus with a specific name.                                                                       |
| admin:query-import:finished   | Allow to check if an import has finished.                                                                                          |
| admin:query-import:running    | Allow to list the currently running imports.                                                                                       |
| admin:write:user              | Allow to the update or create users.                                                                                               |
| admin:read:user               | Allow get the information about a user (like groups or additionally permissions).                                                  |
| admin:write:adminuser         | Additional permission to update or create users with adminstration rights (thus having an extra permission thats starts "admin:"). |


#### Querying ####

Every user that is part of a group that contains a corpus always get these permissions for a corpus automatically. If you want to allow a user to access only a certain functionality 
you can add more fine grained permissions. E.g.
\verbatim
query:count:*
\endverbatim
allows a user to count on all corpora but won't allow him to fetch the annotation graphs. For users that use the graphical user interface and not the service directly you should always grant a user all query permissions for a corpus. Otherwise he the user interface might not function as expected.

| permission                      | description                                                                            |
| ------------------------------- | -------------------------------------------------------------------------------------- |
| query:show:**{corpusname}**     | Allow to show information about a specific corpus.                                     |
| query:count:**{corpusname}**    | Allow to count on a specific corpus.                                                   |
| query:find:**{corpusname}**     | Allow to find matches on a specific corpus.                                            |
| query:subgraph:**{corpusname}** | Allow to query annotated subgraphs and complete annotated graphs on a specific corpus. |
| query:binary:**{corpusname}**   | Allow to get the  binary files of a specific corpus.                                   |
