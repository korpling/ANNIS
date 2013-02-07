/*
 * Copyright 2011 Corpuslinguistic working group Humboldt University Berlin.
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
package annis.gui.tutorial;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.PaintException;
import com.vaadin.server.PaintTarget;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

/**
 *
 * @author thomas
 */
public class TutorialPanel extends Panel
{
  private Embedded embedded;
  public TutorialPanel()
  {
    setSizeFull();   
    
    HorizontalLayout layout = new HorizontalLayout();
    layout.setSizeFull();
    setContent(layout);
    
    embedded = new Embedded();
    embedded.setSizeFull();
    layout.addComponent(embedded);
  }

  @Override
  public void attach()
  {
    String contextPath = VaadinService.getCurrentRequest().getContextPath();
    embedded.setType(Embedded.TYPE_BROWSER);
    embedded.setSource(new ExternalResource(contextPath + "/tutorial/index.html"));
    
    super.attach();
  }

  @Override
  public void paintContent(PaintTarget target) throws PaintException
  {
    super.paintContent(target);
  }
  
}
