# Administrative tasks

Interface defining the REST API calls that ANNIS provides for administrative tasks.

Currently it is possible to import corpora, monitor the import status with this interface and to manage user accounts.
All paths for this part of the service start with "annis/admin/".

## *Import* one or more corpora

### Path(s)

1. `POST` annis/admin/import

### Request body

A ZIP file which contains one or more corpora in separate sub-folders.
Consumes MIME type `application/zip`.

### Parameters

- `overwrite` - Set to "true" if an existing corpus corpus should be overwritten.
- `statusMail` - An e-mail address to which status reports are sent.
- `alias` - An internal alias name of the corpus which can be used instead of the actual corpus name when referring to it in the URL. Corpora can share the same alias.

### Responses

#### Code 202 

Import has been accepted and its status can be queried by the URL given in the `Location` header.

#### Code 400

Bad request, e.g. if the corpus already exists and `overwrite` parameter was not set.

## *Status* of all running import jobs

### Path(s)

1. `GET` annis/admin/import/status/

### Responses

#### Code 200

The response lists all currently running import jobs has the MIME type `application/xml` and the following format:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<importJobs>
  <!-- an importJob tag for each running import -->
  <importJob>
    <!-- visible caption, e.g. corpus name  -->
    <caption>MyNewCorpus</caption>
    <!-- A list of output messages from the import process-->
    <messages>
      <m>first message</m>
      <m>second message</m>
      <m>just another message</m>
    </messages>
    <!-- true if the corpus will be overwritten -->
    <overwrite>true</overwrite>
    <!-- current status, can be WAITING, RUNNING, SUCCESS or ERROR -->
    <status>RUNNING</status>
    <!-- an unique identifier for this import job -->
    <uuid>7799322d-83ec-4900-83b0-c542e2ca2137</uuid>
    <!-- a mail address to which status reports should be send -->
    <statusMail>mail@example.com</statusMail>
    <!-- alias name of the corpus as defined by the import request -->
    <alias>CorpusAlias</alias>
 </importJob>
</importJobs>
```
The root element has the name `importJobs` and there is an `importJob` element for each element of the list. 

## Show import job information after it was *finished*

### Path(s)

1. `GET` annis/admin/import/status/finished/**{uuid}**

The **{uuid}** defines an unique identifier of the import job, which was returned by the [import function](#import-one-or-more-corpora).

### Responses

#### Code 200

If the import finished, a 200 HTTP status code is sent and a proper description of the import job is returned.
After this resource has been successfully accessed once, a 404 HTTP status code will be sent on subsequent requests.

The response has the MIME type `application/xml` and the following format:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<importJob>
  <!-- visible caption, e.g. corpus name  -->
  <caption>MyNewCorpus</caption>
  <!-- A list of output messages from the import process-->
  <messages>
    <m>first message</m>
    <m>second message</m>
    <m>just another message</m>
  </messages>
  <!-- true if the corpus will be overwritten -->
  <overwrite>true</overwrite>
  <!-- current status, can be WAITING, RUNNING, SUCCESS or ERROR -->
  <status>RUNNING</status>
  <!-- an unique identifier for this import job -->
  <uuid>7799322d-83ec-4900-83b0-c542e2ca2137</uuid>
  <!-- a mail address to which status reports should be send -->
  <statusMail>mail@example.com</statusMail>
  <!-- alias name of the corpus as defined by the import request -->
  <alias>CorpusAlias</alias>
</importJob>
```

#### Code 404

When the import is not finished yet or if status was finished and already queries, a 404 HTTP status code will be sent.

## Information about existing *users* 

### Path(s)

1. `GET` annis/admin/users/**{userName}**

Gets information about an existing user with the name **{userName}**.

### Responses

#### Code 200

If a user exists, return the user information with the MIME type `application/xml`.
The fields correspond to the fields of the single user configuration file. 
Please have a look at the general user configuration information in the ANNIS user guide for a more detailed explanation.

```xml
<user>
  <!-- User name (must be the same as the "userName" parameter) -->
  <name>myusername</name>
  <!-- hashed password in the Shiro1CryptFormat -->
  <passwordHash>$shiro1$SHA-256$1$tQNwU[...]</passwordHash>
  <!-- A list of groups the users should belong to. -->
  <group>group1</group>
  <group>group2</group>
  <group>group3</group>
  <!-- A list of explicit permission the users should have. -->
  <permission>admin:*</permission>
  <permission>query:*</permission>
  <!-- Optional expiration date encoded in the ISO-8601 standard</a> -->
  <expires>2015-02-12T00:00:00.000+01:00</expires>
</user>
```

- [Shiro1CryptFormat](http://shiro.apache.org/static/1.3.2/apidocs/org/apache/shiro/crypto/hash/format/Shiro1CryptFormat.html)
- [ISO-8601 standard](https://en.wikipedia.org/wiki/ISO_8601)