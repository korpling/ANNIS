# Public REST API

The ANNIS-service has an public REST API that can be accessed by third-party applications.

## Authentication

Some of the API calls are protected resources. HTTP user/password authentification is used in this case.
The users of the REST service can be configured as described in the User Guide.
Whenever a user is not permitted to perform a certain action a `403 Forbidden` HTTP response is sent.
On some actions (like "list all corpora") only the resources that are available to the user are shown.

You can omit any authentication data. In this case you have the same rights as the "anonymous" user.

## Available APIs

The following APIs are currently available:

- Corpus queries as described by the `annis.service.QueryService` [interface](http://static.javadoc.io/de.hu-berlin.german.korpling.annis/annis-interfaces/${project.version}/annis/service/QueryService.html)
- Administrative tasks as described by the `annis.service.AdminService` [interface](http://static.javadoc.io/de.hu-berlin.german.korpling.annis/annis-interfaces/${project.version}/annis/service/AdminService.html)

ANNIS uses the [semantic versioning scheme](http://semver.org/). Minor version updates of ANNIS will be backwards-compatible
to the APIs described by this documentation. There might be further un-official API calls that might change without any
notice.