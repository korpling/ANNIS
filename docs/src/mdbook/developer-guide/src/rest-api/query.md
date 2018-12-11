# Corpus queries

Interface defining the REST API calls that ANNIS provides for querying the data. 

All paths for this part of the service start with the `annis/query/` prefix.

## *Count* matches of an AQL query 

### Path(s)

- `GET annis/query/search/count` 


### Parameters

- `q` - The AQL query
- `corpora` - A comma separated list of corpus names 

### MIME

Produces an XML representation of the total matches and the number of documents that contain matches (`application/xml`):
```xml
<matchAndDocumentCount>
  <!-- the number of documents that contain matches -->
  <documentCount>2</documentCount>
  <!-- total number of matches -->
  <matchCount>399</matchCount>
</matchAndDocumentCount>
```


## *Find* matches for a given AQL query

### Path(s)

- `GET annis/query/search/find` 

### Parameters

- `q` - The AQL query 
- `corpora` - A comma separated list of corpus names 
- `offset` - Optional offset from where to start the matches. Default is 0. 
- `limit` - Optional limit of the number of returned matches. Set to -1 if unlimited. Default is -1. 
- `order` - Optional order how the results should be sorted. Can be either "normal", "random" or "inverted" "normal" is the default ordering, "inverted" inverses the default ordering and "random" is a non-stable (thus you will get different results for the same offset and limit) random ordering. 

### MIME

produces `application/xml`:

```xml
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
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<match-group>
```

*or* produces: text/plain:
```
salt:/pcc2/11299/#tok_1 tiger::pos::salt:/pcc2/11299/#tok_2
salt:/pcc2/11299/#tok_2 tiger::pos::salt:/pcc2/11299/#tok_3
salt:/pcc2/11299/#tok_3 tiger::pos::salt:/pcc2/11299/#tok_4
```
One line per match, each ID is separated by space. An ID can be prepended by the fully qualified annotation name (which is separated with '::' from the ID).

