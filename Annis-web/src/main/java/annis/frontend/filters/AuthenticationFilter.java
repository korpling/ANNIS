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
package annis.frontend.filters;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.NamingException;
import javax.servlet.*;
import javax.servlet.http.*;

import annis.security.AnnisSecurityManager;
import annis.security.AnnisUser;

public final class AuthenticationFilter implements Filter
{

  private AnnisSecurityManager manager;
  private Properties props;
  public static final String KEY_SECURITY_MANAGER = "securityManager";
  public static final String KEY_USER = "user";
  public static final String KEY_LOGIN_ALREADY_TRIED = "loginAlreadyTried";
  
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
  {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    
    String userName = httpRequest.getParameter("user");
    String password = httpRequest.getParameter("password");
    boolean logout = httpRequest.getParameter("logout") != null;

    HttpSession session = httpRequest.getSession();
    
    String cp = httpRequest.getContextPath();
    
    //store security manger in session
    if(session.getAttribute(KEY_SECURITY_MANAGER) == null)
    {
      session.setAttribute(KEY_SECURITY_MANAGER, this.manager);
    }
    if(logout)
    {
      session.removeAttribute(KEY_USER);
      ((HttpServletResponse) response).sendRedirect(((HttpServletRequest) request).getRequestURI());
    }
    else
    {
      try
      {
        if(session.getAttribute(KEY_USER) != null)
        {
          chain.doFilter(request, response);
        }
        else if(userName == null || password == null)
        {
          String demoEnabled = props.getProperty("enableDemo", "false");
          if(!request.isSecure() && "true".equalsIgnoreCase(demoEnabled))
          {
            // show demo account
            AnnisUser demoUser = manager.login("demo", "demo");
            session.setAttribute(KEY_USER, demoUser);
            chain.doFilter(request, response);
          }
          else
          {         
            throw new AuthenticationException("Insufficient Credentials Provided");
          }
        }
        else
        {
          session.setAttribute(KEY_LOGIN_ALREADY_TRIED, new Boolean(true));
          
          AnnisUser user = manager.login(userName, password);
         
          // we have been successfull
          session.removeAttribute(KEY_LOGIN_ALREADY_TRIED);
          session.setAttribute(KEY_USER, user);
          chain.doFilter(request, response);
        }
      }
      catch(NamingException e)
      {
        try
        {
          response.setContentType("text/html");
          PrintWriter out = response.getWriter();
          out.println("<html debug=\"true\">");
          out.println("	<head>");
          out.println("		<title>Annis&sup2; - Login</title>");
          out.println("		<link rel=\"stylesheet\" type=\"text/css\" href=\"" + cp + "/javascript/extjs/resources/css/ext-all.css\" />");
          out.println("		<script type=\"text/javascript\" src=\"" + cp + "/javascript/extjs/adapter/ext/ext-base.js\"></script>");
          out.println("		<script type=\"text/javascript\" src=\"" + cp + "/javascript/extjs/ext-all.js\"></script>");
          out.println("		<script type=\"text/javascript\" src=\"" + cp + "/javascript/annis/windowLogin.js\"></script>");
          if(session.getAttribute(KEY_LOGIN_ALREADY_TRIED) != null)
          {
            out.println("<script type=\"text/javascript\" src=\"" + 
              cp + "/javascript/annis/loginErrorMessage.js\"></script>");
          }
          out.println("	<body>");
          out.println("	</body>");
          out.println("</html>");
        }
        catch(IllegalStateException e1)
        {
          //ignore
        }
      }
    }
  }

  public void destroy()
  {
  }

  @SuppressWarnings("unchecked")
  public void init(FilterConfig filterConfig)
  {
    try
    {
      this.manager = (AnnisSecurityManager) Class.forName(filterConfig.getInitParameter("managerClassName")).newInstance();
      Enumeration<String> parameterNames = filterConfig.getInitParameterNames();
      Properties properties = new Properties();
      while(parameterNames.hasMoreElements())
      {
        String name = parameterNames.nextElement();
        properties.put(name, filterConfig.getInitParameter(name));
      }
      
      // copy annis-service url from context
      properties.put("AnnisRemoteService.URL",
        filterConfig.getServletContext().getInitParameter("AnnisRemoteService.URL")
      );
      
      this.props = properties;
      
      this.manager.setProperties(properties);
    }
    catch(InstantiationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(IllegalAccessException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch(ClassNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
