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

import annis.exceptions.AnnisServiceFactoryException;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import annis.frontend.filters.AuthenticationFilter;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import annis.service.AnnisService;
import annis.service.AnnisServiceFactory;
import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;

public class CorpusList extends HttpServlet {
	private static final long serialVersionUID = -3646336571272256988l;
	public static final String KEY_CORPUS_FAVORITES = "corpusFavorites";

	public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    response.setCharacterEncoding("UTF-8");
    
    String mode = request.getRequestURI().replaceFirst("/.+/.+/", "");
		
		String add = request.getParameter("add");
		String remove = request.getParameter("remove");
		

		AnnisUser user = ((AnnisUser) request.getSession().getAttribute(AuthenticationFilter.KEY_USER));
		
		Set<String> favoritesSet = new HashSet<String>();
		try {
			for(String id: user.getProperty(KEY_CORPUS_FAVORITES).split(",")) 
				favoritesSet.add(id);
		} catch (Exception e) {
			//the properties are not set... ignore this
		}
		
		if("".equals(mode))
			mode = "All";
		if(add != null || remove != null) {
			if(add != null)
				for(String id : add.split(","))
					favoritesSet.add(id);
			if(remove != null)
				for(String id : remove.split(","))
					favoritesSet.remove(id);
			int i = 0;
			StringBuffer favoritesString = new StringBuffer();
			for(String id : favoritesSet)
      {
        if(id != null && !"".equals(id))
        {
          if(i > 0)
          {
            favoritesString.append(",");
          }
          favoritesString.append(id);
          i++;
        }
      }
			user.put(KEY_CORPUS_FAVORITES, favoritesString.toString());
			AnnisSecurityManager manager = (AnnisSecurityManager) request.getSession().getAttribute(AuthenticationFilter.KEY_SECURITY_MANAGER);
			try {
				manager.storeUserProperties(user);
			} catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		Writer out = response.getWriter();
				
		/* Required Stuff */
		boolean scriptTag = false;
		String cb = request.getParameter("callback");
		if (cb != null) {
		    scriptTag = true;
		    response.setContentType("text/javascript");
		} else {
		    response.setContentType("application/x-json");
		}
		
		if (scriptTag) {
		    out.write(cb + "(");
		}
		/* END Required Stuff */
		
		//The real output
		try {
			AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
			List<Long> corpusIdList = user.getCorpusIdList();
			AnnisCorpusSet corpusSet = service.getCorpusSet();
			
			Set<AnnisCorpus> intersectionSet = new HashSet<AnnisCorpus>();
			for(AnnisCorpus c : corpusSet)
				if(	corpusIdList.contains(c.getId()) && (
							("Favorites".equals(mode) && favoritesSet.contains(Long.toString(c.getId()))) 
						||	("NoFavorites".equals(mode) && !favoritesSet.contains(Long.toString(c.getId())))
						|| "All".equals(mode)
				   ))
					intersectionSet.add(c);
			
			//we do not use the getJSON method of the corpus set since it would return all items.
		
			out.append("{\"size\":" + intersectionSet.size() + ",\"list\":[");
			int i=0;
			for(AnnisCorpus c : intersectionSet)
				out.append((i++ > 0 ? ", " : "") + c.getJSON());
			out.append("]}");
		} catch (AnnisServiceFactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/* Required Stuff */
		if (scriptTag) {
		    out.write(");");
		}
		/* END Required Stuff */
	}
}
