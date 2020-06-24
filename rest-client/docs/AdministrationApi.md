# AdministrationApi

All URIs are relative to *http://localhost:5711/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteGroup**](AdministrationApi.md#deleteGroup) | **DELETE** /groups/{name} | Delete the user group given by its name
[**exportPost**](AdministrationApi.md#exportPost) | **POST** /export | Get all requested corpora as ZIP-file
[**getJob**](AdministrationApi.md#getJob) | **GET** /jobs/{uuid} | Get the status of the background job with the UUID
[**importPost**](AdministrationApi.md#importPost) | **POST** /import | Import all corpora which are part of the uploaded ZIP-file
[**listGroups**](AdministrationApi.md#listGroups) | **GET** /groups | Get all available user groups
[**putGroup**](AdministrationApi.md#putGroup) | **PUT** /groups/{name} | Add or replace the user group given by its name

<a name="deleteGroup"></a>
# **deleteGroup**
> deleteGroup(name)

Delete the user group given by its name

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
String name = "name_example"; // String | 
try {
    apiInstance.deleteGroup(name);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#deleteGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **String**|  |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="exportPost"></a>
# **exportPost**
> InlineResponse202 exportPost(body)

Get all requested corpora as ZIP-file

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
Body body = new Body(); // Body | 
try {
    InlineResponse202 result = apiInstance.exportPost(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#exportPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body**](Body.md)|  |

### Return type

[**InlineResponse202**](InlineResponse202.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="getJob"></a>
# **getJob**
> getJob(uuid)

Get the status of the background job with the UUID

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
String uuid = "uuid_example"; // String | 
try {
    apiInstance.getJob(uuid);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#getJob");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **uuid** | **String**|  |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="importPost"></a>
# **importPost**
> InlineResponse202 importPost(body, overrideExisting)

Import all corpora which are part of the uploaded ZIP-file

This will search for all GraphML and relANNIS files in the uploaded ZIP file and imports them.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
Object body = null; // Object | 
Boolean overrideExisting = false; // Boolean | If true, existing corpora will be overwritten by the uploaded ones.
try {
    InlineResponse202 result = apiInstance.importPost(body, overrideExisting);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#importPost");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | **Object**|  |
 **overrideExisting** | **Boolean**| If true, existing corpora will be overwritten by the uploaded ones. | [optional] [default to false]

### Return type

[**InlineResponse202**](InlineResponse202.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/octet-stream
 - **Accept**: application/json

<a name="listGroups"></a>
# **listGroups**
> List&lt;Group&gt; listGroups()

Get all available user groups

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
try {
    List<Group> result = apiInstance.listGroups();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#listGroups");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Group&gt;**](Group.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="putGroup"></a>
# **putGroup**
> putGroup(body, name)

Add or replace the user group given by its name

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AdministrationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AdministrationApi apiInstance = new AdministrationApi();
Group body = new Group(); // Group | The group to add
String name = "name_example"; // String | 
try {
    apiInstance.putGroup(body, name);
} catch (ApiException e) {
    System.err.println("Exception when calling AdministrationApi#putGroup");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Group**](Group.md)| The group to add |
 **name** | **String**|  |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

