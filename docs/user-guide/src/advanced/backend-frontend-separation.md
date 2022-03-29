# Install backend and front-end on different servers

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
If you your server is `example.com` this configuration would result in the service URL `https://example.com/annis3-service/v2/`

The service is responsible for the authentication and authorization (see [the user configuration](../configuration/user.md) for more information), thus the corpora are only accessible by the 
service if the user can provide the appropriate credentials.
User names and passwords will be sent as clear text over the network.
**Thus you should always make sure to enforce encrypted SSL (HTTPS) connections for the public accessible service.**

After you made the service available for other servers you have to configure the front-end to use this non-default service URL.
Change the file `application.properties` and set the `annis.webservice-url` to the right value:
~~~properties
annis.webservice-url=https://example.com/annis3-service/v2
~~~

If you want to secure your service even further you might want to setup a firewall in a way that only the server running the front-end is allowed to access the HTTP(S) port on the server running the backend service.
