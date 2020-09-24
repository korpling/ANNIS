# Configuration of ANNIS

For the desktop version of ANNIS, you most probably don't need to change any of the default configuration.
If you are installing ANNIS on a server however, you might want to tweak the settings.

ANNIS will search in the following locations for configuration files in the [Java Property file format](https://en.wikipedia.org/wiki/.properties):

- `$HOME/.annis/annis-gui.properties`,
- `$ANNIS_CFG/annis-gui.properties`, and
- `/etc/annis/annis-gui.properties`.

The following is an example configuration with all possible configuration values
```properties
# The port on which this service is listening
server.port=5712

# Defines from where the user interface service can be accessed from.
# 127.0.0.1 as value will only allow connections from the server itself (localhost)
# The default value 0.0.0.0 allows connections from everywhere.
server.address=127.0.0.1

# A configuration file for the embedded REST Service. 
# See https://korpling.github.io/graphANNIS/docs/v0.29/rest.html 
# for more information on how to configure the graphANNIS service.
annis.webservice-config=${user.home}/.annis/service.toml

# If non-empty this URL is used to communicate with the REST backend service instead of using the embedded service (which will not be started)
# annis.webservice-url=http://localhost:5711/v0

# set to "true" to globally disable right-to-left text detection
annis.disable-rtl=false
# set to an valid e-mail adress in order to enable the "Report a bug" button
annis.bug-e-mail=user@example.com

# If "true", the user can create reference links with shortened URLs
annis.shorten-reference-links=true

# for some custom login pages a maximized window is more suitable, set this value
# to "true" in this case
annis.login-window-maximized=false
```

Being a Spring Boot application, ANNIS configuration properties also be given as [command line argument](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-command-line-args) or using [various other means](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-application-property-files).
