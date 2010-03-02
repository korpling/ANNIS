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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import annis.cache.Cache;
import annis.cache.FilesystemCache;
import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisServiceFactoryException;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchResultServlet extends HttpServlet
{

  public static final String FILESYSTEM_CACHE_RESULT = "FileSystemCacheResult";
  private static final long serialVersionUID = 7180460653219721099L;
  private Random rand = new Random();

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


    if (request.getParameter("count") != null)
    {
      writeCount(session, response);
    }
    else
    {
      writeJSONResult(session, request, response);
    }
  }

  private void writeJSONResult(HttpSession session, HttpServletRequest request, HttpServletResponse response)
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
    catch (NumberFormatException e)
    {
      //ignore
    }
    try
    {
      limit = Integer.parseInt(request.getParameter("limit"));
    }
    catch (NumberFormatException e)
    {
      //ignore
    }

    //Gather Result Set from RMIService
    AnnisService service;
    try
    {
      queryAnnisQL = session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL).toString();

      service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      AnnisResultSet resultSet = service.getResultSet(corpusIdList, queryAnnisQL, limit, offset, (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT), (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT));


      if (session.getAttribute(FILESYSTEM_CACHE_RESULT) == null)
      {
        Cache newCache = new FilesystemCache("AnnisResult");
        session.setAttribute(FILESYSTEM_CACHE_RESULT, newCache);
      }
      Cache cacheAnnisResult = (Cache) session.getAttribute(FILESYSTEM_CACHE_RESULT);

      // check whether match count retrieval has finished
      try
      {
        if (session.getAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT) == null)
        {
          // total count not set yet, we use the limit for now
          totalCount = offset + limit;
        }
        else
        {
          totalCount = (Integer) session.getAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT);
        }
      }
      catch (NullPointerException e)
      {
        //ignore
      }
      catch (ClassCastException e)
      {
        //ignore
      }


      //NOW WE HAVE TO CREATE THE JSON OUTPUT FROM resultSet
      JSONObject root = new JSONObject();

      root.putOnce("totalCount", totalCount);
      LinkedList<JSONObject> jsonResultSet = new LinkedList<JSONObject>();
      for (AnnisResult r : resultSet)
      {
        long generatedID = Math.abs(rand.nextLong());
        // construct byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(bos);
        // serialize result
        objectOutput.writeObject(r);
        // copy to cache
        cacheAnnisResult.put("" + generatedID,
          bos.toByteArray());

        // create json
        JSONObject jsonResult = jsonFromAnnisResult(r);
        jsonResult.putOnce("callbackId", "" + generatedID);
        jsonResultSet.add(jsonResult);

      }
      root.putOnce("resultSet", jsonResultSet);

      response.getWriter().append(root.toString(2));

      response.setContentType("application/x-json");
    }
    catch (Exception e)
    {
      Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, "ooops", e);
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
    catch (AnnisServiceFactoryException e)
    {
      this.cleanSession(session);
      out.println("ERROR: " + e.getMessage());
    }
    catch (AnnisCorpusAccessException e)
    {
      this.cleanSession(session);
      out.println("Please select a Corpus.");

    }
    catch (AnnisServiceException e)
    {
      this.cleanSession(session);
      out.println(e.getMessage());
    }


  }

  private void cleanSession(HttpSession session)
  {
    session.removeAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL);
    session.removeAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT);
    session.removeAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);
    session.removeAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT);
    session.removeAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT);
  }

  public JSONObject jsonFromAnnisResult(AnnisResult result) throws JSONException
  {
    JSONObject json = new JSONObject();

    json.putOnce("tokenNamespaces", result.getTokenAnnotationLevelSet());
    HashSet<String> visSet = new HashSet<String>();
    LinkedList<JSONObject> visusalizer = new LinkedList<JSONObject>();

    for (String annoName : result.getAnnotationLevelSet())
    {
      String[] splitted = annoName.split(":");
      if (splitted.length > 0)
      {
        if(!visSet.contains(splitted[0]))
        {
          visSet.add(splitted[0]);
          JSONObject visEntry = new JSONObject();
          visEntry.putOnce("id", splitted[0]);
          visEntry.putOnce("name", splitted[0]);
          visusalizer.add(visEntry);
        }
      }
    }

    // for data debugging
    JSONObject visEntryPaula = new JSONObject();
    visEntryPaula.putOnce("id", "paula");
    visEntryPaula.putOnce("name", "Paula");
    JSONObject visEntryPaulaText = new JSONObject();
    visEntryPaulaText.putOnce("id", "paulatext");
    visEntryPaulaText.putOnce("name", "Paula Text");

    visusalizer.add(visEntryPaula);
    visusalizer.add(visEntryPaulaText);

    json.putOnce("visualizer", visusalizer);

    Set<Long> markedIDs = getMarkedIDs(result.getGraph());
    
    LinkedList<JSONObject> tokenList = new LinkedList<JSONObject>();
    for (AnnisNode n : result.getGraph().getTokens())
    {
      // put first match textId into result
      if(!json.has("textId") && markedIDs.contains(n.getId()))
      {
        json.putOnce("textId", "" + n.getTextId());
      }

      // put first match corpusId into result
      if(!json.has("corpusId") && markedIDs.contains(n.getId()))
      {
        json.putOnce("corpusId", "" + n.getCorpus());
      }

      JSONObject tok = new JSONObject();
      tok.putOnce("id", "" + n.getId());
      tok.putOnce("textId", "" + n.getTextId());
      tok.putOnce("corpusId", "" + n.getCorpus());
      tok.putOnce("tokenIndex", n.getTokenIndex());

      tok.putOnce("text", n.getSpannedText());

      TreeMap<String, JSONObject> annotations = new TreeMap<String, JSONObject>();
      for (Annotation a : n.getNodeAnnotations())
      {
        JSONObject jsonAnno = new JSONObject();
        jsonAnno.putOnce("namespace", a.getNamespace());
        jsonAnno.putOnce("name", a.getName());
        jsonAnno.putOnce("value", a.getValue());
        annotations.put(a.getQualifiedName(), jsonAnno);
      }
      tok.putOnce("annotations", annotations);

      tok.putOnce("marker", markedIDs.contains(n.getId()) ? "red" : "");

      tokenList.add(tok);

    }

    json.putOnce("token", tokenList);

    return json;
  }

  private Set<Long> getMarkedIDs(AnnotationGraph graph)
  {
    Set<Long> matchedNodes = graph.getMatchedNodeIds();
    Set<Long> matchedAndCovered = new HashSet<Long>(matchedNodes);
    // add all covered nodes
    for (AnnisNode n : graph.getNodes())
    {
      if (matchedNodes.contains(n.getId()))
      {
        long left = n.getLeftToken();
        long right = n.getRightToken();

        for (long i = left; i <= right; i++)
        {
          matchedAndCovered.add(graph.getToken(i).getId());
        }
      }
    }

    return matchedAndCovered;
  }
}

