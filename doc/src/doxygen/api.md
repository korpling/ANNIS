Public REST API {#api}
======================

The ANNIS-service has an public REST API that can be accessed by third-party applications.

Authentification {#api-authentification}
================

Some of the API calls are protected resources. HTTP user/password authentification is used in this case.
The users of the REST service can be configured as described [here](@ref admin-configure-user).
Whenever a user is not permitted to perform a certain action a `403 Forbidden` HTTP response is sent.
On some actions (like "list all corpora") only the resources that are available to the user are shown.

You can omit any authentification data. In this case you have the same rights as the "anonymous" user.

Available APIs {#api-available-list}
==============

The following APIs are currently available:

- [Corpus queries](@ref annis.service.QueryService)
- [Administrative tasks](@ref annis.service.AdminService)

ANNIS uses the semantic versioning scheme (http://semver.org/). Minor version updates of ANNIS will be backwards-compatible
to the APIs described by this documentation. There might be further un-official API calls that might change without any
notice.
