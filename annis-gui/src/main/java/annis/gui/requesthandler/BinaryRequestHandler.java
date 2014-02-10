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

import annis.libgui.Helper;
import annis.service.objects.AnnisBinaryMetaData;
import com.google.common.base.Preconditions;
import static com.google.common.base.Preconditions.checkNotNull;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
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

  private static final int BUFFER_SIZE = 0x1000; //4K
  
  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if(request.getPathInfo().startsWith("/Binary"))
    {
      if("GET".equalsIgnoreCase(request.getMethod()))
      {
        sendResponse(session, request, response, true);
        return true;
      }
      else if("HEAD".equalsIgnoreCase(request.getMethod()))
      {
        sendResponse(session, request, response, false);
        return true;
      }
    }
    return false;
  }
  
  public void sendResponse(VaadinSession session, VaadinRequest request, VaadinResponse pureResponse, 
    boolean sendContent) throws IOException
  {
    if(!(pureResponse instanceof VaadinServletResponse))
    {
      pureResponse.sendError(500, "Binary requests only work with servlets");
    }
    
    VaadinServletResponse response = (VaadinServletResponse) pureResponse;
    
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
      // always set the buffer size to the same one we will use for coyping, 
      // otherwise we won't notice any client disconnection
      response.reset();
      response.setCacheTime(-1);
      response.resetBuffer();
      response.setBufferSize(BUFFER_SIZE); // 4K
      
      OutputStream out = response.getOutputStream();

      String requestedRangeRaw = request.getHeader("Range");

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
      
      AnnisBinaryMetaData meta = getMatchingMetadataFromService(metaBinaryRes,
        mimeType);
      if(meta == null)
      {
        response.sendError(404, "Binary file not found");
        return;
      }
      
      ContentRange fullRange = new ContentRange(0,
        meta.getLength()-1, meta.getLength());
      
      ContentRange r = fullRange;
      try
      {
        if(requestedRangeRaw != null)
        {
          List<ContentRange> requestedRanges = ContentRange.parseFromHeader(
            requestedRangeRaw, meta.getLength(), 1);

          if(!requestedRanges.isEmpty())
          {
            r = requestedRanges.get(0);
          }
        }
       
        long contentLength = (r.getEnd() - r.getStart()+1);

        boolean useContentRange = !fullRange.equals(r);
        
        response.setContentType(meta.getMimeType());
        if(useContentRange)
        {
          response.setHeader("Content-Range", r.toString());
        }
        response.setContentLength((int) contentLength);
        response.setStatus(useContentRange  ? 206 : 200);
        
        response.flushBuffer();
        if(sendContent)
        {
          writeFromServiceToClient(
            r.getStart(), contentLength, binaryRes, out, mimeType);
        }
        
        out.close();

      }
      catch(ContentRange.InvalidRangeException ex)
      {
        response.setHeader("Content-Range", "bytes */" + meta.getLength());
        response.sendError(416, "Requested range not satisfiable: " + ex.getMessage());
        return;
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
  
  private AnnisBinaryMetaData getMatchingMetadataFromService(
    WebResource metaBinaryRes, String mimeType)
  {
    List<AnnisBinaryMetaData> allMeta = metaBinaryRes.get(
      new AnnisBinaryMetaDataListType());

    AnnisBinaryMetaData bm = allMeta.get(0);
    for (AnnisBinaryMetaData m : allMeta)
    {
      if (mimeType != null && mimeType.equals(m.getMimeType()))
      {
        bm = m;
        break;
      }
    }
    return bm;
  }
  

  private void writeFromServiceToClient(long offset, long length,
    WebResource binaryRes, OutputStream out, String mimeType)
  {

    InputStream entityStream = null;
    try
    {
      ClientResponse response = binaryRes.path("" + offset).
        path("" + length)
        .accept(mimeType).get(ClientResponse.class);
      
      entityStream = response.getEntityInputStream();
      
      long copiedBytes = copy(entityStream, out);
      Validate.isTrue(copiedBytes == length, "only copied " + copiedBytes + " bytes instead of " + length);
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
        { // always close the entity stream in order to free resources at the service
          entityStream.close();
        }
        catch (IOException ex)
        {
          log.error(null, ex);
        }
      }
    }
  }
  
  private static long copy(InputStream from, OutputStream to)
      throws IOException {
    checkNotNull(from);
    checkNotNull(to);
    byte[] buf = new byte[BUFFER_SIZE];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      to.flush();
      total += r;
    }
    return total;
  }

  private static class AnnisBinaryMetaDataListType extends GenericType<List<AnnisBinaryMetaData>>
  {

    public AnnisBinaryMetaDataListType()
    {
    }
  }
  
}
