package annis.frontend.servlets;


import annis.exceptions.AnnisCorpusAccessException;
import annis.exceptions.AnnisQLSemanticsException;
import annis.exceptions.AnnisQLSyntaxException;
import annis.exceptions.AnnisServiceFactoryException;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import annis.frontend.servlets.visualizers.MarkableVisualizer;
import annis.frontend.servlets.visualizers.Visualizer;

import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisResult;
import annis.service.ifaces.AnnisResultSet;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ExporterServlet extends HttpServlet {

	private static final long serialVersionUID = -8182635617256833563L;

	@SuppressWarnings("unchecked")
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {		
			HttpSession session = request.getSession();
			
			Visualizer visualizer = new MarkableVisualizer();
			
			
			//depending on parameters recompute result set or take shopping cart items from cache
			if(request.getParameter("lastquery") != null) {
				//this is a full result export
				List<Long> corpusIdList = (List<Long>) session.getAttribute("SEARCH_corpusIdList");
				String queryAnnisQL = "";
				Integer totalCount = 0;
				try {
					queryAnnisQL = session.getAttribute("SEARCH_queryAnnisQL").toString();
					totalCount 	 = (Integer) session.getAttribute("SEARCH_totalCount");			
				} catch(NullPointerException e) {
					//ignore
				} catch(ClassCastException e) {
					//ignore
				}
				
				try {
					AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
					AnnisResultSet resultSet = service.getResultSet(corpusIdList, queryAnnisQL, totalCount, 0, (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_LEFT), (Integer) session.getAttribute(SubmitQueryServlet.KEY_CONTEXT_RIGHT));
					Writer writer = response.getWriter();
					
					for(AnnisResult result : resultSet) {
						visualizer.setPaula(result.getPaula());
						Map<String,String> markableMap = new HashMap<String,String>();
						//Todo change annis Result -> add getMarkerSet();
						for(int i=1; i<15; i++) {
							if(result.hasMarker(Integer.toString(i)))
								markableMap.put(result.getMarkerId(new Long(i)), "");
						}
						visualizer.setMarkableMap(markableMap);
						response.setCharacterEncoding(visualizer.getCharacterEncoding());
						response.setContentType(visualizer.getContentType());
						visualizer.writeOutput(writer);						
					}
					writer.flush();
				} catch (AnnisServiceFactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AnnisQLSemanticsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AnnisQLSyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AnnisCorpusAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				//this is an export of a preselected subset
				Set<String> shoppingCart = (Set<String>) session.getAttribute("SHOPPING_CART");
			}
	}
}
