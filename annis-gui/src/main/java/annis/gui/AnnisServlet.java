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

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinServlet;
import java.util.List;
import java.util.ListIterator;
import javax.servlet.ServletException;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;

/**
 * Custom overrides for the default {@link VaadinServlet}
 * @author Thomas Krause <thomas.krause@alumni.hu-berlin.de>
 */
public class AnnisServlet extends VaadinServlet
{
  private final BootstrapListener boot = new BootstrapListener();

  @Override
  protected void servletInitialized() throws ServletException
  {
    super.servletInitialized();
   
    
    
    getService().addSessionInitListener(new SessionInitListener() {

      @Override
      public void sessionInit(SessionInitEvent event) throws ServiceException
      {
        event.getSession().addBootstrapListener(boot);
      }
    }); 
  }
  
  public static class BootstrapListener implements com.vaadin.server.BootstrapListener
  {

    @Override
    public void modifyBootstrapFragment(BootstrapFragmentResponse response)
    {
      List<Node> fragments = response.getFragmentNodes();
      ListIterator<Node> it = response.getFragmentNodes().listIterator();
      while(it.hasNext())
      {
        Node n = it.next();
        if(n instanceof Element)
        {
          Element e = (Element) n;
          if("div".equals(e.tag().getName()) && e.hasAttr("class") && e.attr(
            "class").equals("v-app"))
          {
            // remove this div
            it.remove();
            Element newDiv = new Element(Tag.valueOf("div"), "");
            newDiv.append("ANNIS is being to be loaded");
            
            it.add(newDiv);
          }
        }
      }
    }

    @Override
    public void modifyBootstrapPage(BootstrapPageResponse response)
    {
    }
    
  }
  
}
