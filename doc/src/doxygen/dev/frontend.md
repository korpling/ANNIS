Front-end  {#dev-frontend}
=========

The ANNIS front-end is a web application implemented in Java and the [Vaadin](https://vaadin.com/) framework. 
It communicates with the backend service via a REST interface.

Running an embedded Jetty instance for local access
---------------------------------------------------

This way of running the front-end is very useful, if you want to access Annis on
you local machine as a single user.

You don't need to install Jetty or Tomcat by yourself using this method.

~~~{.sh}
cd <unzipped source>/annis-gui/
mvn jetty:run
~~~

Now you can access the site under http://localhost:8080/annis-gui/app. The Jetty
server might be stopped by pressing "CTRL-C".
