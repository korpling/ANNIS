/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package annis.frontend.servlets;

import annis.exceptions.AnnisServiceFactoryException;
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
    catch(AnnisServiceFactoryException e)
    {
      out.println("ERROR: " + e.getMessage());
    }
    catch(AnnisServiceException e)
    {
      out.println(e.getMessage());
    }

  }
}
