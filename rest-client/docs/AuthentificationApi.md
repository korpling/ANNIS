# AuthentificationApi

All URIs are relative to *http://localhost:5711/v0*

Method | HTTP request | Description
------------- | ------------- | -------------
[**localLogin**](AuthentificationApi.md#localLogin) | **POST** /local-login | Create JWT token for credentials of a locally configured account.

<a name="localLogin"></a>
# **localLogin**
> String localLogin(body)

Create JWT token for credentials of a locally configured account.

### Example
```java
// Import classes:
//import org.corpus_tools.ApiClient;
//import org.corpus_tools.ApiException;
//import org.corpus_tools.Configuration;
//import org.corpus_tools.auth.*;
//import org.corpus_tools.annis.AuthentificationApi;

ApiClient defaultClient = Configuration.getDefaultApiClient();


AuthentificationApi apiInstance = new AuthentificationApi();
Body1 body = new Body1(); // Body1 | Object with the user ID and password to login with
try {
    String result = apiInstance.localLogin(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling AuthentificationApi#localLogin");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Body1**](Body1.md)| Object with the user ID and password to login with |

### Return type

**String**

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: text/plain

