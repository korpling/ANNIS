# Migrating from ANNIS 3

These instructions are a guideline for upgrading the installation of ANNIS on a UNIX-like server. 
If you use the [ANNIS Kickstarter version](installation-kickstarter.md) 
just download the new version and (re-) import the corpora you need.
Please read [the installation instructions](installation-server.md) first if you
haven't done so yet.


## Manual upgrade


#### 1. Save old user configuration and reference links

The user configuration folder can be re-used from the existing ANNIS service and does not need to be copied.
However, users can store custom settings which are stored in a database table with the name `user_config`.
You can export the old version of the table as a CSV file by executing
```.sh
annis-admin.sh dump user_config user_config.csv
```
using the old ANNIS service.
You can select any output file in a location of your choice.
If you want to migrate the reference links, you can export them as well:
```.sh
annis-admin.sh dump url_shortener url_shortener.csv
```

#### 2. Download
Download both the `annis-service-<VERSION>.tar.gz` and the `annis-gui-<VERSION>.war`
to a folder of your choice, e.g. `/tmp/`.

#### 3. Install the new service

Unzip the annis service to a new  directory (don't delete or stop the old service)
and install it. 
These steps are similiar to how to [install a new ANNIS service](./installation-server.md).
```.sh
tar xvzf /tmp/annis-service-<VERSION>.tar.gz -C <new installation directory>
export ANNIS_HOME=<new installation directory>>
export PATH=$PATH:$ANNIS_HOME/bin
```
If you made any manual changes to the `conf/annis-service.properties` file copy the relevant changes to the new installation.
Make sure the changed configuration values also exist in the new version ANNIS.

#### 4. Copy old corpora

The `import` command of the `annis-admin.sh` now supports an additional parameter `--service-url`, which tells the command to import all corpora available from the running service.

You can execute the command
```.sh
annis-admin.sh import --service-url <old-service-url>
```
to import all the public available corpora from the old service.
**For this command to work correctly, the new service needs access to same files from where the old service imported the corpora**.
It is therefore best to run both services on the same server or to copy the directories which contain the corpora from the old server to the new one.
If corpora are not available without login, you can add the `--service-username` parameter to specify a user name with access to all corpora.
The import command will ask you for the password interactively.
 
This can take a long time, so if you use SSH you might want use a program like e.g. [Byobu](http://byobu.org/) to start a terminal that continues to run process in the background even if the connection is interrupted.


#### 5. Migrate user configuration and reference links

You can restore the user settings, execute
```.sh
annis-admin.sh restore user_config user_config.csv
```

ANNIS aims to be as backward-compatible as possible, but we need to make sure the reference links actually point not only to the same queries and corpora, but show exactly the same result [^sameresult].
The special `migrate-url-shortener` sub-command of the `annis-admin.sh` program will execute each query in the given CSV file both on the old ANNIS service and on the new one.
```.sh
annis-admin.sh migrate-url-shortener --service-url <old-service-url> url_shortener.csv
```
It will then compare the results.
If both results are exactly the same, it migrates the reference link to the new system.
Some queries only work in the same way in [compatibility query mode](./aql-compatibility-mode.md), these will be rewritten automatically.
The user will be able to execute this query normally, but sees a warning that its using deprecated functions of the query language.
For queries where both the normal and the compatibility mode don't return the same results, they are still migrated but a clear warning is shown to the user before execution, that this query is known to give different results compared to the previous version of ANNIS.

The command will give a summary how many reference links have been migrated successfully and which kind of errors occurred on the other ones.
If you think a specific query triggers a bug in the new version of ANNIS, please check if there is already a bug report on [https://github.com/korpling/ANNIS/issues](https://github.com/korpling/ANNIS/issues) or open a new one.

#### 6. Switch service

Stop the old service and start the
new service. Remember to set the `ANNIS_HOME` variable to the right value in
both cases before you call `annis-service.sh start/stop` command.

#### 7. Upgrade front-end

Undeploy the old WAR file and deploy the new WAR file.

#### 8. Cleanup

If everything works as expected you can delete the old installation files. 
To delete all unused external data files execute
~~~bash
annis-admin.sh cleanup-data
~~~

***Warning!*** This will delete all data files not known to the current instance of ANNIS.
If you have multiple parallel installations and did not use different values for
the `annis.data-path` variable in the `conf/annis-service.properties`
the data files of the other installations will be lost.

[^sameresult:] Same result means, that the overall count is correct, that the matches have the same Salt IDs and that they are presented in the same order.