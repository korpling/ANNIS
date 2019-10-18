# Architecture 

ANNIS is a web-based search and visualization architecture for
multi-layer corpora. ANNIS consists of two major components: a backend
service and a web front-end. There is also a local version, ANNIS
Kickstarter, which is a simple starting point for new users who want
to try out the system without installing a full server.

## Backend Service

The service runs on a web server such as Tomcat or Jetty and
communicates with a relational database, using the open source DB
PostgreSQL. PostgreSQL (Version 9.4 or larger) must be installed for ANNIS to work. For
more information on installing and managing the backend service, see
the Administration Guide in the documentation.

## Web Front-end

The ANNIS front-end is a web application implemented in Java and the [Vaadin](https://vaadin.com/) framework and runs in a normal browser (we recommend Mozilla Firefox). 
The server running the web-application communicates with the backend service via a REST interface.

## ANNIS Kickstarter

ANNIS Kickstarter is a cross-platform local version which requires
nothing but a PostgreSQL installation to run. It will run under LINUX,
Windows and Mac. For a quick tutorial to get started with Kickstarter,
see the ANNIS User Guide.