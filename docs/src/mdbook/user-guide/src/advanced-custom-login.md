# Provide your own login-system

Per default ANNIS can be used with either no logins at all or with a system where there are users which have a password and can belong to certain groups. The configuration for the default login is system is described [here](import-and-config-user.md).

This documentation provides the proposed way of using external authentication systems like [Shibboleth](http://shibboleth.net/) or [OpenID](http://openid.net/) with ANNIS. ANNIS is using a REST webservice internally. 
This webservice is secured with HTTP user/password authentication. When logging into ANNIS via the web frontend, the credentials are
only passed through to the webservice. Thus all 3rd party authentication systems that rely on
browser redirects can't be used directly. The proposed way is to use temporary accounts instead.

The first step is to configure a custom login page in the `annis-gui.properties` file, which must be located in one of the *front-end configuration folders*. Here you can set the `login-url` parameter to the URL of your custom login page. Optionally set `login-window-maximized=true` to use the full browser window. Since the login page will be displayed using an iframe, it should be on the same server as the ANNIS web frontend.

The custom login page should be a web application that does the following things:
- authenticate the user
- use the special REST API function `PUT annis/admin/users/{userName}` to create a temporary account with a random password (you can use an expiration date to limit the validity). 
FIXME: add more detailed link to REST API here.
- initiate a `POST` request to `https://<yourdmain>/<annispath>/login` containing the parameters `annis-login-user` and `annis-login-password` from the users browser.
- if you want to close the login window without changing the login-information execute the `window.parent.annis.gui.logincallback()` javascript function