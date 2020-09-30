# Architecture 

ANNIS is a web-based search and visualization architecture for
multi-layer corpora. ANNIS consists of two major components: a backend
service and a web front-end.

## Backend Service

The service is part of the graphANNIS project and provides a REST API to query and manage corpora.
GraphANNIS directly implements all needed functionality without the need of an external database.
See the [graphANNIS documentation](https://korpling.github.io/graphANNIS/docs/v0.30/) for more information about the [REST service](https://korpling.github.io/graphANNIS/docs/v0.30/rest.html).

## Web Front-end

The ANNIS front-end is a web application implemented in Java and the [Vaadin](https://vaadin.com/) framework and runs in a normal browser (we recommend Mozilla Firefox). 
The server running the web-application communicates with the backend service via a REST interface.
Per default, the web front-end includes a released version of the backend service and starts an instance of it.
The front-end uses Spring Boot internally and bundles it own server, so you don't need a Tomcat or Jetty installation to run it.
