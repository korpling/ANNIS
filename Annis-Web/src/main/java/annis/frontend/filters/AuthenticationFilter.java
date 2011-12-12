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
package annis.frontend.filters;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;

import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AuthenticationFilter implements Filter
{

  private AnnisSecurityManager manager;
  private Properties props;
  public static final String KEY_SECURITY_MANAGER = "securityManager";
  public static final String KEY_USER = "user";
  public static final String KEY_LOGIN_ALREADY_TRIED = "loginAlreadyTried";

  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
    FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    HttpSession session = httpRequest.getSession();

    //store security manger in session
    if (session.getAttribute(KEY_SECURITY_MANAGER) == null)
    {
      session.setAttribute(KEY_SECURITY_MANAGER, this.manager);
    }

    // check if logged in
    if (session.getAttribute(KEY_USER) != null)
    {
      chain.doFilter(request, response);
    }
    // allow access to LoginLogout servlet without login
    else if ("/LoginLogout".equals(httpRequest.getServletPath()))
    {
      chain.doFilter(request, response);
    }
    // not logged in or demo account
    else
    {
      String demoEnabled = props.getProperty("enableDemo", "false");
      if (!request.isSecure() && "true".equalsIgnoreCase(demoEnabled))
      {
        // show demo account
        AnnisUser demoUser;
        try
        {
          demoUser = manager.login("demo", "demo");

          session.setAttribute(KEY_USER, demoUser);
          chain.doFilter(request, response);
        }
        catch (NamingException ex)
        {
          try
          {
            httpResponse.sendError(403, "{success: false, "
              + "errors: { reason: 'No demo account available' }}");
          }
          catch (IOException e1)
          {
            Logger.getLogger(AuthenticationFilter.class.getName()).log(
              Level.SEVERE, null, e1);
          }
        }
      }
      else
      {
        try
        {
          httpResponse.sendError(403,
            "{success: false, errors: { reason: 'Not logged in and no demo account available' }}");
        }
        catch (IOException e1)
        {
          Logger.getLogger(AuthenticationFilter.class.getName()).log(
            Level.SEVERE, null, e1);
        }
      }
    }
  }

  @Override
  public void destroy()
  {
  }

  @SuppressWarnings("unchecked")
  @Override
  public void init(FilterConfig filterConfig)
  {
    try
    {
      this.manager = (AnnisSecurityManager) Class.forName(filterConfig.
        getInitParameter("managerClassName")).newInstance();
      Enumeration<String> parameterNames = filterConfig.getInitParameterNames();
      Properties properties = new Properties();
      while (parameterNames.hasMoreElements())
      {
        String name = parameterNames.nextElement();
        properties.put(name, filterConfig.getInitParameter(name));
      }

      // copy annis-service url from context
      properties.put("AnnisRemoteService.URL",
        filterConfig.getServletContext().getInitParameter(
        "AnnisRemoteService.URL"));

      this.props = properties;

      this.manager.setProperties(properties);
    }
    catch (InstantiationException e)
    {
      Logger.getLogger(AuthenticationFilter.class.getName()).log(
        Level.SEVERE, null, e);
    }
    catch (IllegalAccessException e)
    {
      Logger.getLogger(AuthenticationFilter.class.getName()).log(
        Level.SEVERE, null, e);
    }
    catch (ClassNotFoundException e)
    {
      Logger.getLogger(AuthenticationFilter.class.getName()).log(
        Level.SEVERE, null, e);
    }
  }
}
