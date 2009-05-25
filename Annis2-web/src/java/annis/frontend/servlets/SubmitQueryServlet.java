package annis.frontend.servlets;

import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisServiceFactoryException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import annis.security.AnnisUser;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;

/**
 * This Servlet recieves an ANNIS query encoded in GET parameters. These Parameters are.
 * <ol>
 * 	<li></li>
 * </ol>
 * 
 * 
 * @author Karsten Huetter
 *
 */
public class SubmitQueryServlet extends HttpServlet
{

  private static final long serialVersionUID = -4188886565776492022L;
  public static final String KEY_QUERY_ANNIS_QL = "SEARCH_queryAnnisQL";
  public static final String KEY_TOTAL_COUNT = "SEARCH_totalCount";
  public static final String KEY_CORPUS_ID_LIST = "SEARCH_corpusIdList";
  public static final String KEY_CONTEXT_LEFT = "SEARCH_padLeft";
  public static final String KEY_CONTEXT_RIGHT = "SEARCH_padRight";
  public static final String KEY_USER = "user";
  public static final String PARAM_CORPUS_ID = "corpusIds";
  public static final String PARAM_ANNIS_QL = "queryAnnisQL";
  public static final String PARAM_CONTEXT_LEFT = "padLeft";
  public static final String PARAM_CONTEXT_RIGHT = "padRight";


  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    HttpSession session = request.getSession();
    
    String queryAnnisQL = request.getParameter(PARAM_ANNIS_QL);
    String corpusIdsParam = request.getParameter(PARAM_CORPUS_ID);

    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/plain");

    int contextLeft = 0;
    int contextRight = 0;

    try
    {
      contextLeft = Integer.parseInt(request.getParameter(PARAM_CONTEXT_LEFT));
    }
    catch(NumberFormatException e)
    {
      //ignore
    }

    try
    {
      contextRight = Integer.parseInt(request.getParameter(PARAM_CONTEXT_RIGHT));
    }
    catch(NumberFormatException e)
    {
      //ignore
    }

    List<Long> corpusIdList = new Vector<Long>();

    //gather selected corpora
    try
    {
      for(String corpusId : corpusIdsParam.split(","))
      {
        corpusIdList.add(Long.parseLong(corpusId));
      }
    }
    catch(NullPointerException e)
    {
      //We got no corpus Ids...
    }
    catch(NumberFormatException e)
    {
      //We got wrong Corpus Ids
    }

    //intersect corpusidlist with allowed corpora
    AnnisUser user = (AnnisUser) request.getSession().getAttribute(KEY_USER);
    corpusIdList.retainAll(user.getCorpusIdList());

    
    //Gathering values from last call
    String oldQueryAnnisQL = (String) session.getAttribute(KEY_QUERY_ANNIS_QL);
    List<Long> oldCorpusIdList = (List<Long>) session.getAttribute(KEY_CORPUS_ID_LIST);

    //Set all parameters that do not need checking
    session.setAttribute(KEY_CONTEXT_LEFT, new Integer(contextLeft));
    session.setAttribute(KEY_CONTEXT_RIGHT, new Integer(contextRight));

    
    //We only contact the backend in case
    //1: Query has changed
    //2: The Corpus List has changed
    if(!queryAnnisQL.equals(oldQueryAnnisQL) || !corpusIdList.equals(oldCorpusIdList))
    {
      // preserve last query and corpus ID list
      session.setAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL, queryAnnisQL);
      session.setAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST, corpusIdList);

      // unset total count to make clear that we need new data
      session.removeAttribute(KEY_TOTAL_COUNT);

    }
    
  }
}
