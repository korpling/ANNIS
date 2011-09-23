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

import annis.model.AnnisNode;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GeneralTextExporter extends HttpServlet
{

  private static final long serialVersionUID = -8182635617256833563L;

  @SuppressWarnings("unchecked")
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    // BEGIN getting the needed parameters from the query
    HttpSession session = request.getSession();

    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");

    /** The selected corpora */
    List<Long> corpusIdList = new LinkedList<Long>();
    /** The AQL query which should be executed */
    String queryAnnisQL = checkAndGetMandatoryStringParam("query", request);

    // the left an right context size
    int contextLeft = checkAndGetMandatoryIntParam("context_left", request);
    int contextRight = checkAndGetMandatoryIntParam("context_right", request);

    // get a "real" list of corpora from the comma-delimited parameter
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
    // END getting the needed parameters from the query

    try
    {
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter(
        "AnnisRemoteService.URL"));

      // int count = service.getCount(corpusIdList, queryAnnisQL);
      AnnisResultSet queryResult = null;

      LinkedList<String> keys = new LinkedList<String>();

      if(request.getParameter("keys") == null)
      {
        // auto set
        keys.add("tok");
        AnnisAttributeSet attributes =
          service.getAttributeSet(corpusIdList, false, false);
        for(AnnisAttribute a : attributes)
        {
          if(a.getName() != null)
          {
            String[] namespaceAndName = a.getName().split(":", 2);
            if(namespaceAndName.length > 1)
            {
              keys.add(namespaceAndName[1]);
            }
            else
            {
              keys.add(namespaceAndName[0]);
            }
          }
        }
      }
      else
      {
        // manually specified
        String[] keysSplitted = request.getParameter("keys").split("\\,");
        for(String k : keysSplitted)
        {
          keys.add(k.trim());
        }
      }
      int offset = 0;
      while(offset == 0 || (queryResult != null && queryResult.size() > 0))
      {

        queryResult = service.getResultSet(corpusIdList, queryAnnisQL, 50, offset, contextLeft, contextRight);


        convertText(queryResult, keys, request.getParameterMap(), response, offset);

        response.getWriter().flush();
        offset = offset + 50;

      }

      response.getWriter().println();
      response.getWriter().println("finished");

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

  public void convertText(AnnisResultSet queryResult, List<String> keys, Map<String, String[]> httpArgs, HttpServletResponse response,
    int offset) throws IOException
  {
    int counter = 0;
    for(AnnisResult annisResult : queryResult)
    {
      Set<Long> matchedNodeIds = annisResult.getGraph().getMatchedNodeIds();

      counter++;
      response.getWriter().append((counter + offset) + ". ");
      List<AnnisNode> tok = annisResult.getGraph().getTokens();

      for(AnnisNode annisNode : tok)
      {
        Long tokID = annisNode.getId();
        if(matchedNodeIds.contains(tokID))
        {
          response.getWriter().append("[");
          response.getWriter().append(annisNode.getSpannedText());
          response.getWriter().append("]");
        }
        else
        {
          response.getWriter().append(annisNode.getSpannedText());
        }

        //for (Annotation annotation : annisNode.getNodeAnnotations()){
        //      response.getWriter().append("/"+annotation.getValue());
        //}

        response.getWriter().append(" ");

      }
      response.getWriter().append("\n");
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
