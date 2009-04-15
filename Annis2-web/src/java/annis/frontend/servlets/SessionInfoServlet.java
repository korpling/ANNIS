/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package annis.frontend.servlets;

import annis.frontend.filters.AuthenticationFilter;
import annis.security.AnnisUser;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * A servlet for retreiving session information at the JS-GUI
 * @author thomas
 */
public class SessionInfoServlet extends HttpServlet
{

  @Override
  protected void doGet(HttpServletRequest request, 
    HttpServletResponse response) throws ServletException, IOException
  {
    Writer out = response.getWriter();
    
    if("username".equals(request.getParameter("what")))
    {
      response.setContentType("text/plain");
      HttpSession session = request.getSession();
      if(session != null)
      {
        AnnisUser  u = (AnnisUser) session.getAttribute(AuthenticationFilter.KEY_USER);
        if(u != null)
        {
          out.write("" + u.getUserName());
        }
      }
    }
    
  }

  @Override
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }
  
  
  
}
