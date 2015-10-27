/*
 * Copyright 2015 Corpuslinguistic working group Humboldt University Berlin.
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

import annis.libgui.Helper;
import com.sun.jersey.api.client.WebResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletResponse;
import com.vaadin.server.VaadinSession;
import java.io.IOException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class ShortenerRequestHandler implements RequestHandler
{

  @Override
  public boolean handleRequest(VaadinSession session, VaadinRequest request,
    VaadinResponse response) throws IOException
  {
    
    String id = request.getParameter("id");
    if(id == null)
    {
      return false;
    }
    
    // get the actual URL
    WebResource res = Helper.getAnnisWebResource();
    res = res.path("shortener").path(id);
    String longURL = res.get(String.class);
    
    // redirects only work in http servlets
    if(response instanceof VaadinServletResponse)
    {
      ServletResponse servletResponse = ((VaadinServletResponse) response).getResponse();
      if(servletResponse instanceof HttpServletResponse)
      {    
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        httpResponse.setHeader("Location", request.getContextPath() +  longURL);
        httpResponse.setStatus(307); // temporary redirect
        return true;
      }
    }
    return false;
  }
  
}
