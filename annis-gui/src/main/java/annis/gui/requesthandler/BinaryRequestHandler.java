/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package annis.gui.requesthandler;

import annis.gui.UIConfig;
import annis.libgui.Helper;
import com.google.common.base.Preconditions;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Pair;
import org.corpus_tools.annis.api.CorporaApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This request handler provides binary-files. It will proxy all requests to the REST service. Since
 * the REST service is supposed to support content range queries, this API does as well.
 * 
 * @author Thomas Krause {@literal <krauseto@hu-berlin.de>}
 * @author benjamin
 */
public class BinaryRequestHandler implements RequestHandler {


  /**
   * 
   */
  private static final long serialVersionUID = -3570539208345869659L;

  private final static Logger log = LoggerFactory.getLogger(BinaryRequestHandler.class);



  private final String prefix;
  private final UIConfig config;

  public BinaryRequestHandler(String urlPrefix, UIConfig config) {
    this.prefix = urlPrefix + "/Binary";
    this.config = config;
  }

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
      VaadinResponse response) throws IOException {
    if (request.getPathInfo() != null && request.getPathInfo().startsWith(prefix)) {
      if ("GET".equalsIgnoreCase(request.getMethod())) {
        sendResponse(session, request, response, true);
        return true;
      } else if ("HEAD".equalsIgnoreCase(request.getMethod())) {
        sendResponse(session, request, response, false);
        return true;
      }
    }
    return false;
  }

  public void sendResponse(VaadinSession session, VaadinRequest request,
      VaadinResponse pureResponse, boolean sendContent) throws IOException {
    if (!(pureResponse instanceof VaadinServletResponse)) {
      pureResponse.sendError(500, "Binary requests only work with servlets");
    }

    VaadinServletResponse response = (VaadinServletResponse) pureResponse;

    Map<String, String[]> binaryParameter = request.getParameterMap();
    String toplevelCorpusName = binaryParameter.get("toplevelCorpusName")[0];
    String[] filePathRaw = binaryParameter.get("file");
    Preconditions.checkNotNull(filePathRaw, "No file path given (parameter \"file\"");
    Preconditions.checkArgument(filePathRaw.length > 0, "No file path given (parameter \"file\"");

    String filePath = filePathRaw[0];

    // Proxy the whole request, including any HTTP headers (e.g. used for range requests) to the
    // REST endpoint.

    CorporaApi api = new CorporaApi(Helper.getClient(session, config));
    ApiClient client = api.getApiClient();
    // create path and map variables
    String localVarPath = "/corpora/{corpus}/files/{name}"
        .replaceAll("\\{" + "corpus" + "\\}", client.escapeString(toplevelCorpusName))
        .replaceAll("\\{" + "name" + "\\}", client.escapeString(filePath));

    List<Pair> localVarQueryParams = new ArrayList<Pair>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<Pair>();
    Map<String, String> localVarHeaderParams = new HashMap<String, String>();
    Map<String, String> localVarCookieParams = new HashMap<String, String>();
    Map<String, Object> localVarFormParams = new HashMap<String, Object>();

    Enumeration<String> originalHeaderNames = request.getHeaderNames();
    while (originalHeaderNames.hasMoreElements()) {
      String headerName = originalHeaderNames.nextElement();
      String headerValue = request.getHeader(headerName);
      localVarHeaderParams.put(headerName, headerValue);
    }

    final String[] localVarAccepts = {"default"};
    final String localVarAccept = client.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    final String[] localVarContentTypes = {};
    final String localVarContentType = client.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"bearerAuth"};

    // Execute the call and return the response
    Call call;
    try {
      call = client.buildCall(localVarPath, "GET", localVarQueryParams,
          localVarCollectionQueryParams, null, localVarHeaderParams, localVarCookieParams,
          localVarFormParams, localVarAuthNames, null);

      Response proxyResponse = call.execute();

      // Copy response headers
      for (String headerName : proxyResponse.headers().names()) {
        response.setHeader(headerName, proxyResponse.header(headerName));
      }
      // Copy response body
      response.setStatus(proxyResponse.code());
      IOUtils.copy(proxyResponse.body().byteStream(), response.getOutputStream());
    } catch (ApiException e) {
      log.error("Could not get binary data from service", e);
      response.sendError(e.getCode(), e.getMessage());
    }
  }

}
