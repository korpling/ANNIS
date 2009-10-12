/*
 * Copyright 2009 Collaborative Research Centre SFB 632 
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

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import annis.cache.Cache;
import annis.cache.FilesystemCache;
import annis.cache.CacheInitializationException;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.model.AnnisNode;
import annis.model.AnnotationGraph;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import annis.service.ifaces.AnnisToken;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import org.apache.commons.lang.StringUtils;

public class SearchResultServlet extends HttpServlet
{

  private static final long serialVersionUID = 7180460653219721099L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException
  {
    doGet(req, rep);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    HttpSession session = request.getSession();
    response.setCharacterEncoding("UTF-8");


    if(request.getParameter("count") != null)
    {
      writeCount(session, response);
    }
    else
    {
      writeJSONResult(session, request, response);
    }

  }

  private void writeCount(HttpSession session, HttpServletResponse response) throws IOException
  {
    response.setContentType("text/plain");

    ServletOutputStream out = response.getOutputStream();

    String queryAnnisQL = (String) session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL);
    List<Long> corpusIdList = (List<Long>) session.getAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);

    //Gather totalCount from AnnisRemoteService
    try
    {
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      int count = service.getCount(corpusIdList, queryAnnisQL);
      session.setAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT, new Integer(count));
      out.println(count);

    }
    catch(AnnisServiceFactoryException e)
    {
      this.cleanSession(session);
      out.println("ERROR: " + e.getMessage());
    }
    catch(AnnisCorpusAccessException e)
    {
      this.cleanSession(session);
      out.println("Please select a Corpus.");

    }
    catch(AnnisServiceException e)
    {
      this.cleanSession(session);
      out.println(e.getMessage());
    }


  }

  private void writeJSONResult(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    String queryAnnisQL = "";
    Integer totalCount = 0;
    List<Long> corpusIdList = (List<Long>) session.getAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);

    int offset = 0;
    int limit = 50;


    try
    {
      offset = Integer.parseInt(request.getParameter("start"));
    }
    catch(NumberFormatException e)
    {
      //ignore
      }
    try
    {
      limit = Integer.parseInt(request.getParameter("limit"));
    }
    catch(NumberFormatException e)
    {
      //ignore
    }


    /* Required Stuff */
    boolean scriptTag = false;
    String cb = request.getParameter("callback");
    if(cb != null)
    {
      scriptTag = true;
      response.setContentType("text/javascript");
    }
    else
    {
      response.setContentType("application/x-json");
    }
    Writer out = response.getWriter();
    if(scriptTag)
    {
      out.write(cb + "(");
    }
    /* END Required Stuff */

    //Gather Result Set from RMIService
    AnnisService service;
    try
    {
      queryAnnisQL = session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL).toString();

      service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      AnnisResultSet resultSet = service.getResultSet(corpusIdList, queryAnnisQL, limit, offset, (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT), (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT));
      Cache cacheAnnisResult = new FilesystemCache("AnnisResult");

      // check whether match count retrieval has finished
      try
      {
        if(session.getAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT) == null)
        {
          // total count not set yet, we use the limit for now
          totalCount = offset + limit;
        }
        else
        {
          totalCount = (Integer) session.getAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT);
        }

      }
      catch(NullPointerException e)
      {
        //ignore
      }
      catch(ClassCastException e)
      {
        //ignore
      }

      //NOW WE HAVE TO CREATE THE JSON OUTPUT FROM resultSet
      StringBuffer json = new StringBuffer();
      json.append("{'totalCount':" + totalCount + ", 'resultSet':[");

      //json.append("{'queryAnnisQL':'" + queryAnnisQL.replace("'", "\\'") + "', 'totalCount':" + totalCount + ", 'resultSet':[");
      int count = 0;
      for(AnnisResult result : resultSet)
      {
        // construct byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(bos);
        // serialize result
        objectOutput.writeObject(result);
        // copy to cache
        cacheAnnisResult.put(result.getStartNodeId() + "," + result.getEndNodeId(),
          bos.toByteArray());

        if(count++ != 0)
        {
          json.append("\n,");
        }
        String resultAsJSON = new AnnisResultToJSON(result).getJSON();
        json.append(resultAsJSON);
      }
      json.append("]}");
      out.append(json.toString());
    }
    catch(AnnisServiceFactoryException e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
    }
    catch(AnnisQLSemanticsException e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
    }
    catch(AnnisQLSyntaxException e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
    }
    catch(AnnisCorpusAccessException e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
    }
    catch(CacheInitializationException e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
    }
    catch(Exception e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, "ooops", e);
    }

    /* Required Stuff */
    if(scriptTag)
    {
      out.write(");");
    }
  /* END Required Stuff */
  }

  private void cleanSession(HttpSession session)
  {
    session.removeAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL);
    session.removeAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT);
    session.removeAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);
    session.removeAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT);
    session.removeAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT);
  }


  public class AnnisResultToJSON
  {
    private AnnisResult annisResult;
    private StringBuffer json;
    private AnnotationGraph graph;
    private Set<Long> idsToMark;

    public AnnisResultToJSON(AnnisResult result)
    {
      this.json = new StringBuffer();
      this.graph = result.getGraph();
      this.annisResult = result;

      this.idsToMark = getMarkedIDs();

      json.append("{'_id':'" + result.getStartNodeId() + ","
        + result.getEndNodeId() + "', '_textId': '"
        + getTextId() + "', '_text':'"
        + getText().replace("'", "\\'") + "'");

      //add annotation levels
      json.append(", '_levels': [");
      int c = 0;
      for(String level : result.getAnnotationLevelSet())
      {
        json.append(((c++ > 0) ? ", " : "") + "'" + level + "'");
      }
      json.append("]");

      //add a list of marked objects
      json.append(", '_markedObjects': [");
      c = 0;
      for(Long id : idsToMark)
      {
        if(c++ > 0)
        {
          json.append(", ");
        }
        json.append(id);
      }
      json.append("]");

      //add token annotation levels
      json.append(", '_tokenLevels': [");
      c = 0;
      for(String level : result.getTokenAnnotationLevelSet())
      {
        json.append(((c++ > 0) ? ", " : "") + "'" + level + "'");
      }
      json.append("]");

      int tokenCount = 0;

      List<AnnisToken> tokenList = result.getTokenList();
      // XXX: Tokens unterhalb eines markierten Knoten werden nicht weiter markiert
      int matchStart = 0;
      int matchEnd = tokenList.size() - 1;
//		int matchStart = tokenList.size() - 1, matchEnd = 0;

      long lastTokenIndex = -1;

      for(AnnisToken token : tokenList)
      {
        if(hasNodeMarker(token.getId()))
        {
//				if(tokenCount > matchEnd)
//					matchEnd = tokenCount;
//				if(tokenCount < matchStart)
//					matchStart = tokenCount;
        }
        
        if(lastTokenIndex == -1)
        {
          lastTokenIndex = token.getTokenIndex();
        }
        
        if(token.getTokenIndex() - lastTokenIndex > 1)
        {
          // insert empty token (...)
          json.append(",'" + tokenCount++ + "':{'_id': " + token.getId()
          + ", '_text':'"
          + "(...)"
          + "', '_marker':''" + ", '_corpusId':'"
          + token.getCorpusId() + "'");
          json.append("}");
        }
        else
        {

          String marker = hasNodeMarker(token.getId()) ? result.getMarkerId(token.getId()) : "";
          json.append(",'" + tokenCount++ + "':{'_id': " + token.getId()
            + ", '_text':'"
            + (token.getText() != null ? token.getText().replace("'", "\\'") : "")
            + "', '_marker':'" + marker + "'" + ", '_corpusId':'"
            + token.getCorpusId() + "'");
          for(Map.Entry<String, String> annotation : token.entrySet())
          {
            json.append(", '" + annotation.getKey() + "':'" + annotation.getValue().replace("'", "\\'") + "'");
          }
          json.append("}");
        }

        lastTokenIndex = token.getTokenIndex();
      }
      json.append(", '_matchStart' : '" + matchStart + "'");
      json.append(", '_matchEnd' : '" + matchEnd + "'");
      json.append("}");
    }

    public String getJSON()
    {
      return json.toString();
    }

  private Set<Long> getMarkedIDs()
  {
    Set<Long> matchedNodes =  graph.getMatchedNodeIds();
    Set<Long> matchedAndCovered = new HashSet<Long>(matchedNodes);
    // add all covered nodes
    for(AnnisNode n : graph.getNodes())
    {
      if(matchedNodes.contains(n.getId()))
      {
        long left = n.getLeftToken();
        long right = n.getRightToken();

        for(long i=left; i <= right; i++)
        {
          matchedAndCovered.add(graph.getToken(i).getId());
        }
      }
    }

    return matchedAndCovered;
  }

  private String getText()
  {
    List<String> tokenSpans = new ArrayList<String>();
    for(AnnisNode token : graph.getTokens())
    {
      tokenSpans.add(token.getSpannedText());
    }
    return StringUtils.join(tokenSpans, " ");
  }

  private String getTextId()
  {
    if(graph.getNodes().isEmpty())
    {
      return "1";
    }
    else
    {
      return String.valueOf(graph.getNodes().get(0).getTextId());
    }
  }

  private boolean hasNodeMarker(long id)
  {
    return idsToMark.contains(id);
  }

  }
}

