Provide you own login-system {#admin-configure-customlogin}
==========================

Per default ANNIS can be used with either no logins at all or with a system where there are users which have a password and can belong to certain groups. The configuration for the default login is system is described [here](@ref admin-configure-user).

This documentation provides the proposed way of using external authentification systems like [Shibboleth](http://shibboleth.net/) or [OpenID](http://openid.net/) with ANNIS. ANNIS is using a 
[REST webservice](@ref api) internally. This webservice is secured with HTTP user/password authentification. When logging into ANNIS via the web frontend, the credentials are
only passed to the webservice. Thus all 3rd party authentification system that rely on
browser redirects can't be used directly. The proposed way is to use temporary accounts instead.

The first step is to configure a custom login page in the `annis-gui.properties` file, which must be located in one of the [configuration folders](@ref admin-configure-general). Here you can set the `login-url` parameter to the URL of your custom login page. Optionally set `login-window-maximized=true` to use the full browser window. Since the login page will be displayed using an iframe, it should be on the same server as the ANNIS web frontend.
