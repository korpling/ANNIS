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

import annis.libgui.Helper;
import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.service.objects.AnnisBinary;
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  
  private final static Logger log = LoggerFactory.getLogger(BinaryServlet.class);

  private static final int MAX_LENGTH = 10*1024; // max portion which is transfered over REST at once
  
  @Override
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }

  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException
  {
    Map<String, String[]> binaryParameter = request.getParameterMap();
    String toplevelCorpusName = binaryParameter.get("toplevelCorpusName")[0];
    String documentName = binaryParameter.get("documentName")[0];
    String mimeType = binaryParameter.get("mime")[0];
    
    try
    {
      ServletOutputStream out = response.getOutputStream();

      String range = request.getHeader("Range");

      HttpSession session = request.getSession();
      Object annisServiceURLObject =  session.getAttribute(AnnisBaseUI.WEBSERVICEURL_KEY);
      
      if(annisServiceURLObject == null || !(annisServiceURLObject instanceof String))
      {
        throw new ServletException("AnnisWebService.URL was not set as init parameter in web.xml");
      }
      
      String annisServiceURL = (String) annisServiceURLObject;
      
      WebResource annisRes = Helper.getAnnisWebResource(annisServiceURL, 
        (AnnisUser) session.getAttribute(AnnisBaseUI.USER_KEY));
      
      WebResource binaryRes = annisRes.path("query").path("corpora")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
        .path(URLEncoder.encode(documentName, "UTF-8")).path("binary")
        .queryParam("mime", mimeType); 

      if (range != null)
      {
        responseStatus206(binaryRes, mimeType, out, response, range);
      }
      else
      {
        responseStatus200(binaryRes, mimeType, out, response);
      }
      
      out.flush();
    }
    catch(IOException ex)
    {
      log.debug("IOException in BinaryServlet", ex);
    }
    catch(ClientHandlerException ex)
    {
      log.error(null, ex);
      response.setStatus(500);
    }
    catch(UniformInterfaceException ex)
    {
      log.error(null, ex);
      response.setStatus(500);
    }
  }

  private void responseStatus206(WebResource binaryRes, String mimeType, ServletOutputStream out,
    HttpServletResponse response, String range) throws RemoteException, IOException
  {
    List<AnnisBinaryMetaData> allMeta = binaryRes.path("meta")
      .get(new AnnisBinaryMetaDataListType());

    if(allMeta.size() > 0 )
    {
      AnnisBinaryMetaData bm = allMeta.get(0);
      for(AnnisBinaryMetaData m : allMeta)
      {
        if(mimeType.equals(m.getMimeType()))
        {
          bm = m;
          break;
        }
      }


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
  }

  private void responseStatus200(WebResource binaryRes, String mimeType, ServletOutputStream out,
    HttpServletResponse response) throws RemoteException, IOException
  {
    
    
    List<AnnisBinaryMetaData> allMeta = binaryRes.path("meta")
      .get(new AnnisBinaryMetaDataListType());

    if(allMeta.size() > 0 )
    {
      AnnisBinaryMetaData binaryMeta = allMeta.get(0);
      for(AnnisBinaryMetaData m : allMeta)
      {
        if(mimeType.equals(m.getMimeType()))
        {
          binaryMeta = m;
          break;
        }
      }

      response.setStatus(200);
      response.setHeader("Accept-Ranges", "bytes");
      response.setContentType(binaryMeta.getMimeType());
      response.setHeader("Content-Range", "bytes 0-" + (binaryMeta.getLength() - 1)
        + "/" + binaryMeta.getLength());
      response.setContentLength(binaryMeta.getLength());

      getCompleteFile(binaryRes, out);
    }
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
      Validate.isTrue(bin.getBytes().length == stepLength);
      out.write(bin.getBytes());
      out.flush();
      
      offset += stepLength;      
      remaining = remaining - stepLength;
    }
  }

  private static class AnnisBinaryMetaDataListType extends GenericType<List<AnnisBinaryMetaData>>
  {

    public AnnisBinaryMetaDataListType()
    {
    }
  }
}
