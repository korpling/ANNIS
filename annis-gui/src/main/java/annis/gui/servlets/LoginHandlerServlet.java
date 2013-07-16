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
package annis.gui.servlets;

import annis.libgui.AnnisBaseUI;
import annis.libgui.AnnisUser;
import annis.libgui.Helper;
import com.google.common.base.Charsets;
import com.google.common.io.CharSink;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class LoginHandlerServlet extends HttpServlet
{
  
  private final static Logger log = LoggerFactory.getLogger(LoginHandlerServlet.class);

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    OutputStream out = resp.getOutputStream();
    try
    { 
      String htmlSource = Resources.toString(LoginHandlerServlet.class.getResource(
        "login.html"), Charsets.UTF_8);
      
      htmlSource = htmlSource
        .replaceAll("%usercaption%", "Username")
        .replaceAll("%passwordcaption%", "Password")
        .replaceAll("%title%", "ANNIS Login")
        .replaceAll("%referer%", "well...");
      
      OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.UTF_8);
      CharStreams.copy(new StringReader(htmlSource), writer);
      writer.close();
    }
    finally
    {
      resp.setContentType("text/html");
      resp.setStatus(200);
      out.close();
    }
  }

  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    HttpSession session = request.getSession();

    String username = request.getParameter("annis-login-user");
    String password = request.getParameter("annis-login-password");

    if (username != null && password != null)
    {
      // get the URL for the REST service
      Object annisServiceURLObject = session.getAttribute(
        AnnisBaseUI.WEBSERVICEURL_KEY);

      if (annisServiceURLObject == null || !(annisServiceURLObject instanceof String))
      {
        throw new ServletException(
          "AnnisWebService.URL was not set as init parameter in web.xml");
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
          Helper.setUser(new AnnisUser(username, client));
        }
      }
      catch (ClientHandlerException ex)
    {
//      Notification.show("Authentification error: " + ex.getMessage(),
//        Notification.Type.WARNING_MESSAGE);
    }
    catch(UniformInterfaceException ex)
    {
      if(ex.getResponse().getStatus() == Response.Status.UNAUTHORIZED.getStatusCode())
      {
//        Notification.show("Username or password wrong", ex.getMessage(), 
//          Notification.Type.WARNING_MESSAGE);
      }
      else
      {
        log.error(null, ex);
//        Notification.show("Unexpected exception: " + ex.getMessage(),
//          Notification.Type.WARNING_MESSAGE);
      }
    }
    catch (Exception ex)
    {
      log.error(null, ex);

//      Notification.show("Unexpected exception: " + ex.getMessage(),
//        Notification.Type.WARNING_MESSAGE);
    }
    }

  }
}
