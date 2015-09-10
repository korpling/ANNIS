Install ANNIS Service and Web front-end on different servers {#admin-multiple-servers}
============================================================

It is possible to install the service and the front-end on different servers.
Per-default the ANNIS service is only listening to connections from localhost for security reasons.
You should use a proxy server if you want to enable access from outside.
E.g. the Apache configuration could look like this:
\verbatim
ProxyPass /annis3-service http://localhost:5711
<location /annis3-service>
 SSLRequireSSL
</location>
\endverbatim  
If you your server is `example.com` this configuration would result in the service URL `https://example.com/annis3-service/annis/`

The service is responsible for the authentification and authorization (see [the user configuration](@ref admin-configure-user) for more information), thus the corpora are only accessible by the 
service if the user can provide the appropriate credentials.
[HTTP Basic Authentification](http://en.wikipedia.org/wiki/Basic_access_authentication) is used for transporting the user name and password as clear text over the network.
**Thus you should always make sure to enforce encrypted SSL (HTTPS) connections for the public accessable service.**

After you made the service available for other servers you have to configure the front-end to use this non-default service URL.
Change the file `WEB-INF/conf/annis-gui.properties` and set the `AnnisWebService.URL` to the right value:
\verbatim
AnnisWebService.URL=https://example.com/annis3-service/annis/
DotPath=dot
# set to an valid e-mail adress in order to enable the "Report a bug" button
bug-e-mail=
\endverbatim

If you want to secure your service even further you might want to setup a firewall in a way that only the server running the front-end is allowed to access the HTTP(S) port on the server running the backend service.
