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
package annis.gui;

import annis.gui.servlets.LoginHandlerServlet;
import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
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
import javax.servlet.ServletException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LoginRequestHandler implements RequestHandler
{

  private final static Logger log = LoggerFactory.getLogger(
    LoginRequestHandler.class);

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    if (request.getPathInfo() != null
      && request.getPathInfo().startsWith("/login")
      && "GET".equalsIgnoreCase(request.getMethod()))
    {
      return doGet(response);
    }
    else if ("POST".equalsIgnoreCase(request.getMethod())
      && request.getParameter("annis-login-password") != null
      && request.getParameter("annis-login-user") != null)
    {
      return doPost(session, request);
    }
    return false;
  }

  private boolean doGet(VaadinResponse response) throws IOException
  {
    OutputStream out = response.getOutputStream();
    try
    {
      String htmlSource = Resources.toString(LoginHandlerServlet.class.
        getResource(
        "login.html"), Charsets.UTF_8);

      htmlSource = htmlSource
        .replaceAll("%usercaption%", "Username")
        .replaceAll("%passwordcaption%", "Password")
        .replaceAll("%title%", "ANNIS Login")
        .replaceAll("%logincaption%", "Login")
        .replaceAll("%or%", "or")
        .replaceAll("%cancelcaption%", "Cancel");

      OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
      CharStreams.copy(new StringReader(htmlSource), writer);
      writer.close();

      return true;
    }
    catch (Exception ex)
    {
      log.error(null, ex);
    }
    finally
    {
      response.setContentType("text/html");
      response.setStatus(200);
      out.close();
    }
    return false;
  }

  private boolean doPost(VaadinSession session, VaadinRequest request) throws IOException
  {
    String username = request.getParameter("annis-login-user");
    String password = request.getParameter("annis-login-password");

    if (username != null && password != null)
    {
      // forget any old user information
      session.getSession().removeAttribute(AnnisBaseUI.USER_KEY);
      session.getSession().removeAttribute(AnnisBaseUI.USER_LOGIN_ERROR);

      // get the URL for the REST service
      Object annisServiceURLObject = session.getAttribute(
        AnnisBaseUI.WEBSERVICEURL_KEY);

      if (annisServiceURLObject == null || !(annisServiceURLObject instanceof String))
      {
        log.warn("AnnisWebService.URL was not set as init parameter in web.xml");
        return false;
      }

      String webserviceURL = (String) annisServiceURLObject;

      Client client = Helper.createRESTClient(username, password);

      try
      {
        WebResource res = client.resource(webserviceURL)
          .path("admin").path("is-authenticated");
        if ("true".equalsIgnoreCase(res.get(String.class)))
        {
          // everything ok, save this user configuration for re-use
          session.getSession().setAttribute(AnnisBaseUI.USER_KEY, new AnnisUser(
            username,
            client));
        }
      }
      catch (ClientHandlerException ex)
      {
        session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
          "Authentification error: \" + ex.getMessage()");
      }
      catch (UniformInterfaceException ex)
      {
        if (ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.
          getStatusCode())
        {
          session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
            "Username or password wrong");
        }
        else
        {
          log.error(null, ex);
          session.getSession().setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
            "Unexpected exception:  + ex.getMessage()");
        }
      }
      catch (Exception ex)
      {
        log.error(null, ex);
        session.setAttribute(AnnisBaseUI.USER_LOGIN_ERROR,
          "Unexpected exception:  + ex.getMessage()");
      }
      return true;
    } // end if login attempt

    return false;
  }
}
