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
package annis.gui.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;

import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinSession;

import annis.gui.requesthandler.ShortenerRequestHandler;
import annis.libgui.AnnisBaseUI;
import annis.libgui.Helper;

/**
 *
 * @author Thomas Krause <krauseto@hu-berlin.de>
 */
public class AnnisServlet extends VaadinServlet
{
  
  protected void loadApplicationProperties(String configFile, VaadinSession session)
  {
    List<File> locations = AnnisBaseUI.getAllConfigLocations(configFile);

    // load properties in the right order
    for(File f : locations)
    {
      loadPropertyFile(f, session);
    }
  }
  
  private void loadPropertyFile(File f, VaadinSession session)
  {
   if(f.canRead() && f.isFile())
    {
      try(FileInputStream fis = new FileInputStream(f))
      {
        Properties p = new Properties();
        p.load(fis);
        
        // copy all properties to the session
        for(String name : p.stringPropertyNames())
        {
           session.setAttribute(name, p.getProperty(name));
        }
        
      }
      catch(IOException ex)
      {

      }
    }
  }

  @Override
  protected void servletInitialized() throws ServletException
  {
    super.servletInitialized();
    
    
    getService().addSessionInitListener(new SessionInitListener()
    {

      @Override
      public void sessionInit(SessionInitEvent event) throws ServiceException
      {
        VaadinSession session = event.getSession();
        if(Helper.isKickstarter(session))
        {
          // only load configurations from the WEB-INF/conf folder
          File config = new File(event.getService().getBaseDirectory(),
            "/WEB-INF/conf/annis-gui.properties");
          loadPropertyFile(config, session);
          // manually override the service URL if given
          String serviceURL = session.getConfiguration().getInitParameters().getProperty(Helper.KEY_WEB_SERVICE_URL);
          if(serviceURL != null)
          {
            session.setAttribute(Helper.KEY_WEB_SERVICE_URL, serviceURL);
          }
        }
        else
        {
          // load some additional properties from our ANNIS configuration
          loadApplicationProperties("annis-gui.properties", session);
        }
        
        event.getSession().addRequestHandler(new ShortenerRequestHandler());
      }
    });
  }
  
}
