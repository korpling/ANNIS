# Configuration of ANNIS

For the desktop version of ANNIS, you most probably don't need to change any of the default configuration and you can skip this section.
If you are installing ANNIS on a server however, you might want to tweak the settings.
The [Java Properties](http://en.wikipedia.org/w/index.php?title=.properties&oldid=521500688), [TOML](https://toml.io/) and [JSON](http://www.json.org/) file formats are used for different kind of configuration.

ANNIS uses the Spring Boot configuration system and thus it will search for a Java Properties based configuration file named `application.properties` in the current working directory or a `config` sub-directory of the working directory.[^working-dir]
You can also use the command line argument `--spring.config.location=file:/<location-on-disk>` to specify a specific configuration file.
More search paths for configurations files are documented in the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/2.3.x/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files).


The following is an example configuration with ANNIS-specific configuration values.
```properties
# The port on which this service is listening
server.port=5712

# Defines from where the user interface service can be accessed from.
# 127.0.0.1 as value will only allow connections from the server itself (localhost)
# The default value 0.0.0.0 allows connections from everywhere.
server.address=127.0.0.1

# Allows to change the context path the ANNIS frontend application is deployed to
# E.g. you can change this to /annis4 to change the path to 
# http://localhost:5711/annis4
server.servlet.context-path=

# A configuration file for the embedded REST Service, which is started automatically
# by the ANNIS application.
# See https://korpling.github.io/graphANNIS/docs/v2.1/rest/index.html 
# for more information on how to configure the graphANNIS service.
annis.webservice-config=${user.home}/.annis/service.toml

# If non-empty this URL is used to communicate with the REST backend service instead of using the embedded service (which will not be started)
# annis.webservice-url=http://localhost:5711/v1

# set to "true" to globally disable right-to-left text detection
annis.disable-rtl=false
# set to an valid e-mail adress in order to enable the "Report a bug" button
annis.bug-e-mail=user@example.com

# If "true", the user can create reference links with shortened URLs
annis.shorten-reference-links=true

# Path to the persistent database, where e.g. the reference links are stored  
spring.datasource.url=jdbc:h2:file:${user.home}/.annis/v4/frontend_data.h2
```

Being a Spring Boot application, ANNIS configuration properties also be directly given as [command line argument](https://docs.spring.io/spring-boot/docs/2.3.x/reference/html/spring-boot-features.html#boot-features-external-config-command-line-args).

[^working-dir]: Depending on how you start ANNIS, the working directory can be different.
When using `java -jar /path/to/annis-server.jar`, the working directory is the same as the working directory of your current shell.
If you start ANNIS by treating it like an executable file, e.g. by running `/path/to/annis-server.jar` directly, the directory containing the jar is used as the working directory of the application per default.