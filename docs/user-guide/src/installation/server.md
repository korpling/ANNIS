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

