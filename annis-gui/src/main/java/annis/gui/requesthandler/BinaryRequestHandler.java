/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package annis.gui.requesthandler;

import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import annis.service.objects.AnnisBinaryMetaData;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This request handler provides binary-files with a stream of partial-content. 
 * The first GET-request is answered with the status-code 206 Partial Content.
 * 
 * @author Thomas Krause <krauseto@hu-berlin.de>
 * @author benjamin
 */
public class BinaryRequestHandler implements RequestHandler
{
  
  private final static Logger log = LoggerFactory.getLogger(BinaryRequestHandler.class);

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if(request.getPathInfo().startsWith("/Binary") 
      && "GET".equalsIgnoreCase(request.getMethod()))
    {
      doGet(session, request, response);
      return true;
    }
    return false;
  }
  
  public void doGet(VaadinSession session, VaadinRequest request, VaadinResponse response)
  {
    Map<String, String[]> binaryParameter = request.getParameterMap();
    String toplevelCorpusName = binaryParameter.get("toplevelCorpusName")[0];
    String documentName = binaryParameter.get("documentName")[0];
    
    
    String mimeType = null;
    if(binaryParameter.containsKey("mime"))
    {
      mimeType = binaryParameter.get("mime")[0];
    }
    try
    {
      OutputStream out = response.getOutputStream();

      String range = request.getHeader("Range");

      WebResource binaryRes = Helper.getAnnisWebResource()
        .path("query").path("corpora")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
        .path(URLEncoder.encode(documentName, "UTF-8")).path("binary");

      WebResource metaBinaryRes = Helper.getAnnisWebResource()
        .path("meta").path("binary")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
        .path(URLEncoder.encode(documentName, "UTF-8"));

      // tell client that we support byte ranges
      response.setHeader("Accept-Ranges", "bytes");

      Preconditions.checkNotNull(mimeType, "No mime type given (parameter \"mime\"");
      
      if (range != null)
      {
        responseStatus206(metaBinaryRes, binaryRes, mimeType, out, response,
          range);
      }
      else
      {
        responseStatus200(metaBinaryRes, binaryRes, mimeType, out, response);
      }
    }
    catch (IOException ex)
    {
      log.error("IOException in BinaryRequestHandler", ex);
      response.setStatus(500);
    }
    catch (ClientHandlerException ex)
    {
      log.error(null, ex);
      response.setStatus(500);
    }
    catch (UniformInterfaceException ex)
    {
      log.error(null, ex);
      response.setStatus(500);
    }
  }
  
  private void responseStatus206(WebResource metaBinaryRes,
    WebResource binaryRes, String mimeType, OutputStream out,
    VaadinResponse response, String range) throws RemoteException
  {
    List<AnnisBinaryMetaData> allMeta = metaBinaryRes.get(
      new AnnisBinaryMetaDataListType());

    if (allMeta.size() > 0)
    {
      AnnisBinaryMetaData bm = allMeta.get(0);
      for (AnnisBinaryMetaData m : allMeta)
      {
        if (mimeType.equals(m.getMimeType()))
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
      response.setHeader("Content-Length", "" + lengthToFetch);

      writeFromServiceToClient(offset, lengthToFetch, binaryRes, out, mimeType);
    }
  }

  private void responseStatus200(WebResource metaBinaryRes,
    WebResource binaryRes, String mimeType, OutputStream out,
    VaadinResponse response) throws RemoteException, IOException
  {


    List<AnnisBinaryMetaData> allMeta = metaBinaryRes
      .get(new AnnisBinaryMetaDataListType());

    if (allMeta.size() > 0)
    {
      AnnisBinaryMetaData binaryMeta = allMeta.get(0);
      for (AnnisBinaryMetaData m : allMeta)
      {
        if (mimeType.equals(m.getMimeType()))
        {
          binaryMeta = m;
          break;
        }
      }

      response.setStatus(200);
      response.setHeader("Accept-Ranges", "bytes");
      response.setContentType(binaryMeta.getMimeType());
      response.setHeader("Content-Range",
        "bytes 0-" + (binaryMeta.getLength() - 1)
        + "/" + binaryMeta.getLength());
      response.setHeader("Content-Length", "" + binaryMeta.getLength());

      int offset = 0;
      int length = binaryMeta.getLength();

      writeFromServiceToClient(offset, length, binaryRes, out, mimeType);
    }
  }

  private void writeFromServiceToClient(int offset, int completeLength,
    WebResource binaryRes, OutputStream out, String mimeType)
  {

    InputStream entityStream = null;
    try
    {
      ClientResponse response = binaryRes.path("" + offset).
        path("" + completeLength)
        .accept(mimeType).get(ClientResponse.class);
      
      entityStream = response.getEntityInputStream();
      
      int copiedBytes = IOUtils.copy(entityStream, out);
      Validate.isTrue(copiedBytes == completeLength);
      out.flush();
    }
    catch(IOException ex)
    {
      log.debug("writing to client failed", ex);
    }
    finally
    {
      if(entityStream != null)
      {
        try
        {
          // always close the entity stream in order to free resources at the service
          entityStream.close();
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }
    }
    
  }

  private static class AnnisBinaryMetaDataListType extends GenericType<List<AnnisBinaryMetaData>>
  {

    public AnnisBinaryMetaDataListType()
    {
    }
  }
  
}
