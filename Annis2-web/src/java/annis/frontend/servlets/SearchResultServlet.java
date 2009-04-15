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
import annisservice.exceptions.AnnisCorpusAccessException;
import annisservice.exceptions.AnnisQLSemanticsException;
import annisservice.exceptions.AnnisQLSyntaxException;
import annisservice.ifaces.AnnisResultSet;
import annisservice.ifaces.AnnisResult;
import annisservice.AnnisService;
import annisservice.AnnisServiceFactory;
import annisservice.exceptions.AnnisServiceFactoryException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchResultServlet extends HttpServlet {
	private static final long serialVersionUID = 7180460653219721099L;

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException
  {
    doGet(req, rep);
  }

  
  
	@SuppressWarnings("unchecked")
  @Override
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
		HttpSession session = request.getSession();

    response.setCharacterEncoding("UTF-8");
		
		String queryAnnisQL = "";
		Integer totalCount = 0;
		List<Long> corpusIdList = (List<Long>) session.getAttribute(SubmitQueryServlet.KEY_CORPUS_ID_LIST);
			
		int offset = 0;
		int limit = 50;
				
		try {
			queryAnnisQL = session.getAttribute(SubmitQueryServlet.KEY_QUERY_ANNIS_QL).toString();
			totalCount 	 = (Integer) session.getAttribute(SubmitQueryServlet.KEY_TOTAL_COUNT);
		} catch(NullPointerException e) {
			//ignore
		} catch(ClassCastException e) {
			//ignore
		}
		
		try {
			offset = Integer.parseInt(request.getParameter("start"));
		} catch (NumberFormatException e) {
			//ignore
		}
		try {
			limit = Integer.parseInt(request.getParameter("limit"));
		} catch (NumberFormatException e) {
			//ignore
		}
		
		
		/* Required Stuff */
		boolean scriptTag = false;
		String cb = request.getParameter("callback");
		if (cb != null) {
		    scriptTag = true;
		    response.setContentType("text/javascript");
		} else {
		    response.setContentType("application/x-json");
		}
		Writer out = response.getWriter();
		if (scriptTag) {
		    out.write(cb + "(");
		}
		/* END Required Stuff */
		
		//Gather Result Set from RMIService 
		AnnisService service;
		try {
			service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
			AnnisResultSet resultSet = service.getResultSet(corpusIdList, queryAnnisQL, limit, offset, (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT), (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT));
			Cache cache  = new FilesystemCache("Paula");
			//NOW WE HAVE TO CREATE THE JSON OUTPUT FROM resultSet
			StringBuffer json = new StringBuffer();
			json.append("{'totalCount':" + totalCount + ", 'resultSet':[");

      //json.append("{'queryAnnisQL':'" + queryAnnisQL.replace("'", "\\'") + "', 'totalCount':" + totalCount + ", 'resultSet':[");
			int count=0;
			for(AnnisResult result : resultSet) {
        cache.put(result.getStartNodeId() + "," + result.getEndNodeId(), result.getPaula());
				if(count++ != 0)
					json.append("\n,");
				json.append(result.getJSON());
			}
			json.append("]}");
			out.append(json.toString());
		} catch (AnnisServiceFactoryException e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
		} catch (AnnisQLSemanticsException e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
		} catch (AnnisQLSyntaxException e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
		} catch (AnnisCorpusAccessException e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
		} catch (CacheInitializationException e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, null, e);
		} catch (Exception e) {
			Logger.getLogger(SearchResultServlet.class.getName()).log(Level.SEVERE, "ooops", e);
		}
		
		/* Required Stuff */
		if (scriptTag) {
		    out.write(");");
		}
		/* END Required Stuff */
	}
}
