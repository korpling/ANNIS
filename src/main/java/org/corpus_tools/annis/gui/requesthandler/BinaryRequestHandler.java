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

package org.corpus_tools.annis.gui.requesthandler;

import com.google.common.base.Preconditions;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.corpus_tools.annis.gui.CommonUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
  private final CommonUI ui;

  public BinaryRequestHandler(String urlPrefix, CommonUI ui) {
    this.prefix = urlPrefix + "/Binary";
    this.ui = ui;
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
    WebClient client = ui.getWebClient();

    // Execute the call and return the response
    try {
      ResponseEntity<DataBuffer> proxyResponse = client.get()
          .uri("/corpora/{top}/files/{path}",
              toplevelCorpusName, filePath)
          .accept(MediaType.ALL).headers(httpHeaders -> {
            Enumeration<String> originalHeaderNames = request.getHeaderNames();
            while (originalHeaderNames.hasMoreElements()) {
              String headerName = originalHeaderNames.nextElement();
              String headerValue = request.getHeader(headerName);
              httpHeaders.set(headerName, headerValue);
            }
          }).retrieve().toEntity(DataBuffer.class).block();


      // Copy response headers
      for (String headerName : proxyResponse.getHeaders().keySet()) {
        List<String> headerValues = proxyResponse.getHeaders().getValuesAsList(headerName);
        if(!headerValues.isEmpty()) {
          response.setHeader(headerName, headerValues.get(0));
        }
      }
      // Copy response body
      response.setStatus(proxyResponse.getStatusCodeValue());
      IOUtils.copy(proxyResponse.getBody().asInputStream(), response.getOutputStream());
    } catch (WebClientResponseException e) {
      log.error("Could not get binary data from service", e);
      response.sendError(e.getStatusCode().value(), e.getMessage());
    }
  }

}
