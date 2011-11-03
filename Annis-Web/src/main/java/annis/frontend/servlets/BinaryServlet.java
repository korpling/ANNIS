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
package annis.frontend.servlets;

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisBinary;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import java.io.IOException;
import java.util.Map;

/**
 * This Servlet provides binary-files with a stream of partial-content. The
 * first GET-request is answered with the status-code 206 Partial Content.
 * 
 * @author benjamin
 * 
 */
public class BinaryServlet extends HttpServlet
{

  private static final long serialVersionUID = -8182635617256833563L;

  @SuppressWarnings("deprecation")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    int offset = 0;
    int length = 2047;

    Map<String, String[]> binaryParameter = request.getParameterMap();
    long corpusId = Long.parseLong(binaryParameter.get("id")[0]);
    ServletOutputStream out = response.getOutputStream();

    try
    {

      String range = request.getHeader("Range");
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));

      if (range != null)
      {

        responseStatus206(service, out, response, range, corpusId, offset,
                length);

      } else
      {

        responseStatus200(service, out, response, corpusId, offset, length);
      }
    } catch (AnnisServiceFactoryException e)
    {
      throw new RuntimeException(e.getMessage());
    }
  }

  private void responseStatus206(AnnisService service, ServletOutputStream out,
          HttpServletResponse response, String range, long corpusId, int offset,
          int length) throws RemoteException, IOException
  {

    AnnisBinary binary;

    // Range: byte=x-y | Range: byte=0-
    String[] rangeTupel = range.split("-");
    offset = Integer.parseInt(rangeTupel[0].split("=")[1]);
    binary = service.getBinary(corpusId, offset + 1, length + 1); //index shifting

    if (rangeTupel.length > 1)
    {
      length = Integer.parseInt(rangeTupel[1]);
    }

    response.setHeader("Content-Range", "bytes " + offset + "-" + (offset
            + length) + "/" + binary.getLength());
    response.setContentType(binary.getMimeType());
    response.setStatus(206);
    response.setContentLength(length+1);

    out.write(binary.getBytes());
    out.flush();

  }

  private void responseStatus200(AnnisService service, ServletOutputStream out,
          HttpServletResponse response, long corpusId, int offset, int length)
          throws RemoteException, IOException
  {
    AnnisBinary binary = service.getBinary(corpusId, offset + 1, length + 1);

    response.setStatus(200);
    response.setHeader("Accept-Ranges", "bytes");
    response.setContentType(binary.getMimeType());
    response.setContentLength(binary.getLength());

    getByteWise(service, out, corpusId);
  }

  /**
   * This function get the whole binary-file and put it to responds.out
   * there must exist at least one byte
   * 
   * 
   * @param service
   * @param out
   * @param corpusId 
   */
  private void getByteWise(AnnisService service, ServletOutputStream out, long corpusId) throws RemoteException, IOException
  {

    AnnisBinary annisBinary = service.getBinary(corpusId, 1, 1);
    int length = annisBinary.getLength();
    for (int i = 1; i <= length; i++)
    {
      out.write(service.getBinary(corpusId, i, 1).getBytes());
      out.flush();
    }

  }
}
