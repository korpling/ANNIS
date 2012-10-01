/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.servlets;

import annis.provider.SaltProjectProvider;
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This Servlet provides binary-files with a stream of partial-content. The
 * first GET-request is answered with the status-code 206 Partial Content.
 *
 * TODO: handle more than one byte-range TODO: split rmi-requests TODO: wrote
 * tests TODO:
 *
 * @author benjamin
 *
 */
public class BinaryServlet extends HttpServlet
{

  private static final int MAX_LENGTH = 50*1024; // max portion which is transfered over REST at once
  private String toplevelCorpusName;
  private String documentName;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {

    // get Parameter from url, actually it' s only the corpusId
    Map<String, String[]> binaryParameter = request.getParameterMap();
    toplevelCorpusName = binaryParameter.get("toplevelCorpusName")[0];
    documentName = binaryParameter.get("documentName")[0];
    ServletOutputStream out = response.getOutputStream();


    String range = request.getHeader("Range");

    ClientConfig rc = new DefaultClientConfig(SaltProjectProvider.class);
    Client c = Client.create(rc);
        
    String annisServiceURL = getServletContext().getInitParameter("AnnisWebService.URL");
    if(annisServiceURL == null)
    {
      throw new ServletException("AnnisWebService.URL was not set as init parameter in web.xml");
    }
    WebResource annisRes = c.resource(annisServiceURL);
    
    WebResource binaryRes = annisRes.path("corpora")
      .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
      .path(URLEncoder.encode(documentName, "UTF-8")).path("binary"); 
    
    if (range != null)
    {
      responseStatus206(binaryRes, out, response, range);
    }
    else
    {
      responseStatus200(binaryRes, out, response);
    }

    out.flush();
    out.close();
  }

  private void responseStatus206(WebResource binaryRes, ServletOutputStream out,
    HttpServletResponse response, String range) throws RemoteException, IOException
  {
    AnnisBinaryMetaData bm = binaryRes.path("meta")
      .get(AnnisBinary.class);

    // Range: byte=x-y | Range: byte=0-
    String[] rangeTupel = range.split("-");
    int offset = Integer.parseInt(rangeTupel[0].split("=")[1]);

    int slice;
    if (rangeTupel.length > 1)
    {
      slice = Integer.parseInt(rangeTupel[1]);
    }
    else
    {
      slice = bm.getLength();
    }
    
    int lengthToFetch = slice - offset;
    
    response.setHeader("Content-Range", "bytes " + offset + "-"
      + (bm.getLength() - 1) + "/" + bm.getLength());
    response.setContentType(bm.getMimeType());
    response.setStatus(206);
    response.setContentLength(lengthToFetch);

    writeStepByStep(offset, lengthToFetch, binaryRes, out);
    
  }

  private void responseStatus200(WebResource binaryRes, ServletOutputStream out,
    HttpServletResponse response) throws RemoteException, IOException
  {
    
    
    AnnisBinaryMetaData binaryMeta = binaryRes.path("meta")
      .get(AnnisBinary.class);

    response.setStatus(200);
    response.setHeader("Accept-Ranges", "bytes");
    response.setContentType(binaryMeta.getMimeType());
    response.setHeader("Content-Range", "bytes 0-" + (binaryMeta.getLength() - 1)
      + "/" + binaryMeta.getLength());
    response.setContentLength(binaryMeta.getLength());

    getCompleteFile(binaryRes, out);
  }

  /**
   * This function get the whole binary-file and put it to responds.out there
   * must exist at least one byte
   *
   *
   * @param service
   * @param out
   * @param corpusId
   */
  private void getCompleteFile(WebResource binaryRes, ServletOutputStream out)
    throws RemoteException, IOException
  {

    AnnisBinaryMetaData annisBinary = binaryRes.path("meta")
      .get(AnnisBinary.class);
    
    int offset = 0;
    int length = annisBinary.getLength();
    
    writeStepByStep(offset, length, binaryRes, out);

  }
  
  private void writeStepByStep(int offset, int completeLength, WebResource binaryRes, ServletOutputStream out) throws IOException
  {
    int remaining = completeLength;
    while(remaining > 0)
    {
      int stepLength = Math.min(MAX_LENGTH, remaining);
      
      AnnisBinary bin = binaryRes.path("" + offset).path("" + stepLength).get(AnnisBinary.class);
      out.write(bin.getBytes());
      
      offset += stepLength;      
      remaining = remaining - stepLength;
    }
  }
}
