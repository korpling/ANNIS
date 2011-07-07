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

import java.io.IOException;
import java.rmi.RemoteException;
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
import annis.frontend.filters.AuthenticationFilter;
import annis.model.AnnisNode;
import annis.model.Annotation;
import annis.model.AnnotationGraph;
import annis.model.Edge;
import annis.resolver.ResolverEntry;
import annis.resolver.ResolverEntry.ElementType;
import annis.resolver.SingleResolverRequest;
import annis.security.AnnisUser;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessResourceFailureException;

public class SearchResultServlet extends HttpServlet
{

  public static final String FILESYSTEM_CACHE_RESULT = "FileSystemCacheResult";
  private static final long serialVersionUID = 7180460653219721099L;
  private Random rand = new Random();

  private HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>> cacheResolver;

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
      writeCount(session, request, response);
    }
    else
    {
      writeJSONResult(session, request, response);
    }
  }

  private void writeJSONResult(HttpSession session, HttpServletRequest request, HttpServletResponse response)
  {
    cacheResolver = new HashMap<HashSet<SingleResolverRequest>, List<ResolverEntry>>();
    String queryAnnisQL = "";
    Integer totalCount = 0;
    List<Long> corpusIdList = (List<Long>) session.getAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);

    int offset = 0;
    int limit = 50;

    if(!checkCorpora(response, request, corpusIdList))
    {
      return;
    }

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

      int contextLeft = (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT);
      int contextRight = (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT);

      queryAnnisQL = session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL).toString();

      service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      AnnisResultSet resultSet = service.getResultSet(corpusIdList, queryAnnisQL, limit, offset, contextLeft, contextRight);


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

        ResolverEntry[] resolverEntries = getResolverEntries(r, service);

        // construct byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutput = new ObjectOutputStream(bos);
        
        // serialize AnnisResult
        objectOutput.writeObject(r);

        // also put the resolver entries in the cache file
        objectOutput.writeObject(resolverEntries);

        // copy to cache
        cacheAnnisResult.put("" + generatedID,
          bos.toByteArray());

        // create json
        JSONObject jsonResult = jsonFromAnnisResult(r, resolverEntries);
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

  private void writeCount(HttpSession session, HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    response.setContentType("text/plain");

    ServletOutputStream out = response.getOutputStream();

    String queryAnnisQL = (String) session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL);
    List<Long> corpusIdList = (List<Long>) session.getAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);

    if(!checkCorpora(response, request, corpusIdList))
    {
      return;
    }
    
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
      Throwable secondLevelCause = e.getCause();
      if(secondLevelCause != null && secondLevelCause.getCause() != null && secondLevelCause.getCause() instanceof DataAccessResourceFailureException)
      {
        out.println("Timeout or connection error");
      }
      else
      {
        out.println(e.getMessage());
      }
      response.setStatus(504);
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

  private JSONObject jsonFromAnnisResult(AnnisResult result, ResolverEntry[] visArray) throws JSONException, RemoteException
  {
    JSONObject json = new JSONObject();

    json.putOnce("tokenNamespaces", result.getTokenAnnotationLevelSet());

    LinkedList<JSONObject> visusalizer = new LinkedList<JSONObject>();
    
    for(int i=0; i < visArray.length; i++)
    {
      ResolverEntry e = visArray[i];
      JSONObject visEntry = new JSONObject();
      visEntry.putOnce("id", i);
      visEntry.putOnce("namespace", e.getNamespace());
      visEntry.putOnce("displayname", e.getDisplayName());
      visEntry.putOnce("vistype", e.getVisType());
      visusalizer.add(visEntry);
    }

    json.putOnce("visualizer", visusalizer);

    Map<AnnisNode,Long> markedAndCoveredNodes = calculateMarkedAndCoveredIDs(result.getGraph());

    HashSet<Long> textIdListFromResult = new HashSet<Long>();
    LinkedList<JSONObject> tokenList = new LinkedList<JSONObject>();
    for (AnnisNode n : result.getGraph().getTokens())
    {
      // put first match corpus id into result
      if(!json.has("corpusId") && markedAndCoveredNodes.containsKey(n))
      {
        json.putOnce("corpusId", "" + n.getCorpus());
      }
      
      if(!json.has("documentName"))
    	  json.putOnce("documentName", result.getDocumentName());
      
      textIdListFromResult.add(n.getTextId());
      
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
      tokenList.add(tok);

    }

    Map<String,String> markedAndCoveredAsMap = new TreeMap<String, String>();
    Map<String,String> markedExactAsMap = new TreeMap<String, String>();


    for(AnnisNode n : markedAndCoveredNodes.keySet())
    {
      Long matchNodeInQuery = markedAndCoveredNodes.get(n);
      if(matchNodeInQuery != null)
      {
        long pos =  matchNodeInQuery;
        int color = Math.min((int) (pos-1), (MatchedNodeColors.values().length-1));
        color = Math.max(color, 0);
        markedAndCoveredAsMap.put("" + n.getId(), MatchedNodeColors.values()[color].name());


        if(result.getGraph().getMatchedNodeIds().contains(n.getId()))
        {
          markedExactAsMap.put("" + n.getId(), MatchedNodeColors.values()[color].name());
        }
      }
    }

    json.putOnce("marker", markedAndCoveredAsMap);
    json.putOnce("markerExact", markedExactAsMap);
    json.putOnce("textIdList", textIdListFromResult);
    json.putOnce("token", tokenList);

    return json;
  }

  private ResolverEntry[] getResolverEntries(AnnisResult result, AnnisService service) throws RemoteException
  {
    HashSet<ResolverEntry> visSet = new HashSet<ResolverEntry>();
    
    long corpusIdFromFirstNode = result.getGraph().getNodes().get(0).getCorpus();

    // create a request for resolver entries
    HashSet<SingleResolverRequest> resolverRequests = new HashSet<SingleResolverRequest>();

    Set<String> nodeNamespaces = new HashSet<String>();
    for(AnnisNode node : result.getGraph().getNodes())
    {
      nodeNamespaces.add(node.getNamespace());
      for(Annotation annotation : node.getNodeAnnotations())
      {
        nodeNamespaces.add(annotation.getNamespace());
      }
    }
    Set<String> edgeNamespaces = new HashSet<String>();
    for(Edge e : result.getGraph().getEdges())
    {
      edgeNamespaces.add(e.getNamespace());
      for(Annotation annotation : e.getAnnotations())
      {
        edgeNamespaces.add(annotation.getNamespace());
      }
    }
    for(String ns : nodeNamespaces)
    {
      resolverRequests.add(new SingleResolverRequest(corpusIdFromFirstNode, ns, ElementType.node));
    }
    for(String ns : edgeNamespaces)
    {
      resolverRequests.add(new SingleResolverRequest(corpusIdFromFirstNode, ns, ElementType.edge));
    }

    // query with this resolver request and make sure it is unique
    if(cacheResolver.containsKey(resolverRequests))
    {
      visSet.addAll(cacheResolver.get(resolverRequests));
    }
    else
    {
      List<ResolverEntry> resolverList =
        service.getResolverEntries(resolverRequests.toArray(new SingleResolverRequest[0]));
      visSet.addAll(resolverList);
      cacheResolver.put(resolverRequests, resolverList);
    }
    // sort everything
    ResolverEntry[] visArray = visSet.toArray(new ResolverEntry[0]);
    Arrays.sort(visArray, new Comparator<ResolverEntry>()
    {

      @Override
      public int compare(ResolverEntry o1, ResolverEntry o2)
      {
        if(o1.getOrder() < o2.getOrder())
        {
          return -1;
        }
        else if(o1.getOrder() > o2.getOrder())
        {
          return 1;
        }
        else
        {
          return 0;
        }
      }

    });
    return visArray;
  }

  private Map<AnnisNode,Long> calculateMarkedAndCoveredIDs(AnnotationGraph graph)
  {
    Set<Long> matchedNodes = graph.getMatchedNodeIds();
    Map<AnnisNode,Long> matchedAndCovered = new HashMap<AnnisNode, Long>();

    // add all covered nodes
    for (AnnisNode n : graph.getNodes())
    {
      if (matchedNodes.contains(n.getId()) && n.getMatchedNodeInQuery() != null)
      {
        Long matchPosition = n.getMatchedNodeInQuery();
        matchedAndCovered.put(n,matchPosition);

        long left = n.getLeftToken();
        long right = n.getRightToken();

        for (long i = left; i <= right; i++)
        {
          AnnisNode tok = graph.getToken(i);
          Long oldTokenPosition = matchedAndCovered.get(tok);
          if(oldTokenPosition == null 
            || (!matchedNodes.contains(tok.getId()) && matchPosition.compareTo(oldTokenPosition) >= 0) )
          {
            matchedAndCovered.put(tok, matchPosition);
          }
        }
      }
    }

    return matchedAndCovered;
  }

  private boolean checkCorpora(HttpServletResponse response, HttpServletRequest request ,List<Long> corpusIdList)
  {
    AnnisUser user = ((AnnisUser) request.getSession().getAttribute(AuthenticationFilter.KEY_USER));

    Set<Long> allowedCorpora = new HashSet<Long>(user.getCorpusIdList());
    if(!allowedCorpora.containsAll(corpusIdList))
    {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      try
      {
        response.getWriter().append("You are not allowed to access all of these corpora: " + corpusIdList.toString());
      }
      catch (IOException ex)
      {
        Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, ex);
      }
      return false;
    }

    return true;
  }
}

