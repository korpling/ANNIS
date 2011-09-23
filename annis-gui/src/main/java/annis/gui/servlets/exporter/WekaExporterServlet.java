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
package annis.gui.servlets.exporter;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.exceptions.AnnisServiceFactoryException;
import javax.servlet.ServletException;
import javax.servlet.http.*;


import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class WekaExporterServlet extends HttpServlet
{

  private static final long serialVersionUID = -8182635617256833563L;

  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    HttpSession session = request.getSession();

    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    //this is a full result export
    List<Long> corpusIdList = new LinkedList<Long>();
    String queryAnnisQL = checkAndGetMandatoryStringParam("query", request);
    String corpusListAsString = checkAndGetMandatoryStringParam("corpora", request);
    
    for(String corpusId : corpusListAsString.split(","))
    {
      try
      {
        corpusIdList.add(Long.parseLong(corpusId));
      }
      catch(NumberFormatException ex)
      {
        // ignore
      }
    }

    try
    {
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      response.getWriter().print(service.getWeka(corpusIdList, queryAnnisQL));
    }
    catch(AnnisServiceFactoryException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(AnnisQLSemanticsException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(AnnisQLSyntaxException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(AnnisCorpusAccessException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  private String checkAndGetMandatoryStringParam(String name, HttpServletRequest request)
  {
    String result = request.getParameter(name);
    if(result == null)
    {
      throw new NullPointerException("Parameter '" + name + "' must no be null.");
    }
    return result;
  }

  private int checkAndGetMandatoryIntParam(String name, HttpServletRequest request)
  {
    String asString = checkAndGetMandatoryStringParam(name, request);

    try
    {
      return Integer.parseInt(asString);
    }
    catch(NumberFormatException ex)
    {
      throw new NumberFormatException("Could not cast the parameter '" + name
        + "' to an integer (parameter value was '" + asString + "')");
    }
  }
}
