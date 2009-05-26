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
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;

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
      Cache cache = new FilesystemCache("Paula");

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
        cache.put(result.getStartNodeId() + "," + result.getEndNodeId(), result.getPaula());
        if(count++ != 0)
        {
          json.append("\n,");
        }
        json.append(result.getJSON());
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
}
