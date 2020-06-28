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
> CountExtra count(countQuery)

Count the number of results for a query.

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.SearchApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    SearchApi apiInstance = new SearchApi(defaultClient);
    CountQuery countQuery = new CountQuery(); // CountQuery | The definition of the query to execute.
    try {
      CountExtra result = apiInstance.count(countQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SearchApi#count");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **countQuery** | [**CountQuery**](CountQuery.md)| The definition of the query to execute. |

### Return type

[**CountExtra**](CountExtra.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The extended count result. |  -  |

<a name="find"></a>
# **find**
> String find(findQuery)

Find results for a query and return the IDs of the matched nodes.

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.SearchApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    SearchApi apiInstance = new SearchApi(defaultClient);
    FindQuery findQuery = new FindQuery(); // FindQuery | The definition of the query to execute.
    try {
      String result = apiInstance.find(findQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SearchApi#find");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **findQuery** | [**FindQuery**](FindQuery.md)| The definition of the query to execute. |

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: text/plain

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The matches for the given query. |  -  |

<a name="frequency"></a>
# **frequency**
> List&lt;FrequencyTableRow&gt; frequency(frequencyQuery)

Find results for a query and return the IDs of the matched nodes.

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.SearchApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    SearchApi apiInstance = new SearchApi(defaultClient);
    FrequencyQuery frequencyQuery = new FrequencyQuery(); // FrequencyQuery | The definition of the query to execute.
    try {
      List<FrequencyTableRow> result = apiInstance.frequency(frequencyQuery);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SearchApi#frequency");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **frequencyQuery** | [**FrequencyQuery**](FrequencyQuery.md)| The definition of the query to execute. |

### Return type

[**List&lt;FrequencyTableRow&gt;**](FrequencyTableRow.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Frequency of different annotation values as table |  -  |

<a name="subgraphForQuery"></a>
# **subgraphForQuery**
> String subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter)

Get a subgraph of the corpus format given a list of nodes and a context.

This only includes the nodes that are the result of the given query and no context is created automatically. The annotation graph also includes all edges between the included nodes. 

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.SearchApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    SearchApi apiInstance = new SearchApi(defaultClient);
    String corpus = GUM; // String | The name of the corpus to get the subgraph for.
    String query = annis:node_type="corpus"; // String | The query which defines the nodes to include.
    QueryLanguage queryLanguage = new QueryLanguage(); // QueryLanguage | 
    AnnotationComponentType componentTypeFilter = new AnnotationComponentType(); // AnnotationComponentType | If given, restricts the included edges to components with the given type.
    try {
      String result = apiInstance.subgraphForQuery(corpus, query, queryLanguage, componentTypeFilter);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling SearchApi#subgraphForQuery");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **corpus** | **String**| The name of the corpus to get the subgraph for. |
 **query** | **String**| The query which defines the nodes to include. |
 **queryLanguage** | [**QueryLanguage**](.md)|  | [optional] [default to AQL] [enum: AQL, AQLQuirksV3]
 **componentTypeFilter** | [**AnnotationComponentType**](.md)| If given, restricts the included edges to components with the given type. | [optional] [enum: Coverage, Dominance, Pointing, Ordering, LeftToken, RightToken, PartOf]

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/xml

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | The subgraph in the GraphML format with the graphANNIS data model. |  -  |

