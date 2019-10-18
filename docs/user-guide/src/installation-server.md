# Installing an ANNIS Server

The ANNIS server version can be installed on UNIX based servers, or else under
Windows using [Cygwin](http://www.cygwin.com/), the freely available UNIX emulator. To install the ANNIS server:

1. Install a Java Servlet Container ("Java web server") such as Tomcat or Jetty
2. Download the ANNIS service distribution file `annis-service-<version>- distribution.tar.gz` from the website and then unzip the downloaded file:
```bash
tar xzvf annis-service-<version>-distribution.tar.gz -C <installation directory>
```
3. Set the environment variables (each time when starting up)
```bash
export ANNIS_HOME=<installation directory>

export PATH=$PATH:$ANNIS_HOME/bin
```
4.  Now you can import some corpora:
```bash
annis-admin.sh import path/to/corpus1 path/to/corpus2 ...
```
6. Then start the ANNIS service:
```bash
annis-service.sh start
```
7. To get the ANNIS front-end running, first download `annis-gui-<version>.war`
from our website and deploy it to your Java servlet container (this depends on
the servlet container you use).

## Tomcat: UTF8 encoding in server.xml

If using Tomcat make sure the UTF-8 encoding is used for URLs. Some
installations of Tomcat don't use UTF-8 for the encoding of the URLs and that will
cause problems when searching for non-ASCII characters. In order to avoid this
the Connector-configuration needs the property "URIEncoding" set to "UTF-8"
like in this example (`$CATALINA_HOME/server.xml`):

~~~xml
<Connector port="8080" protocol="HTTP/1.1"
connectionTimeout="20000"
URIEncoding="UTF-8"
redirectPort="8443"
executor="tomcatThreadPool" />
~~~

## Install ANNIS Service and Web front-end on different servers

It is possible to install the service and the front-end on different servers.
Per-default the ANNIS service is only listening to connections from localhost for security reasons.
You should use a proxy server if you want to enable access from outside.
E.g. the Apache configuration could look like this:
~~~Apache
ProxyPass /annis3-service http://localhost:5711
<location /annis3-service>
 SSLRequireSSL
</location>
~~~ 
If you your server is `example.com` this configuration would result in the service URL `https://example.com/annis3-service/annis/`

The service is responsible for the authentication and authorization (see [the user configuration](import-and-config-user.md) for more information), thus the corpora are only accessible by the 
service if the user can provide the appropriate credentials.
[HTTP Basic Authentication](http://en.wikipedia.org/wiki/Basic_access_authentication) is used for transporting the user name and password as clear text over the network.
**Thus you should always make sure to enforce encrypted SSL (HTTPS) connections for the public accessable service.**

After you made the service available for other servers you have to configure the front-end to use this non-default service URL.
Change the file `WEB-INF/classes/annis-gui.properties` and set the `AnnisWebService.URL` to the right value:
~~~Ini
AnnisWebService.URL=https://example.com/annis3-service/annis/
DotPath=dot
# set to an valid e-mail adress in order to enable the "Report a bug" button
bug-e-mail=
~~~

If you want to secure your service even further you might want to setup a firewall in a way that only the server running the front-end is allowed to access the HTTP(S) port on the server running the backend service.
