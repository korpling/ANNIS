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

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import javax.servlet.ServletException;

/**
 *
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisVaadinServlet extends VaadinServlet
{

  @Override
  protected void servletInitialized() throws ServletException
  {
    super.servletInitialized();
    getService().addSessionInitListener(new SessionInitListener() {

      @Override
      public void sessionInit(SessionInitEvent event) throws ServiceException
      {
     //   event.getSession().addBootstrapListener(new AnnisBootstrapListener());
      }
    });
  }
  
}
