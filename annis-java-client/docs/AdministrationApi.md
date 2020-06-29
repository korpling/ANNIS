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
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    String name = academic; // String | 
    try {
      apiInstance.deleteGroup(name);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#deleteGroup");
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
 **name** | **String**|  |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Group was deleted successfully. |  -  |
**403** | Access is forbidden if the requesting client does not have administrator privileges. |  -  |

<a name="exportPost"></a>
# **exportPost**
> InlineResponse202 exportPost(inlineObject)

Get all requested corpora as ZIP-file

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    InlineObject inlineObject = new InlineObject(); // InlineObject | 
    try {
      InlineResponse202 result = apiInstance.exportPost(inlineObject);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#exportPost");
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
 **inlineObject** | [**InlineObject**](InlineObject.md)|  |

### Return type

[**InlineResponse202**](InlineResponse202.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**202** | Corpus export started. Returns a UUID for the background job which can be used with the &#x60;/jobs&#x60; endpoint |  -  |

<a name="getJob"></a>
# **getJob**
> getJob(uuid)

Get the status of the background job with the UUID

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    String uuid = "uuid_example"; // String | 
    try {
      apiInstance.getJob(uuid);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#getJob");
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
 **uuid** | **String**|  |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**202** | Job is still running |  -  |
**200** | Job was finished successfully and result can be downloaded from the body |  -  |
**303** | Job was finished successfully |  -  |
**410** | Job failed |  -  |
**404** | Job not found |  -  |

<a name="importPost"></a>
# **importPost**
> InlineResponse202 importPost(body, overrideExisting)

Import all corpora which are part of the uploaded ZIP-file

This will search for all GraphML and relANNIS files in the uploaded ZIP file and imports them.

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    File body = new File("/path/to/file"); // File | 
    Boolean overrideExisting = false; // Boolean | If true, existing corpora will be overwritten by the uploaded ones.
    try {
      InlineResponse202 result = apiInstance.importPost(body, overrideExisting);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#importPost");
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
 **body** | **File**|  |
 **overrideExisting** | **Boolean**| If true, existing corpora will be overwritten by the uploaded ones. | [optional] [default to false]

### Return type

[**InlineResponse202**](InlineResponse202.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/octet-stream
 - **Accept**: application/json

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**202** | Corpus import started. Returns a UUID for the background job which can be used with the &#x60;/jobs&#x60; endpoint |  -  |

<a name="listGroups"></a>
# **listGroups**
> List&lt;Group&gt; listGroups()

Get all available user groups

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    try {
      List<Group> result = apiInstance.listGroups();
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#listGroups");
      System.err.println("Status code: " + e.getCode());
      System.err.println("Reason: " + e.getResponseBody());
      System.err.println("Response headers: " + e.getResponseHeaders());
      e.printStackTrace();
    }
  }
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

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | All available user groups with their name and the corpora the user is allowed to access. |  -  |
**403** | Access is forbidden if the requesting client does not have administrator privileges. |  -  |

<a name="putGroup"></a>
# **putGroup**
> putGroup(name, group)

Add or replace the user group given by its name

### Example
```java
// Import classes:
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.auth.*;
import org.corpus_tools.annis.models.*;
import org.corpus_tools.annis.api.AdministrationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AdministrationApi apiInstance = new AdministrationApi(defaultClient);
    String name = academic; // String | 
    Group group = new Group(); // Group | The group to add
    try {
      apiInstance.putGroup(name, group);
    } catch (ApiException e) {
      System.err.println("Exception when calling AdministrationApi#putGroup");
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
 **name** | **String**|  |
 **group** | [**Group**](Group.md)| The group to add |

### Return type

null (empty response body)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
**200** | Group was added or replaced successfully. |  -  |
**403** | Access is forbidden if the requesting client does not have administrator privileges. |  -  |

