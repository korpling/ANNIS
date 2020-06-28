/*
 * graphANNIS
 * Access the graphANNIS corpora and execute AQL queries with this service. 
 *
 * The version of the OpenAPI document: 0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package org.corpus_tools.api;

import org.corpus_tools.ApiCallback;
import org.corpus_tools.ApiClient;
import org.corpus_tools.ApiException;
import org.corpus_tools.ApiResponse;
import org.corpus_tools.Configuration;
import org.corpus_tools.Pair;
import org.corpus_tools.ProgressRequestBody;
import org.corpus_tools.ProgressResponseBody;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;


import org.corpus_tools.annis.api.model.InlineObject1;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthentificationApi {
    private ApiClient localVarApiClient;

    public AuthentificationApi() {
        this(Configuration.getDefaultApiClient());
    }

    public AuthentificationApi(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return localVarApiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.localVarApiClient = apiClient;
    }

    /**
     * Build call for localLogin
     * @param inlineObject1  (required)
     * @param _callback Callback for upload/download progress
     * @return Call to execute
     * @throws ApiException If fail to serialize the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Returns a JSON Web Token (JWT), valid until its expiration time is reached. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call localLoginCall(InlineObject1 inlineObject1, final ApiCallback _callback) throws ApiException {
        Object localVarPostBody = inlineObject1;

        // create path and map variables
        String localVarPath = "/local-login";

        List<Pair> localVarQueryParams = new ArrayList<Pair>();
        List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
        Map<String, String> localVarHeaderParams = new HashMap<String, String>();
        Map<String, String> localVarCookieParams = new HashMap<String, String>();
        Map<String, Object> localVarFormParams = new HashMap<String, Object>();
        final String[] localVarAccepts = {
            "text/plain"
        };
        final String localVarAccept = localVarApiClient.selectHeaderAccept(localVarAccepts);
        if (localVarAccept != null) {
            localVarHeaderParams.put("Accept", localVarAccept);
        }

        final String[] localVarContentTypes = {
            "application/json"
        };
        final String localVarContentType = localVarApiClient.selectHeaderContentType(localVarContentTypes);
        localVarHeaderParams.put("Content-Type", localVarContentType);

        String[] localVarAuthNames = new String[] { "bearerAuth" };
        return localVarApiClient.buildCall(localVarPath, "POST", localVarQueryParams, localVarCollectionQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAuthNames, _callback);
    }

    @SuppressWarnings("rawtypes")
    private okhttp3.Call localLoginValidateBeforeCall(InlineObject1 inlineObject1, final ApiCallback _callback) throws ApiException {
        
        // verify the required parameter 'inlineObject1' is set
        if (inlineObject1 == null) {
            throw new ApiException("Missing the required parameter 'inlineObject1' when calling localLogin(Async)");
        }
        

        okhttp3.Call localVarCall = localLoginCall(inlineObject1, _callback);
        return localVarCall;

    }

    /**
     * Create JWT token for credentials of a locally configured account.
     * 
     * @param inlineObject1  (required)
     * @return String
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Returns a JSON Web Token (JWT), valid until its expiration time is reached. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
     </table>
     */
    public String localLogin(InlineObject1 inlineObject1) throws ApiException {
        ApiResponse<String> localVarResp = localLoginWithHttpInfo(inlineObject1);
        return localVarResp.getData();
    }

    /**
     * Create JWT token for credentials of a locally configured account.
     * 
     * @param inlineObject1  (required)
     * @return ApiResponse&lt;String&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Returns a JSON Web Token (JWT), valid until its expiration time is reached. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
     </table>
     */
    public ApiResponse<String> localLoginWithHttpInfo(InlineObject1 inlineObject1) throws ApiException {
        okhttp3.Call localVarCall = localLoginValidateBeforeCall(inlineObject1, null);
        Type localVarReturnType = new TypeToken<String>(){}.getType();
        return localVarApiClient.execute(localVarCall, localVarReturnType);
    }

    /**
     * Create JWT token for credentials of a locally configured account. (asynchronously)
     * 
     * @param inlineObject1  (required)
     * @param _callback The callback to be executed when the API call finishes
     * @return The request call
     * @throws ApiException If fail to process the API call, e.g. serializing the request body object
     * @http.response.details
     <table summary="Response Details" border="1">
        <tr><td> Status Code </td><td> Description </td><td> Response Headers </td></tr>
        <tr><td> 200 </td><td> Returns a JSON Web Token (JWT), valid until its expiration time is reached. </td><td>  -  </td></tr>
        <tr><td> 401 </td><td> Unauthorized </td><td>  -  </td></tr>
     </table>
     */
    public okhttp3.Call localLoginAsync(InlineObject1 inlineObject1, final ApiCallback<String> _callback) throws ApiException {

        okhttp3.Call localVarCall = localLoginValidateBeforeCall(inlineObject1, _callback);
        Type localVarReturnType = new TypeToken<String>(){}.getType();
        localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
        return localVarCall;
    }
}
