package org.corpus_tools.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.api.CorporaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

public class PatchedCorporaApi extends CorporaApi {

  @Autowired
  public PatchedCorporaApi(ApiClient apiClient) {
    super(apiClient);
  }

  public Mono<List<String>> listCorporaAsMono() throws WebClientResponseException {
    ParameterizedTypeReference<List<String>> localVarReturnType =
        new ParameterizedTypeReference<List<String>>() {};
    return listCorporaRequestCreation().bodyToMono(localVarReturnType);
  }

  /**
   * Get a list of all corpora the user is authorized to use.
   * 
   * <p>
   * <b>200</b> - OK
   * 
   * @return List&lt;String&gt;
   * @throws WebClientResponseException if an error occurs while attempting to invoke the API
   */
  private ResponseSpec listCorporaRequestCreation() throws WebClientResponseException {
    Object postBody = null;
    // create path and map variables
    final Map<String, Object> pathParams = new HashMap<String, Object>();

    final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
    final HttpHeaders headerParams = new HttpHeaders();
    final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<String, String>();
    final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

    final String[] localVarAccepts = {"application/json"};
    final List<MediaType> localVarAccept = getApiClient().selectHeaderAccept(localVarAccepts);
    final String[] localVarContentTypes = {};
    final MediaType localVarContentType =
        getApiClient().selectHeaderContentType(localVarContentTypes);

    String[] localVarAuthNames = new String[] {"bearerAuth"};

    ParameterizedTypeReference<String> localVarReturnType =
        new ParameterizedTypeReference<String>() {};
    return getApiClient().invokeAPI("/corpora", HttpMethod.GET, pathParams, queryParams, postBody,
        headerParams, cookieParams, formParams, localVarAccept, localVarContentType,
        localVarAuthNames, localVarReturnType);
  }
}
