# SearchApi

All URIs are relative to *http://localhost:5711/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**count**](SearchApi.md#count) | **POST** /search/count | Count the number of results for a query.
[**find**](SearchApi.md#find) | **POST** /search/find | Find results for a query and return the IDs of the matched nodes.
[**frequency**](SearchApi.md#frequency) | **POST** /search/frequency | Find results for a query and return the IDs of the matched nodes.
[**subgraphForQuery**](SearchApi.md#subgraphForQuery) | **GET** /corpora/{corpus}/subgraph-for-query | Get a subgraph of the corpus format given a list of nodes and a context.

<a name="count"></a>
# **count**
> CountExtra count(body)

Count the number of results for a query.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.SearchApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


SearchApi apiInstance = new SearchApi();
CountQuery body = new CountQuery(); // CountQuery | The definition of the query to execute.
try {
    CountExtra result = apiInstance.count(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SearchApi#count");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**CountQuery**](CountQuery.md)| The definition of the query to execute. |

### Return type

[**CountExtra**](CountExtra.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="find"></a>
# **find**
> String find(body)

Find results for a query and return the IDs of the matched nodes.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.SearchApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


SearchApi apiInstance = new SearchApi();
FindQuery body = new FindQuery(); // FindQuery | The definition of the query to execute.
try {
    String result = apiInstance.find(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SearchApi#find");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**FindQuery**](FindQuery.md)| The definition of the query to execute. |

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: text/plain

<a name="frequency"></a>
# **frequency**
> FrequencyTable frequency(body)

Find results for a query and return the IDs of the matched nodes.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.SearchApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


SearchApi apiInstance = new SearchApi();
FrequencyQuery body = new FrequencyQuery(); // FrequencyQuery | The definition of the query to execute.
try {
    FrequencyTable result = apiInstance.frequency(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SearchApi#frequency");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**FrequencyQuery**](FrequencyQuery.md)| The definition of the query to execute. |

### Return type

[**FrequencyTable**](FrequencyTable.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

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
//import org.corpus_tools.annis.SearchApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


SearchApi apiInstance = new SearchApi();
String corpus = "corpus_example"; // String | The name of the corpus to get the subgraph for.
String query = "query_example"; // String | The query which defines the nodes to include.
QueryLanguage queryLanguage = new QueryLanguage(); // QueryLanguage | 
AnnotationComponentType componentTypeFilter = new AnnotationComponentType(); // AnnotationComponentType | If given, restricts the included edges to components with the given type.
try {
    String result = apiInstance.subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling SearchApi#subgraphForQuery");
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

