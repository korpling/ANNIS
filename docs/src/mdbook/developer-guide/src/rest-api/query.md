# Corpus queries

Interface defining the REST API calls that ANNIS provides for querying the data. 

All paths for this part of the service start with the "annis/query/" prefix.

## *Count* matches of a query 

### Path(s)

1. `GET` annis/query/search/count


### Parameters

- `q` - The query in the ANNIS Query Language (AQL)
- `corpora` - A comma separated list of corpus names 

### Responses

#### Code 200

Produces an XML representation of the total matches and the number of documents that contain matches (`application/xml`):

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<matchAndDocumentCount>
  <!-- the number of documents that contain matches -->
  <documentCount>2</documentCount>
  <!-- total number of matches -->
  <matchCount>399</matchCount>
</matchAndDocumentCount>
```


## *Find* matches for a given query

### Path(s)

1. `GET` annis/query/search/find 

### Parameters

- `q` - The query in the ANNIS Query Language (AQL)
- `corpora` - A comma separated list of corpus names 
- `offset` - Optional offset from where to start the matches. Default is 0. 
- `limit` - Optional limit of the number of returned matches. Set to -1 if unlimited. Default is -1. 
- `order` - Optional order how the results should be sorted. Can be either "normal", "random" or "inverted" "normal" is the default ordering, "inverted" inverses the default ordering and "random" is a non-stable (thus you will get different results for the same offset and limit) random ordering. 

### Responses

#### Code 200

A list of the match identifiers for the query.

Can produce the MIME type `application/xml` in the following format

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<match-group>
  <!-- each match is enclosed in an match tag -->
  <match>
    <!-- the first matched node of match 1 did not match an annotation -->
    <anno></anno>
    <!-- the second matched node of match 1 was a match on the 'tiger::pos' annotation-->
    <anno>tiger::pos</anno>
    <!-- ID of first matched node of match 1 -->
    <id>salt:/pcc2/11299/#tok_1</id>
    <!-- ID of second matched noded  of match 1 -->
    <id>salt:/pcc2/11299/#tok_2</id>
  </match>
  <match>
    <anno></anno>
    <anno>tiger::pos</anno>
    <!-- ID of first matched noded of match 2 -->
    <id>salt:/pcc2/11299/#tok_2</id>
    <!-- ID of second matched noded of match 2-->
    <id>salt:/pcc2/11299/#tok_3</id>
  </match>
  <!-- and so on -->
</match-group>
```

*or* the MIME type `text/plain`
```
salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
salt:/pcc2/11299/#tok_2 tiger::pos::salt:/pcc2/11299/#tok_3
salt:/pcc2/11299/#tok_3 tiger::pos::salt:/pcc2/11299/#tok_4
```
In this format, there is one line per match and each ID is separated by space.
An ID can be prefixed by the fully qualified annotation name (which is separated with '::' from the ID).

## Get a *subgraph* from a set of (matched) Salt IDs

### Path(s)

1. `POST` annis/query/search/subgraph

### Request body

Accepts `application/xml`:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<match-group>
  <!-- each match is enclosed in an match tag -->
  <match>
    <!-- the first matched node of match 1 did not match an annotation -->
    <anno></anno>
    <!-- the second matched node of match 1 was a match on the 'tiger::pos' annotation-->
    <anno>tiger::pos</anno>
    <!-- ID of first matched node of match 1 -->
    <id>salt:/pcc2/11299/#tok_1</id>
    <!-- ID of second matched noded  of match 1 -->
    <id>salt:/pcc2/11299/#tok_2</id>
  </match>
  <match>
    <anno></anno>
    <anno>tiger::pos</anno>
    <!-- ID of first matched noded of match 2 -->
    <id>salt:/pcc2/11299/#tok_2</id>
    <!-- ID of second matched noded of match 2-->
    <id>salt:/pcc2/11299/#tok_3</id>
  </match>
  <!-- and so on -->
</match-group>
```

*or* accepts `text/plain`:

```
salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
salt:/pcc2/11299/#tok_2 tiger::pos::salt:/pcc2/11299/#tok_3
salt:/pcc2/11299/#tok_3 tiger::pos::salt:/pcc2/11299/#tok_4
```

One line per match, each ID is separated by space. An ID can be prepended by the fully qualified annotation name (which is separated with '::' from the ID).

### Parameters

- `segmentation` - Optional parameter for segmentation layer on which the context is applied. Leave empty for token layer (which is default).
- `left` - Optional parameter for the left context size, default is 0.
- `right` - Optional parameter for the right context size, default is 0.
- `filter` - Optional parameter with value "all" or "token". If "token" only token will be fetched. Default is "all". 

### Responses

#### Code 200

Returns a representation of the [Salt](http://corpus-tools.org/salt/) annotation graph in the EMF XMI format and with MIME type `application/xml` or `application/xmi+xml`. 

## Get the annotation *graph* of a complete document

### Path(s)

1. `GET` annis/query/graph/**{top}**/**{doc}**

**{top}** is the toplevel corpus name of the document and **{doc}** the document name. 

### Parameters

- `filternodeanno` - 	A comma seperated list of node annotations which are used as a filter for the graph. Only nodes having one of the annotations are included in the result. 

### Responses

#### Code 200

Returns a representation of the [Salt](http://corpus-tools.org/salt/) annotation graph in the EMF XMI format and with MIME type `application/xml` or `application/xmi+xml`. 

## Get the content a *binary* object for a specific document

### Path(s)

1. `GET` annis/query/corpora/**{top}**/**{document}**/binary 
2. `GET` annis/query/corpora/**{top}**/**{document}**/binary/**{offset}**/**{length}**
3. `GET` annis/query/corpora/**{top}**/**{document}**/binary/**{file}**
4. `GET` annis/query/corpora/**{top}**/**{document}**/binary/**{file}**/**{offset}**/**{length}**

Accepts any MIME type. The MIME type is used as implicit argument to filter the files that match a given query. 

There are several ways of selecting the binary data you want to receive. 
You can choose to select the file only by giving a document name given by the **{top}** and **{document}** arguments (paths 1 and 2). 
This will return the first file that also matches the requested accepted mime types. 
Alternatively the name of the file itself can be given as path argument **{file}** (paths 3 and 4).
You can also choose to either get the complete file (paths 1 and 3) or chunks containing only a subset of the binary data (paths 2 and 4). 
In the latter case, you can specify the **{offset}** and the **{length}** of the chunk (both in bytes).

- **{top}** - The toplevel corpus name. 
- **{document}** - The name of the document that has the file. If you want the files for the toplevel corpus itself, use the name of the toplevel corpus as document name. 
- **{file}** - File name/title to select.
- **{offset}** - Defines the offset from the the binary chunk starts (in bytes).
- **{length}** - Defines the length of the binary chunk (in bytes).

### Parameters

No parameters.

### Responses

#### Code 200

A binary stream that contains the file content. If path variant 2 and 4 is used only a subset of the file is returned. 
Path variant 1 and 3 always return the complete file.