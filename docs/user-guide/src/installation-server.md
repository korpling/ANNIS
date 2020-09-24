# Installing an ANNIS Server

The ANNIS server version can be installed  most   versions   of  Linux,   Windows and  MacOS but we will focus on Linux based servers in this documentation. 
To install the ANNIS server:

1. Download and the ANNIS Server JAR-file from the ANNIS website. We assume you are saving it under `/usr/local/bin` but you can choose any other location.
2. Open your system terminal and execute the JAR-file:
   ```bash
   /usr/local/bin/annis-gui-<version>-desktop.jar
   ```
   You have to replace `<version>` with the version of ANNIS you are using.

This will start a REST service on port 5711 and the user interface on port 5712.
The user interface service will accept requests from all IPs and the embedded REST service only from `localhost`.
We recommend using a Nginx or Apache server as proxy for the user interface service for production to enable important features like HTTPS encryption.

## Use systemd to run ANNIS as a service

You can create a simple systemd configuration file with the name `annis.service` and save it under one of the valid configuration folders, e.g. `/etc/systemd/system` to register ANNIS as a system service.

```
[Unit]
Description=ANNIS corpus search and visualization

[Service]
Type=simple
ExecStart=/usr/local/bin/annis-gui-<version>-desktop.jar
Environment=""
User=annis
Group=annis
WorkingDirectory=/usr/local

[Install]
WantedBy=multi-user.target
```

This configuration assumes that there is a “annis” user and group on the server.

You can permanently enable this service by calling
```bash
$ systemctl enable annis.service
```

Since ANNIS is a Spring Boot application, the other methods of running a Spring Boot application as service (described in their [documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html#deployment-service)) are also possible.

## Configure the service

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

## Install ANNIS Service and Web front-end on different servers

It is possible to install the service and the front-end on different servers.
Per-default the ANNIS REST service is only listening to connections from localhost for security reasons.
You should use a proxy server if you want to enable access from outside.
E.g. the Apache configuration could look like this:
~~~Apache
ProxyPass /annis3-service http://localhost:5711
<location /annis3-service>
 SSLRequireSSL
</location>
~~~ 
If you your server is `example.com` this configuration would result in the service URL `https://example.com/annis3-service/v0/`

The service is responsible for the authentication and authorization (see [the user configuration](import-and-config-user.md) for more information), thus the corpora are only accessible by the 
service if the user can provide the appropriate credentials.
User names and passwords will be sent as clear text over the network.
**Thus you should always make sure to enforce encrypted SSL (HTTPS) connections for the public accessible service.**

After you made the service available for other servers you have to configure the front-end to use this non-default service URL.
Change the file `application.properties` and set the `annis.webservice-url` to the right value:
~~~properties
annis.webservice-url=http://localhost:5711/v0
~~~

If you want to secure your service even further you might want to setup a firewall in a way that only the server running the front-end is allowed to access the HTTP(S) port on the server running the backend service.
