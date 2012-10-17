/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.gui.servlets;

import annis.gui.controlpanel.CorpusListPanel;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 *
 * @author thomas
 */
public class CitationRedirectionServlet extends HttpServlet
{
  
  private static final org.slf4j.Logger log = LoggerFactory.getLogger(CitationRedirectionServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      URI uri = new URI(req.getRequestURI());
      req.getSession().setAttribute("citation", uri.getPath());
      
      String addtionalParams = "";
      if(req.getParameter(CorpusListPanel.CORPUSSET_PARAM) != null)
      {
        addtionalParams += "&" + CorpusListPanel.CORPUSSET_PARAM 
          + "=" 
          + URLEncoder.encode(req.getParameter(CorpusListPanel.CORPUSSET_PARAM), "UTF-8");
      }
      resp.sendRedirect(req.getContextPath() + "/?citation" + addtionalParams);
    }
    catch(URISyntaxException ex)
    {
      log.error(null, ex);
      
      resp.sendError(500, ex.getMessage());
    }
  }
  
}
