/*
 * Copyright 2013 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.requesthandler;

import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import annis.libgui.LoginDataLostException;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class LoginServletRequestHandler implements RequestHandler
{

  private final static Logger log = LoggerFactory.getLogger(
    LoginServletRequestHandler.class);
  
  private final String prefix;
  
  public LoginServletRequestHandler(String urlPrefix)
  {
    this.prefix = urlPrefix + "/login";
  }

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if(request.getPathInfo() != null && request.getPathInfo().startsWith(prefix))
    {
      if("GET".equalsIgnoreCase(request.getMethod()))
      {
        doGet(session, request, response);
        return true;
      }
      else if("POST".equalsIgnoreCase(request.getMethod()))
      {
        doPost(session, request, response);
        return true;
      }
    }
    return false; 
  }
  
  private void doGet(VaadinSession session, VaadinRequest request,
    VaadinResponse response)
  {
    response.setContentType("text/html");
    OutputStream out = null;
    try
    {
      out = response.getOutputStream();

      String htmlSource = Resources.toString(LoginServletRequestHandler.class.
        getResource(
        "login.html"), Charsets.UTF_8);

      htmlSource = htmlSource
        .replaceAll("%usercaption%", "Username")
        .replaceAll("%passwordcaption%", "Password")
        .replaceAll("%title%", "ANNIS Login")
        .replaceAll("%logincaption%", "Login")
        .replaceAll("%or%", "or")
        .replaceAll("%cancelcaption%", "Cancel");

      try (OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8))
      {
        CharStreams.copy(new StringReader(htmlSource), writer);
        out.flush();
      }
    }
    catch (IOException ex)
    {
      log.error(null, ex);
    }
    catch(Exception ex)
    {
      log.error(null, ex);
    }
    finally
    {
      response.setStatus(200);
      if(out != null)
      {
        try
        {
          out.close();
        }
        catch (IOException ex)
        {
          log.error("closing OutputStream filed", ex);
        }
      }
    }
  }

  private void doPost(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    
    response.setContentType("text/html");
    
    String username = request.getParameter("annis-login-user");
    String password = request.getParameter("annis-login-password");

    if (username != null && password != null)
    {
      // forget any old user information
      session.getSession().removeAttribute(AnnisBaseUI.USER_KEY);
      session.getSession().removeAttribute(AnnisBaseUI.USER_LOGIN_ERROR);

      // get the URL for the REST service
      Object annisServiceURLObject = session.getSession().getAttribute(
        AnnisBaseUI.WEBSERVICEURL_KEY);

      if (annisServiceURLObject == null || !(annisServiceURLObject instanceof String))
      {
        log.warn("AnnisWebService.URL was not set as init parameter in web.xml");
      }

      String webserviceURL = (String) annisServiceURLObject;

      try
      {
        AnnisUser user = new AnnisUser(username, password);
        WebResource res = user.getClient().resource(webserviceURL)
          .path("admin").path("is-authenticated");
        if ("true".equalsIgnoreCase(res.get(String.class)))
        {
          // everything ok, save this user configuration for re-use
          Helper.setUser(user);
        }
      }
      catch (ClientHandlerException ex)
      {
        session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
          "Authentification error: " + ex.getMessage());
        response.setStatus(502); // bad gateway
      }
      catch (LoginDataLostException ex)
      {
        session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
          "Lost password in memory. Sorry.");
        response.setStatus(500); // server error
      }
      catch (UniformInterfaceException ex)
      {
        if (ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.
          getStatusCode())
        {
          session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
            "Username or password wrong");
          response.setStatus(Response.Status.UNAUTHORIZED.getStatusCode());
        }
        else if (ex.getResponse().getStatus() == Response.Status.FORBIDDEN.
          getStatusCode())
        {
          session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
            "Account has expired");
          response.setStatus(Response.Status.FORBIDDEN.getStatusCode()); // Forbidden
        }
        else
        {
          log.error(null, ex);
          session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
            "Unexpected exception: " + ex.getMessage());
          response.setStatus(500);
        }
      }

      try (OutputStreamWriter writer = new OutputStreamWriter(response.
        getOutputStream(), Charsets.UTF_8)) 
      {
        String html = Resources.toString(LoginServletRequestHandler.class.getResource(
          "closelogin.html"), Charsets.UTF_8);
        CharStreams.copy(new StringReader(html), writer);
      }

    } // end if login attempt

  }
}
