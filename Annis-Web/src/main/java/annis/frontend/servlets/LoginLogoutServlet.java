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

import annis.frontend.filters.AuthenticationFilter;
import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import java.io.IOException;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * A servlet for retreiving session information at the JS-GUI
 * @author thomas
 */
public class LoginLogoutServlet extends HttpServlet
{

  @Override
  protected void doGet(HttpServletRequest request,
    HttpServletResponse response) throws ServletException, IOException
  {
    HttpServletRequest httpRequest = (HttpServletRequest) request;

    String userName = httpRequest.getParameter("user");
    String password = httpRequest.getParameter("password");
    boolean logout = httpRequest.getParameter("logout") != null;

    HttpSession session = httpRequest.getSession();

    if(logout)
    {
      session.removeAttribute(AuthenticationFilter.KEY_USER);
      // delete cache
      session.removeAttribute(SearchResultServlet.FILESYSTEM_CACHE_RESULT);
      response.getWriter().print("logged out");
    }
    else
    {
      try
      {
        if(userName != null && password != null)
        {
          session.setAttribute(AuthenticationFilter.KEY_LOGIN_ALREADY_TRIED,
            new Boolean(true));

          AnnisSecurityManager manager = (AnnisSecurityManager) session.getAttribute(
            AuthenticationFilter.KEY_SECURITY_MANAGER);
          AnnisUser user = manager.login(userName, password);

          // we have been successfull
          response.getWriter().print("{success: true}");
          session.removeAttribute(AuthenticationFilter.KEY_LOGIN_ALREADY_TRIED);
          session.setAttribute(AuthenticationFilter.KEY_USER, user);
        }
      }
      catch(NamingException e)
      {
        try
        {
          response.sendError(403, "{success: false, errors: { reason: 'Wrong password or username' }}");
        }
        catch(IOException e1)
        {
          e1.printStackTrace();
        }
      }
    }

  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    doGet(req, resp);
  }

}
