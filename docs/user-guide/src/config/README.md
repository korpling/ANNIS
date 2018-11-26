# Configuration and Administration

ANNIS consists of a front-end web-application, a back-end service and the PostgreSQL server.
The behavior of these components can be customized changing their configuration.

## Back-end service

There is a configuration folder `conf/` in the installation directory with the main configuration file `annis-service.properties`.
In addition, there is the `shiro.info` file to change the location of the [user configuration](user.md).
You should not need to change any of the other files in this directory.

## Front-end web-application

The ANNIS front-end will search in different folders for it's configuration.

Folder | Description
------ | -----------
`<Installation>/WEB-INF/conf/` | Default configuration inside the deployed web application folder. Should not be changed.
`$ANNIS_CFG` or `/etc/annis/` | The global configuration folder defined by the environment variable `ANNIS_CFG` or a default path if not set.
`~/.annis/` | User specific configuration inside the `.annis` sub-folder inside the home folder of the user who is running the frontend.

Configuration files can be either in the [Java Properties](http://en.wikipedia.org/w/index.php?title=.properties&oldid=521500688)
or [JSON](http://www.json.org/) format. Configuration files from the user directory can
overwrite the global configuration and the global configuration overwrites the
default configuration.


## PostgreSQL

See the [PostgreSQL documentation](https://www.postgresql.org/docs/9.6/runtime-config.html) for more information about how to change the
PostgreSQL configuration in general. 
We provide a [guide for tuning PostgreSQL for ANNIS](postgresql.md).