# AuthentificationApi

All URIs are relative to *http://localhost:5711/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**localLogin**](AuthentificationApi.md#localLogin) | **POST** /local-login | Create JWT token for credentials of a locally configured account.


<a name="localLogin"></a>
# **localLogin**
> String localLogin(inlineObject1)

Create JWT token for credentials of a locally configured account.

### Example
```java
// Import classes:
import org.corpus_tools.ApiClient;
import org.corpus_tools.ApiException;
import org.corpus_tools.Configuration;
import org.corpus_tools.auth.*;
import org.corpus_tools.models.*;
import org.corpus_tools.api.AuthentificationApi;

public class Example {
  public static void main(String[] args) {
    ApiClient defaultClient = Configuration.getDefaultApiClient();
    defaultClient.setBasePath("http://localhost:5711/v0");
    
    // Configure HTTP bearer authorization: bearerAuth
    HttpBearerAuth bearerAuth = (HttpBearerAuth) defaultClient.getAuthentication("bearerAuth");
    bearerAuth.setBearerToken("BEARER TOKEN");

    AuthentificationApi apiInstance = new AuthentificationApi(defaultClient);
    InlineObject1 inlineObject1 = new InlineObject1(); // InlineObject1 | 
    try {
      String result = apiInstance.localLogin(inlineObject1);
      System.out.println(result);
    } catch (ApiException e) {
      System.err.println("Exception when calling AuthentificationApi#localLogin");
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
 **inlineObject1** | [**InlineObject1**](InlineObject1.md)|  |

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
**200** | Returns a JSON Web Token (JWT), valid until its expiration time is reached. |  -  |
**401** | Unauthorized |  -  |

