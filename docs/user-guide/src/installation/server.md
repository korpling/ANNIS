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

## Configuring a proxy server

To configure a proxy that uses a different path than `/`, e.g. `https://example.com/annis`, first make sure that the context path is set in the [`application.properties` file](../configuration/).

```
server.servlet.context-path=/annis
```

### Nginx

You can configure the location to act as a proxy.
The `location` must match the context path and end with an `/`.
The `proxy_pass` directive does not contain the context path, but only specifies the server host and port.

```
location /annis/ {
   proxy_pass http://127.0.0.1:5712;
   # Optimize for Web Sockets
   # https://www.nginx.com/blog/websocket-nginx/
   proxy_http_version 1.1;
   proxy_set_header Upgrade $http_upgrade;
   proxy_set_header Connection: $connection_upgrade;
   client_max_body_size 2G;
}
```

### Apache

Recent versions of Apache have support for proxying web socket connects, but the `mod_proxy` module must be enabled: <https://httpd.apache.org/docs/2.4/mod/mod_proxy.html>

Then add the following configuration to your site:

```
ProxyPass /annis/ http://localhost:5712/annis/ upgrade=websocket
ProxyPassReverse /annis/ http://localhost:5712/annis/
```

## Use systemd to run ANNIS as a service

You can create a simple systemd configuration file with the name `annis.service` and save it under one of the valid configuration folders, e.g. `/etc/systemd/system` to register ANNIS as a system service.

```
[Unit]
Description=ANNIS corpus search and visualization

[Service]
Type=simple
ExecStart=/usr/local/bin/annis-gui-<version>-desktop.jar --spring.config.location=file:/<location-on-disk>
Environment=""
User=annis
Group=annis
WorkingDirectory=/usr/local

[Install]
WantedBy=multi-user.target
```

This configuration assumes that there is a “annis” user and group on the server.
The `WorkingDirectory` is not used to locate the `application.properties` file and thus it is best to specify its location with the `--spring.config.location` argument.

You can permanently enable this service by calling
```bash
$ systemctl enable annis.service
```

Since ANNIS is a Spring Boot application, the other methods of running a Spring Boot application as service (described in their [documentation](https://docs.spring.io/spring-boot/docs/2.3.x/reference/html/deployment.html#deployment-service)) are also possible.

