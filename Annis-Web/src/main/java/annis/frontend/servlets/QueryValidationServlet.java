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

import annis.exceptions.AnnisServiceFactoryException;
import annis.exceptions.ParseException;
import annis.ql.parser.ParserException;
import annis.service.AnnisService;
import annis.service.AnnisServiceException;
import annis.service.AnnisServiceFactory;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author thomas
 */
public class QueryValidationServlet extends HttpServlet
{

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("text/plain");

    String queryAnnisQL = new String(req.getParameter(SubmitQueryServlet.PARAM_ANNIS_QL));

    ServletOutputStream out = resp.getOutputStream();

    if(queryAnnisQL == null || "".equals(queryAnnisQL))
    {
      out.println("Empty Query");
      return;
    }
    else if("Angela Merkel".equals(queryAnnisQL))
    {
      out.println("A corpus a day keeps the dr. away!");
      return;
    }

    try
    {
      AnnisService service = AnnisServiceFactory.getClient(this.getServletContext().getInitParameter("AnnisRemoteService.URL"));
      boolean isValid = service.isValidQuery(queryAnnisQL);
      if(isValid)
      {
        out.println("Valid Query");
      }
      else
      {
        out.println("Invalid Query");
      }
    }
    catch(Exception e)
    {
      out.println(e.getLocalizedMessage());
    }

  }
}
