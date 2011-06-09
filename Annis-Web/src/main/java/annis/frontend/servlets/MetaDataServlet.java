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
import annis.model.Annotation;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Servlet that gives returns you metadata for a given token.
 * @author thomas
 */
public class MetaDataServlet extends HttpServlet
{

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    doGet(req, resp);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {

    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/x-json");

    OutputStreamWriter outWriter = new OutputStreamWriter(resp.getOutputStream(), "UTF-8");
    
    JSONWriter json = new JSONWriter(outWriter);
    
    String idAsString = req.getParameter("mID");
    if(idAsString != null)
    {
      try
      {
        long id = Long.parseLong(idAsString);
        List<Annotation> queryResult = createData(id);
        
        int size = 0;
        try
        {
          json.object();

          json.key("metadata");
          json.array();

          for(Annotation a : queryResult)
          {
            json.object();
            size++;
            json.key("key");
            json.value(a.getQualifiedName());
            json.key("value");
            json.value(a.getValue());

            json.endObject();
          }

          json.endArray();


          json.key("size");
          json.value(size);

          json.endObject();

        }
        catch(JSONException ex)
        {
          Logger.getLogger(MetaDataServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

      }
      catch(NumberFormatException ex)
      {
      }
    }
    outWriter.flush();
  }

  private List<Annotation> createData(long id)
  {
    List<Annotation> md = new LinkedList<Annotation>();

    try
    {
      AnnisService service = 
        AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));

      md = service.getMetadata(id);
    }
    catch(RemoteException ex)
    {
      Logger.getLogger(MetaDataServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch(AnnisServiceFactoryException ex)
    {
      Logger.getLogger(MetaDataServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    return md;
  }
}

