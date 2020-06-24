# CorporaApi

All URIs are relative to *http://localhost:5711/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**corpusComponents**](CorporaApi.md#corpusComponents) | **GET** /corpora/{corpus}/components | List all edge components of the corpus.
[**corpusConfiguration**](CorporaApi.md#corpusConfiguration) | **GET** /corpora/{corpus}/configuration | Get the corpus configuration object.
[**corpusEdgeAnnotations**](CorporaApi.md#corpusEdgeAnnotations) | **GET** /corpora/{corpus}/edge-annotations/{type}/{layer}/{name}/ | List all annotations of the corpus for a given edge component
[**corpusFiles**](CorporaApi.md#corpusFiles) | **GET** /corpora/{corpus}/files | Get an associated file for the corpus.
[**corpusNodeAnnotations**](CorporaApi.md#corpusNodeAnnotations) | **GET** /corpora/{corpus}/node-annotations | List all node annotations of the corpus.
[**listCorpora**](CorporaApi.md#listCorpora) | **GET** /corpora | Get a list of all corpora the user is authorized to use.
[**subgraphForNodes**](CorporaApi.md#subgraphForNodes) | **POST** /corpora/{corpus}/subgraph | Get a subgraph of the corpus format given a list of nodes and a context.
[**subgraphForQuery**](CorporaApi.md#subgraphForQuery) | **GET** /corpora/{corpus}/subgraph-for-query | Get a subgraph of the corpus format given a list of nodes and a context.

<a name="corpusComponents"></a>
# **corpusComponents**
> List&lt;Component&gt; corpusComponents(corpus, type, name)

List all edge components of the corpus.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the components for.
String type = "type_example"; // String | Only return components with this type.
String name = "name_example"; // String | Only return components with this name.
try {
    List<Component> result = apiInstance.corpusComponents(corpus, type, name);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#corpusComponents");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the components for. |
 **type** | **String**| Only return components with this type. | [optional]
 **name** | **String**| Only return components with this name. | [optional]

### Return type

[**List&lt;Component&gt;**](Component.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="corpusConfiguration"></a>
# **corpusConfiguration**
> CorpusConfiguration corpusConfiguration(corpus)

Get the corpus configuration object.

The corpus configuration is created by the corpus authors to configure how the corpus should be displayed in query engines and visualizers.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the configuration for.
try {
    CorpusConfiguration result = apiInstance.corpusConfiguration(corpus);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#corpusConfiguration");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the configuration for. |

### Return type

[**CorpusConfiguration**](CorpusConfiguration.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="corpusEdgeAnnotations"></a>
# **corpusEdgeAnnotations**
> List&lt;Annotation&gt; corpusEdgeAnnotations(corpus, type, layer, name, listValues, onlyMostFrequentValues)

List all annotations of the corpus for a given edge component

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the configuration for.
String type = "type_example"; // String | The component type.
String layer = "layer_example"; // String | The component layer.
String name = "name_example"; // String | The component name.
Boolean listValues = false; // Boolean | If true, possible values are returned.
Boolean onlyMostFrequentValues = false; // Boolean | If true, only the most frequent value per annotation is returned.
try {
    List<Annotation> result = apiInstance.corpusEdgeAnnotations(corpus, type, layer, name, listValues, onlyMostFrequentValues);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#corpusEdgeAnnotations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the configuration for. |
 **type** | **String**| The component type. |
 **layer** | **String**| The component layer. |
 **name** | **String**| The component name. |
 **listValues** | **Boolean**| If true, possible values are returned. | [optional] [default to false]
 **onlyMostFrequentValues** | **Boolean**| If true, only the most frequent value per annotation is returned. | [optional] [default to false]

### Return type

[**List&lt;Annotation&gt;**](Annotation.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="corpusFiles"></a>
# **corpusFiles**
> corpusFiles(corpus, name)

Get an associated file for the corpus.

The annotation graph of a corpus can contain special nodes of the type \&quot;file\&quot;,  which are connected to (sub-) corpus and document nodes with a &#x60;PartOf&#x60; relation. This endpoint allows to access the content of these file nodes. It supports [HTTP range requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests)  if you only need to access parts of the file. 

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the configuration for.
String name = "name_example"; // String | The name of the file node.
try {
    apiInstance.corpusFiles(corpus, name);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#corpusFiles");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the configuration for. |
 **name** | **String**| The name of the file node. |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="corpusNodeAnnotations"></a>
# **corpusNodeAnnotations**
> List&lt;Annotation&gt; corpusNodeAnnotations(corpus, listValues, onlyMostFrequentValues)

List all node annotations of the corpus.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the configuration for.
Boolean listValues = false; // Boolean | If true, possible values are returned.
Boolean onlyMostFrequentValues = false; // Boolean | If true, only the most frequent value per annotation is returned.
try {
    List<Annotation> result = apiInstance.corpusNodeAnnotations(corpus, listValues, onlyMostFrequentValues);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#corpusNodeAnnotations");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the configuration for. |
 **listValues** | **Boolean**| If true, possible values are returned. | [optional] [default to false]
 **onlyMostFrequentValues** | **Boolean**| If true, only the most frequent value per annotation is returned. | [optional] [default to false]

### Return type

[**List&lt;Annotation&gt;**](Annotation.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="listCorpora"></a>
# **listCorpora**
> CorpusList listCorpora()

Get a list of all corpora the user is authorized to use.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
try {
    CorpusList result = apiInstance.listCorpora();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#listCorpora");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**CorpusList**](CorpusList.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="subgraphForNodes"></a>
# **subgraphForNodes**
> String subgraphForNodes(body, corpus)

Get a subgraph of the corpus format given a list of nodes and a context.

This creates a subgraph for node IDs, which can e.g. generated by executing a &#x60;find&#x60; query. The subgraph contains  - the given nodes,  - all tokens that are covered by the given nodes, - all tokens left and right in the given context from the tokens covered by the give nodes, - all other nodes covering the tokens of the given context. The annotation graph also includes all edges between the included nodes. 

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
SubgraphWithContext body = new SubgraphWithContext(); // SubgraphWithContext | The definition of the subgraph to extract.
String corpus = "corpus_example"; // String | The name of the corpus to get the subgraph for.
try {
    String result = apiInstance.subgraphForNodes(body, corpus);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#subgraphForNodes");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**SubgraphWithContext**](SubgraphWithContext.md)| The definition of the subgraph to extract. |
 **corpus** | **String**| The name of the corpus to get the subgraph for. |

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/xml

<a name="subgraphForQuery"></a>
# **subgraphForQuery**
> String subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter)

Get a subgraph of the corpus format given a list of nodes and a context.

This only includes the nodes that are the result of the given query and no context is created automatically. The annotation graph also includes all edges between the included nodes. 

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.CorporaApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


CorporaApi apiInstance = new CorporaApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the subgraph for.
String query = "query_example"; // String | The query which defines the nodes to include.
QueryLanguage queryLanguage = new QueryLanguage(); // QueryLanguage | 
AnnotationComponentType componentTypeFilter = new AnnotationComponentType(); // AnnotationComponentType | If given, restricts the included edges to components with the given type.
try {
    String result = apiInstance.subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling CorporaApi#subgraphForQuery");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the subgraph for. |
 **query** | **String**| The query which defines the nodes to include. |
 **queryLanguage** | [**QueryLanguage**](.md)|  | [optional]
 **componentTypeFilter** | [**AnnotationComponentType**](.md)| If given, restricts the included edges to components with the given type. | [optional]

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/xml

