package annis.frontend.servlets;

import annis.exceptions.AnnisServiceFactoryException;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisAttribute;
import annis.service.ifaces.AnnisAttributeSet;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AttributeListServlet extends HttpServlet {
	private static final long serialVersionUID = -4188886565776492022L;

  @Override
  protected void doPost(HttpServletRequest arg0, HttpServletResponse arg1) throws ServletException, IOException
  {
    doGet(arg0, arg1);
  }
	
  
  
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String corpusIdsParam = request.getParameter("corpusIds");
		
		List<Long> corpusIdList = new Vector<Long>();
				
		//gather selected corpora
		try {
			for(String corpusId : corpusIdsParam.split(","))
				corpusIdList.add(Long.parseLong(corpusId));
		} catch (NullPointerException e) {
			//We got no corpus Ids...
		} catch (NumberFormatException e) {
			//We got wrong Corpus Ids
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
		
		try {
			AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
			AnnisAttributeSet attributeList;
//
//      // add all corpora if not choosen
//      if(corpusIdsParam == null)
//      {
//        AnnisCorpusSet allCorpora = service.getCorpusSet();
//        for(AnnisCorpus c : allCorpora)
//        {
//          corpusIdList.add(c.getId());
//        }
//      }
      
			if("edge".equals(request.getParameter("type"))) {
				// not implemented yet
			} else {
				attributeList = service.getNodeAttributeSet(corpusIdList, false);
        
        // check if we should not add prefix
        if(request.getParameter("noprefix") != null)
        {
          for(AnnisAttribute att : attributeList)
          {
            String a = att.getName();
            int found = a.indexOf(':');
            if(found > -1 && (found+1) < a.length())
            {
              att.setName(a.substring(found+1));
            }
          }
        }
        
				out.write(attributeList.getJSON());
			}
		} catch (AnnisServiceFactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		/* Required Stuff */
		if (scriptTag) {
		    out.write(");");
		}
		/* END Required Stuff */
		
	}
}
