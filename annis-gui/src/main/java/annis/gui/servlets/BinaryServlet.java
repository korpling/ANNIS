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
import annis.service.objects.AnnisBinaryMetaData;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Servlet provides binary-files with a stream of partial-content. The
 * first GET-request is answered with the status-code 206 Partial Content.
 *
 * @author benjamin
 *
 */
public class BinaryServlet extends HttpServlet
{

  private final static Logger log = LoggerFactory.getLogger(BinaryServlet.class);

  private static final int MAX_LENGTH = 1024 * 1024; // max portion which is transfered over REST at once: 1MB

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
      Object annisServiceURLObject = session.getAttribute(
        AnnisBaseUI.WEBSERVICEURL_KEY);

      if (annisServiceURLObject == null || !(annisServiceURLObject instanceof String))
      {
        throw new ServletException(
          "AnnisWebService.URL was not set as init parameter in web.xml");
      }

      String annisServiceURL = (String) annisServiceURLObject;

      WebResource binaryRes = Helper.getAnnisWebResource(annisServiceURL,
        (AnnisUser) session.getAttribute(AnnisBaseUI.USER_KEY))
        .path("query").path("corpora")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
        .path(URLEncoder.encode(documentName, "UTF-8")).path("binary");

      WebResource metaBinaryRes = Helper.getAnnisWebResource(annisServiceURL,
        (AnnisUser) session.getAttribute(AnnisBaseUI.USER_KEY))
        .path("meta").path("binary")
        .path(URLEncoder.encode(toplevelCorpusName, "UTF-8"))
        .path(URLEncoder.encode(documentName, "UTF-8"));

      // tell client that we support byte ranges
      response.setHeader("Accept-Ranges", "bytes");

      if (range != null)
      {
        responseStatus206(metaBinaryRes, binaryRes, mimeType, out, response,
          range);
      }
      else
      {
        responseStatus200(metaBinaryRes, binaryRes, mimeType, out, response);
      }

      out.flush();
    }
    catch (IOException ex)
    {
      log.debug("IOException in BinaryServlet", ex);
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
    WebResource binaryRes, String mimeType, ServletOutputStream out,
    HttpServletResponse response, String range) throws RemoteException, IOException
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
      response.setContentLength(lengthToFetch);

      writeFromServiceToClient(offset, lengthToFetch, binaryRes, out, mimeType);
    }
  }

  private void responseStatus200(WebResource metaBinaryRes,
    WebResource binaryRes, String mimeType, ServletOutputStream out,
    HttpServletResponse response) throws RemoteException, IOException
  {


    List<AnnisBinaryMetaData> allMeta = metaBinaryRes.path("meta")
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
      response.setContentLength(binaryMeta.getLength());

      getCompleteFile(binaryRes, mimeType, out);
    }
  }

  private void getCompleteFile(WebResource binaryRes, String mimeType,
    ServletOutputStream out)
    throws RemoteException, IOException
  {

    List<AnnisBinaryMetaData> allMeta = binaryRes.path("meta")
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

      int offset = 0;
      int length = binaryMeta.getLength();

      writeFromServiceToClient(offset, length, binaryRes, out, mimeType);

    }
  }

  private void writeFromServiceToClient(int offset, int completeLength,
    WebResource binaryRes, ServletOutputStream out, String mimeType) throws IOException
  {

    ClientResponse response = binaryRes.path("" + offset).
      path("" + completeLength)
      .accept(mimeType).get(ClientResponse.class);
    int copiedBytes = IOUtils.copy(response.getEntityInputStream(), out);
    Validate.isTrue(copiedBytes == completeLength);
    out.flush();
  }

  private static class AnnisBinaryMetaDataListType extends GenericType<List<AnnisBinaryMetaData>>
  {

    public AnnisBinaryMetaDataListType()
    {
    }
  }
}
